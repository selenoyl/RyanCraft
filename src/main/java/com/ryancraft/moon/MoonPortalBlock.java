package com.ryancraft.moon;

import com.mojang.serialization.MapCodec;
import com.ryancraft.RyanCraft;
import com.ryancraft.sky.SkyPortalShape;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * The jade portal to the Hollow Moon: a nether portal's shape framed in Blocks of Jade, lit with an eye of ender at
 * night under a full moon. Its surface is the night sky, stars drifting through a slow swirl.
 * <p>The trip is one way: you land on the moon with no portal back, and a jade frame won't light up there. The way home
 * is the portal that opens in a jade temple when its Great Moon Rabbit falls (it stays open for later trips), leading
 * to your matching spot on Earth, where a jade portal is built for you. {@link #WAY_HOME_OPEN} would let any jade frame
 * on the moon light (off).
 */
public class MoonPortalBlock extends NetherPortalBlock {
    public static final MapCodec<NetherPortalBlock> CODEC = simpleCodec(MoonPortalBlock::new);
    public static final ResourceKey<Level> MOON = ResourceKey.create(Registries.DIMENSION, RyanCraft.id("moon"));
    /** May any jade frame be lit on the moon? (No: the way home is through the rabbit.) */
    public static final boolean WAY_HOME_OPEN = false;

    public MoonPortalBlock(Properties p) { super(p); }
    @Override public MapCodec<NetherPortalBlock> codec() { return CODEC; }
    @Override protected void randomTick(BlockState s, ServerLevel level, BlockPos pos, RandomSource r) {}

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos nPos, BlockState n, RandomSource r) {
        boolean inPlane = dir.getAxis() == state.getValue(AXIS) || dir.getAxis() == Direction.Axis.Y;
        if (inPlane && !n.is(this) && !n.is(MoonBlocks.JADE_BLOCK)) return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (r.nextInt(5) == 0) level.addParticle(ParticleTypes.END_ROD, pos.getX() + r.nextDouble(), pos.getY() + r.nextDouble(), pos.getZ() + r.nextDouble(), 0, 0.01, 0);
        if (r.nextInt(3) == 0) level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + r.nextDouble(), pos.getY() + r.nextDouble(), pos.getZ() + r.nextDouble(), 0, 0.02, 0);
    }

    /** Full moon: the first night of vanilla's eight-day cycle. */
    static boolean fullMoonNight(Level level) { return level.isDarkOutside() && Math.floorMod(level.getDayTime() / 24000L, 8L) == 0; }

    public static void init() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.ENDER_EYE) || player.isSpectator()) return InteractionResult.PASS;
            BlockPos at = hit.getBlockPos().relative(hit.getDirection());
            if (!level.getBlockState(hit.getBlockPos()).is(MoonBlocks.JADE_BLOCK)) return InteractionResult.PASS;
            boolean onMoon = level.dimension().equals(MOON);
            for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
                SkyPortalShape shape = SkyPortalShape.find(level, at, axis, MoonBlocks.JADE_BLOCK, MoonBlocks.MOON_PORTAL);
                if (shape == null) continue;
                if (level.isClientSide()) return InteractionResult.SUCCESS;
                if (onMoon && !WAY_HOME_OPEN) { player.displayClientMessage(Component.translatable("message.ryancraft.moon_portal_sealed"), true); return InteractionResult.FAIL; }
                if (!onMoon && !fullMoonNight(level)) { player.displayClientMessage(Component.translatable("message.ryancraft.moon_portal_wait"), true); return InteractionResult.FAIL; }
                shape.fill(level);
                level.playSound(null, at, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.6f, 1.4f);
                stack.consume(1, player);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    public TeleportTransition getPortalDestination(ServerLevel from, Entity e, BlockPos pos) {
        boolean up = !from.dimension().equals(MOON);
        ServerLevel to = from.getServer().getLevel(up ? MOON : Level.OVERWORLD);
        if (to == null) return null;
        BlockPos target = findPortal(to, pos);
        if (target == null) target = up ? landing(to, pos) : buildPortal(to, pos);     // the moon end gets no portal: one way
        return new TeleportTransition(to, Vec3.atBottomCenterOf(target), Vec3.ZERO, e.getYRot(), e.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    private static BlockPos findPortal(ServerLevel level, BlockPos near) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockPos best = null; double bestD = Double.MAX_VALUE;
        for (int dx = -32; dx <= 32; dx++) for (int dz = -32; dz <= 32; dz++) {
            int x = near.getX() + dx, z = near.getZ() + dz;
            int top = Math.min(level.getMaxY(), level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1);
            for (int y = level.getMinY(); y <= top; y++) {
                m.set(x, y, z);
                if (level.getBlockState(m).is(MoonBlocks.MOON_PORTAL) && !level.getBlockState(m.below()).is(MoonBlocks.MOON_PORTAL)) {
                    double d = near.distSqr(m);
                    if (d < bestD) { bestD = d; best = m.immutable(); }
                    break;
                }
            }
        }
        return best;
    }

    /** A clear spot on the moon's surface near x/z. */
    private static BlockPos landing(ServerLevel level, BlockPos near) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near.getX(), near.getZ());
        return new BlockPos(near.getX(), Math.max(y, level.getMinY() + 2), near.getZ());
    }

    /** Home: a jade-framed portal (2x3 inside) at the matching spot on Earth's surface. */
    private static BlockPos buildPortal(ServerLevel level, BlockPos near) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near.getX(), near.getZ());
        BlockPos base = new BlockPos(near.getX(), Math.max(y, level.getSeaLevel() + 1), near.getZ());
        BlockState jade = MoonBlocks.JADE_BLOCK.defaultBlockState(), portal = MoonBlocks.MOON_PORTAL.defaultBlockState().setValue(AXIS, Direction.Axis.X);
        for (int x = -1; x <= 2; x++) for (int dy = -1; dy <= 3; dy++) {
            boolean edge = x == -1 || x == 2 || dy == -1 || dy == 3;
            level.setBlock(base.offset(x, dy, 0), edge ? jade : portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        for (int x = -1; x <= 2; x++) for (int dy = 0; dy <= 3; dy++)
            for (int z : new int[]{-1, 1}) if (!level.getBlockState(base.offset(x, dy, z)).isAir()) level.setBlockAndUpdate(base.offset(x, dy, z), Blocks.AIR.defaultBlockState());
        return base;
    }
}

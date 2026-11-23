package com.ryancraft.sky;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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
 * The Skylands portal: a nether portal's shape and feel, framed in glowstone, leading between the overworld and the
 * Skylands. The far end finds a portal within 32 blocks, or builds one (on an island, or on a little skystone ledge).
 */
public class SkyPortalBlock extends NetherPortalBlock {
    public static final MapCodec<NetherPortalBlock> CODEC = simpleCodec(SkyPortalBlock::new);
    public SkyPortalBlock(Properties p) { super(p); }
    @Override public MapCodec<NetherPortalBlock> codec() { return CODEC; }

    @Override protected void randomTick(BlockState s, ServerLevel level, BlockPos pos, RandomSource r) {}   // no zombified piglins

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos nPos, BlockState n, RandomSource r) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean inPlane = dir.getAxis() == axis || dir.getAxis() == Direction.Axis.Y;
        if (inPlane && !n.is(this) && !n.is(Blocks.GLOWSTONE)) return Blocks.AIR.defaultBlockState();   // a broken frame goes out
        return state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (r.nextInt(3) == 0) level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + r.nextDouble(), pos.getY() + r.nextDouble(), pos.getZ() + r.nextDouble(), 0, 0, 0);
        if (r.nextInt(4) == 0) level.addParticle(ParticleTypes.CLOUD, pos.getX() + r.nextDouble(), pos.getY() + r.nextDouble(), pos.getZ() + r.nextDouble(), 0, 0.02, 0);
    }

    @Override
    public TeleportTransition getPortalDestination(ServerLevel from, Entity e, BlockPos pos) {
        boolean up = !from.dimension().equals(Skylands.LEVEL);
        ServerLevel to = from.getServer().getLevel(up ? Skylands.LEVEL : Level.OVERWORLD);
        if (to == null) return null;
        BlockPos target = findPortal(to, pos);
        if (target == null) target = buildPortal(to, pos, up);
        Vec3 at = Vec3.atBottomCenterOf(target);
        return new TeleportTransition(to, at, Vec3.ZERO, e.getYRot(), e.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    /** The lowest portal block of a portal within 32 blocks (horizontally) of x/z, if there is one. */
    private static BlockPos findPortal(ServerLevel level, BlockPos near) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockPos best = null; double bestD = Double.MAX_VALUE;
        for (int dx = -32; dx <= 32; dx++) for (int dz = -32; dz <= 32; dz++) {
            int x = near.getX() + dx, z = near.getZ() + dz;
            int top = Math.min(level.getMaxY(), level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1);
            for (int y = level.getMinY(); y <= top; y++) {
                m.set(x, y, z);
                if (level.getBlockState(m).is(Skylands.SKY_PORTAL) && !level.getBlockState(m.below()).is(Skylands.SKY_PORTAL)) {
                    double d = near.distSqr(m);
                    if (d < bestD) { bestD = d; best = m.immutable(); }
                    break;
                }
            }
        }
        return best;
    }

    /** Build a 2x3 glowstone-framed portal near x/z: on the ground below, on an island in the Skylands, or on a ledge. */
    private static BlockPos buildPortal(ServerLevel level, BlockPos near, boolean sky) {
        BlockPos base = null;
        for (int r = 0; r <= 24 && base == null; r += 4)                                         // look for ground nearby
            for (int dx = -r; dx <= r && base == null; dx += 4) for (int dz = -r; dz <= r && base == null; dz += 4) {
                int x = near.getX() + dx, z = near.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y > level.getMinY() + 1 && level.getBlockState(new BlockPos(x, y - 1, z)).isSolidRender()) base = new BlockPos(x, y, z);
            }
        if (base == null) {                                                                    // open sky: a little skystone ledge
            base = new BlockPos(near.getX(), sky ? 120 : Math.max(level.getSeaLevel() + 1, near.getY()), near.getZ());
            for (int dx = -2; dx <= 3; dx++) for (int dz = -2; dz <= 2; dz++) level.setBlockAndUpdate(base.offset(dx, -1, dz), Skylands.SKYSTONE.defaultBlockState());
        }
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState portal = Skylands.SKY_PORTAL.defaultBlockState().setValue(AXIS, Direction.Axis.X);
        for (int x = -1; x <= 2; x++) for (int y = -1; y <= 3; y++) {
            BlockPos p = base.offset(x, y, 0);
            boolean edge = x == -1 || x == 2 || y == -1 || y == 3;
            level.setBlock(p, edge ? glow : portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        for (int x = -1; x <= 2; x++) for (int y = 0; y <= 3; y++)                            // clear space to step out
            for (int z : new int[]{-1, 1}) if (!level.getBlockState(base.offset(x, y, z)).isAir() && y > 0) level.setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
        return base;
    }
}

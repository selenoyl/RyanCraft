package com.ryancraft.moon;

import com.mojang.serialization.MapCodec;
import com.ryancraft.RyanCraft;
import com.ryancraft.entity.BossMeteor;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Force field generator: the moon base's shield. Feed it a nether star (a long charge) or a block of meteor iron (a
 * shorter, moon-made one) and it holds a sphere of 8, 12 or 16 blocks (sneak + right-click to change): meteors are
 * caught at its edge, and inside it you can breathe on the moon. A bigger field burns fuel faster, and every meteor it
 * stops costs a little more. Redstone pauses it; right-click shows its charge.
 */
public final class ForceField {
    public static final int[] RADII = {8, 12, 16};
    public static final int STAR = 48000, METEOR_BLOCK = 6000, METEOR_COST = 300, MAX = STAR * 4;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    /** Running fields, per dimension: position -> radius. */
    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> LIVE = new ConcurrentHashMap<>();

    public static boolean inside(Level level, Vec3 p) {
        Map<BlockPos, Integer> m = LIVE.get(level.dimension());
        if (m == null) return false;
        for (var e : m.entrySet()) if (e.getKey().getCenter().distanceToSqr(p) < (double) e.getValue() * e.getValue()) return true;
        return false;
    }

    public static class GeneratorBlock extends BaseEntityBlock {
        public static final MapCodec<GeneratorBlock> CODEC = simpleCodec(GeneratorBlock::new);
        public GeneratorBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(BlockStateProperties.POWERED, false)); }
        @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(ACTIVE, BlockStateProperties.POWERED); }
        @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
        @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Generator(pos, state); }

        @Override
        public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide() ? null : createTickerHelper(type, GENERATOR, (l, p, s, be) -> be.serverTick((ServerLevel) l, p, s));
        }

        @Override
        protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.@Nullable Orientation o, boolean moved) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(BlockStateProperties.POWERED)) level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, powered), 3);
        }

        @Override
        protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!(level.getBlockEntity(pos) instanceof Generator g)) return InteractionResult.PASS;
            int add = stack.is(Items.NETHER_STAR) ? STAR : stack.is(MoonBlocks.METEOR_IRON_BLOCK.asItem()) ? METEOR_BLOCK : 0;
            if (add == 0) return InteractionResult.TRY_WITH_EMPTY_HAND;
            if (g.charge + add > MAX) { if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message.ryancraft.force_field_full"), true); return InteractionResult.FAIL; }
            if (!level.isClientSide()) { g.charge += add; g.setChanged(); stack.consume(1, player); level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.8f, 1.4f); }
            return InteractionResult.SUCCESS;
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!(level.getBlockEntity(pos) instanceof Generator g)) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown()) { g.size = (g.size + 1) % RADII.length; g.setChanged(); }
                int mins = g.charge / (20 * 60) / Math.max(1, g.costPerTick());
                player.displayClientMessage(Component.translatable("message.ryancraft.force_field", RADII[g.size], g.charge * 100 / MAX, mins), true);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
            if (!state.getValue(ACTIVE)) return;
            int rad = 12; if (level.getBlockEntity(pos) instanceof Generator g) rad = RADII[g.size];
            for (int i = 0; i < 6; i++) {                                          // shimmer at the field's edge
                Vec3 d = new Vec3(r.nextGaussian(), Math.abs(r.nextGaussian()), r.nextGaussian()).normalize().scale(rad);
                level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + d.x, pos.getY() + 0.5 + d.y, pos.getZ() + 0.5 + d.z, 0, 0, 0);
            }
        }
    }

    public static class Generator extends BlockEntity {
        int charge, size = 1;
        public Generator(BlockPos pos, BlockState state) { super(GENERATOR, pos, state); }
        int costPerTick() { int r = RADII[size]; return Math.max(1, r * r / 64); }

        void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
            boolean run = charge > 0 && !state.getValue(BlockStateProperties.POWERED);
            if (run != state.getValue(ACTIVE)) level.setBlock(pos, state.setValue(ACTIVE, run), 3);
            Map<BlockPos, Integer> live = LIVE.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
            if (!run) { live.remove(pos); return; }
            int rad = RADII[size]; live.put(pos.immutable(), rad);
            charge -= costPerTick();
            Vec3 c = pos.getCenter();
            for (BossMeteor m : level.getEntitiesOfClass(BossMeteor.class, new AABB(pos).inflate(rad + 4), m -> m.position().distanceToSqr(c) < (rad + 1.5) * (rad + 1.5))) {
                level.sendParticles(ParticleTypes.EXPLOSION, m.getX(), m.getY(), m.getZ(), 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, m.getX(), m.getY(), m.getZ(), 12, 0.4, 0.4, 0.4, 0.05);
                level.playSound(null, m.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.BLOCKS, 1f, 0.6f);
                m.discard(); charge -= METEOR_COST;
            }
            if (charge < 0) charge = 0;
            if (level.getGameTime() % 40 == 0) setChanged();
        }

        @Override public void setRemoved() { super.setRemoved(); if (level != null) { var m = LIVE.get(level.dimension()); if (m != null) m.remove(worldPosition); } }
        @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Charge", charge); out.putInt("Size", size); }
        @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); charge = in.getIntOr("Charge", 0); size = in.getIntOr("Size", 1); }
    }

    public static Block GENERATOR_BLOCK;
    public static BlockEntityType<Generator> GENERATOR;

    public static void init(Block block) {
        GENERATOR_BLOCK = block;
        GENERATOR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("force_field_generator"),
                FabricBlockEntityTypeBuilder.create(Generator::new, block).build());
    }

    private ForceField() {}
}

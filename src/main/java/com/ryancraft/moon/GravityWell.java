package com.ryancraft.moon;

import com.mojang.serialization.MapCodec;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gravity well: moonstone gravity, bound with meteor iron. It sets a sphere of moon gravity ({@link #RADIUS} blocks)
 * around itself wherever it's placed, Earth included: floaty jumps, slow falls, gentle landings, items that drift
 * instead of drop. Redstone switches it off. (MoonPhysics asks {@link #inField} for every falling thing.)
 */
public final class GravityWell {
    public static final int RADIUS = 6;
    private static final Map<ResourceKey<Level>, Set<BlockPos>> LIVE = new ConcurrentHashMap<>();

    public static boolean inField(Level level, Vec3 p) {
        Set<BlockPos> s = LIVE.get(level.dimension());
        if (s == null || s.isEmpty()) return false;
        for (BlockPos b : s) if (b.getCenter().distanceToSqr(p) < RADIUS * RADIUS) return true;
        return false;
    }

    public static class WellBlock extends BaseEntityBlock {
        public static final MapCodec<WellBlock> CODEC = simpleCodec(WellBlock::new);
        public WellBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWERED, false)); }
        @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BlockStateProperties.POWERED); }
        @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
        @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Well(pos, state); }

        @Override
        public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return createTickerHelper(type, WELL, (l, p, s, be) -> {                 // both sides: gravity is worked out on each
                Set<BlockPos> live = LIVE.computeIfAbsent(l.dimension(), k -> ConcurrentHashMap.newKeySet());
                if (s.getValue(BlockStateProperties.POWERED)) live.remove(p); else live.add(p.immutable());
            });
        }

        @Override
        protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.@Nullable Orientation o, boolean moved) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(BlockStateProperties.POWERED)) level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, powered), 3);
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
            if (state.getValue(BlockStateProperties.POWERED)) return;
            for (int i = 0; i < 2; i++) {                                           // motes drifting slowly upward
                Vec3 d = new Vec3(r.nextGaussian(), r.nextGaussian(), r.nextGaussian()).normalize().scale(r.nextDouble() * RADIUS);
                level.addParticle(ParticleTypes.WHITE_ASH, pos.getX() + 0.5 + d.x, pos.getY() + 0.5 + d.y, pos.getZ() + 0.5 + d.z, 0, 0.02, 0);
            }
        }
    }

    public static class Well extends BlockEntity {
        public Well(BlockPos pos, BlockState state) { super(WELL, pos, state); }
        @Override public void setRemoved() { super.setRemoved(); if (level != null) { var s = LIVE.get(level.dimension()); if (s != null) s.remove(worldPosition); } }
    }

    public static BlockEntityType<Well> WELL;
    public static void init(Block block) {
        WELL = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("gravity_well"), FabricBlockEntityTypeBuilder.create(Well::new, block).build());
    }

    private GravityWell() {}
}

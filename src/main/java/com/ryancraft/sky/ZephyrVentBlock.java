package com.ryancraft.sky;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Zephyr vent: a skystone vent breathing a column of rising air (up to {@link #HEIGHT} blocks, stopped by anything
 * solid). Whatever stands in it is lifted, players, pegasi, sheep and dropped items alike, and takes no fall damage
 * while in it. Found on the islands; craft one from skystone round a zephyrite.
 */
public class ZephyrVentBlock extends BaseEntityBlock {
    public static final MapCodec<ZephyrVentBlock> CODEC = simpleCodec(ZephyrVentBlock::new);
    public static final int HEIGHT = 12;
    private static final double LIFT = 0.11, MAX_RISE = 0.55;

    public ZephyrVentBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Entity_(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Skylands.ZEPHYR_VENT_ENTITY, (l, pos, s, be) -> lift(l, pos));
    }

    /** How high the column reaches before something solid stops it. */
    static int reach(Level level, BlockPos pos) {
        for (int i = 1; i <= HEIGHT; i++) {
            BlockPos p = pos.above(i);
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) return i - 1;
        }
        return HEIGHT;
    }

    /** Players and what they ride move on their own client, so each side lifts only what it moves. */
    private static void lift(Level level, BlockPos pos) {
        int h = reach(level, pos);
        if (h <= 0) return;
        AABB column = new AABB(pos.getX() + 0.1, pos.getY() + 1, pos.getZ() + 0.1, pos.getX() + 0.9, pos.getY() + 1 + h, pos.getZ() + 0.9);
        for (Entity e : level.getEntities((Entity) null, column, e -> !e.isSpectator() && !e.noPhysics)) {
            boolean clientMoved = e instanceof Player || e.getControllingPassenger() instanceof Player;
            if (level.isClientSide() ? !e.isLocalInstanceAuthoritative() : clientMoved) continue;
            Vec3 v = e.getDeltaMovement();
            double top = pos.getY() + 1 + h, near = Math.max(0.15, Math.min(1, (top - e.getY()) / 3));   // eases off near the top
            if (v.y < MAX_RISE * near) e.setDeltaMovement(v.x, Math.min(MAX_RISE * near, v.y + LIFT + (v.y < 0 ? -v.y * 0.5 : 0)), v.z);
            e.resetFallDistance();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        int h = reach(level, pos);
        for (int i = 0; i < 2 && h > 0; i++)
            level.addParticle(r.nextInt(3) == 0 ? ParticleTypes.CLOUD : ParticleTypes.WHITE_ASH, pos.getX() + 0.2 + r.nextDouble() * 0.6,
                    pos.getY() + 1.05 + r.nextDouble() * h * 0.5, pos.getZ() + 0.2 + r.nextDouble() * 0.6, 0, 0.12 + r.nextDouble() * 0.1, 0);
    }

    /** The vent's (empty) block entity: it only carries the ticker. */
    public static class Entity_ extends BlockEntity {
        public Entity_(BlockPos pos, BlockState state) { super(Skylands.ZEPHYR_VENT_ENTITY, pos, state); }
    }
}

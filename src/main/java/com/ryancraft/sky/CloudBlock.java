package com.ryancraft.sky;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** White cloud: you can stand on it, and landing on it never hurts. */
public class CloudBlock extends HalfTransparentBlock {
    public CloudBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends HalfTransparentBlock> codec() { return simpleCodec(CloudBlock::new); }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0f, level.damageSources().fall());   // a soft landing
    }

    /** Blue cloud: springs you back up higher than you fell, to hop between islands. */
    public static class Bouncy extends CloudBlock {
        public Bouncy(Properties p) { super(p); }
        @Override protected MapCodec<? extends HalfTransparentBlock> codec() { return simpleCodec(Bouncy::new); }
        @Override
        public void updateEntityMovementAfterFallOn(BlockGetter level, Entity e) {
            if (e.isSuppressingBounce()) { super.updateEntityMovementAfterFallOn(level, e); return; }
            Vec3 v = e.getDeltaMovement();
            if (v.y < 0) e.setDeltaMovement(v.x, Math.min(1.6, -v.y * 1.1 + 0.6), v.z);
        }
        @Override
        public void stepOn(Level level, BlockPos pos, BlockState state, Entity e) {
            if (!e.isSteppingCarefully() && e.onGround() && e.getDeltaMovement().y <= 0.01) e.setDeltaMovement(e.getDeltaMovement().x, 0.9, e.getDeltaMovement().z);
            super.stepOn(level, pos, state, e);
        }
    }

    /** Storm cloud: not solid; you sink slowly through it, safe from the fall. */
    public static class Storm extends CloudBlock {
        public Storm(Properties p) { super(p); }
        @Override protected MapCodec<? extends HalfTransparentBlock> codec() { return simpleCodec(Storm::new); }
        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity e, InsideBlockEffectApplier fx, boolean b) {
            Vec3 v = e.getDeltaMovement();
            e.setDeltaMovement(v.x * 0.8, Math.max(v.y, -0.08), v.z * 0.8);
            e.resetFallDistance();
        }
    }
}

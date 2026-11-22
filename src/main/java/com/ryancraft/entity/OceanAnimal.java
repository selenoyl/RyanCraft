package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Big ocean swimmers (whales, orcas, sharks, rays, jellyfish), with the dolphin's smooth swimming.
 * Air-breathers come up every so often to breathe and blow a spout.
 */
public abstract class OceanAnimal extends WaterAnimal implements CritterVisual {
    private int breath;

    protected OceanAnimal(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    /** Ticks between breaths for air-breathers (whales, orcas), or 0 for gill-breathers. */
    protected int breathInterval() { return 0; }
    /** How tall the spout is. */
    protected float spoutSize() { return 1.0f; }
    protected double swimSpeed() { return 1.0; }

    @Override public int getVariant() { return 0; }

    @Override protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this, level); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        if (breathInterval() > 0) this.goalSelector.addGoal(1, new SurfaceGoal());
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, swimSpeed(), 10));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void travelInWater(Vec3 input, double gravity, boolean falling, double fallSpeed) {
        if (this.isInWater()) {
            this.moveRelative(this.getSpeed(), input);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travelInWater(input, gravity, falling, fallSpeed);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (breathInterval() > 0 && this.isInWater()) breath++;
    }

    /** Only in water, with water above too. */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return super.checkSpawnRules(level, reason);
        BlockPos p = this.blockPosition();
        return level.getFluidState(p).is(FluidTags.WATER) && level.getFluidState(p.above()).is(FluidTags.WATER);
    }

    /** Swim up, break the surface, blow, and go back down. */
    class SurfaceGoal extends Goal {
        private int hold;
        SurfaceGoal() { this.setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() { return breath > breathInterval() && OceanAnimal.this.isInWater(); }
        @Override public boolean canContinueToUse() { return breath > 0; }
        @Override public void start() { hold = 0; }

        @Override public void tick() {
            OceanAnimal self = OceanAnimal.this;
            BlockPos.MutableBlockPos p = self.blockPosition().mutable();
            int guard = 0;
            while (self.level().getFluidState(p.above()).is(FluidTags.WATER) && guard++ < 64) p.move(0, 1, 0);
            double surface = p.getY() + 0.9 - self.getBbHeight() * 0.6;
            Vec3 ahead = self.getLookAngle().scale(2);
            self.getMoveControl().setWantedPosition(self.getX() + ahead.x, surface, self.getZ() + ahead.z, 1.0);
            if (self.getY() >= surface - 0.4) {
                if (hold++ == 0 && self.level() instanceof ServerLevel sl) {
                    double top = self.getY() + self.getBbHeight();
                    sl.sendParticles(ParticleTypes.CLOUD, self.getX(), top + 0.5, self.getZ(), (int) (25 * spoutSize()), 0.15, 0.8 * spoutSize(), 0.15, 0.05);
                    sl.sendParticles(ParticleTypes.SPLASH, self.getX(), top + 0.3, self.getZ(), (int) (40 * spoutSize()), 0.3, 0.6 * spoutSize(), 0.3, 0.2);
                    self.playSound(SoundEvents.PLAYER_SPLASH, 1.2f, 0.6f);
                }
                if (hold > 40) breath = 0;      // linger a moment at the top
            }
        }
    }
}

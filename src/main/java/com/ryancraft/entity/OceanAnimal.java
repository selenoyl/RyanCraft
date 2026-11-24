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
        if (breathInterval() > 0) this.goalSelector.addGoal(5, new DeepCruiseGoal());
        else this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, swimSpeed(), 10));
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

    /** How far below the surface air-breathers like to cruise between breaths. */
    protected int cruiseDepth() { return 8; }

    /**
     * Whales and orcas keep to deep water: long, straight glides to a point 16-32 blocks ahead (roughly the way they're
     * already facing, so they visibly swim forward instead of milling about), at least {@link #cruiseDepth()} below the
     * surface and clear of the floor. Of a few candidate points, the one over the deepest water wins, so they drift
     * out of shallows and bays toward open ocean.
     */
    class DeepCruiseGoal extends Goal {
        private int cooldown;
        DeepCruiseGoal() { this.setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (OceanAnimal.this.isVehicle() || !OceanAnimal.this.isInWater()) return false;
            if (cooldown > 0) { cooldown--; return false; }
            return OceanAnimal.this.getNavigation().isDone();
        }
        @Override public boolean canContinueToUse() { return !OceanAnimal.this.getNavigation().isDone() && OceanAnimal.this.isInWater(); }

        @Override public void start() {
            OceanAnimal self = OceanAnimal.this;
            Vec3 best = null; int bestDepth = -1;
            for (int i = 0; i < 6; i++) {
                float yaw = (self.getYRot() + (self.random.nextFloat() - 0.5f) * 120f) * ((float) Math.PI / 180f);
                double dist = 16 + self.random.nextDouble() * 16;
                int x = net.minecraft.util.Mth.floor(self.getX() - Math.sin(yaw) * dist), z = net.minecraft.util.Mth.floor(self.getZ() + Math.cos(yaw) * dist);
                int[] col = column(x, net.minecraft.util.Mth.floor(self.getY()), z);
                if (col == null) continue;
                int floor = col[0], surface = col[1], depth = surface - floor;
                if (depth <= bestDepth) continue;
                int lo = floor + 3, hi = Math.max(lo, surface - cruiseDepth());
                if (depth < cruiseDepth() + 4) { lo = hi = (floor + surface) / 2; }       // shallow: just stay mid-water
                best = new Vec3(x + 0.5, lo + self.random.nextInt(hi - lo + 1), z + 0.5); bestDepth = depth;
            }
            if (best != null) self.getNavigation().moveTo(best.x, best.y, best.z, swimSpeed());
            cooldown = 20 + self.random.nextInt(60);
        }

        /** The water column at x, z near y: {floor (first water block above the bottom), surface (top water block)}, or null if it's not water. */
        private int[] column(int x, int y, int z) {
            Level lv = OceanAnimal.this.level();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, y, z);
            if (!lv.isLoaded(p)) return null;
            int guard = 0;
            if (!lv.getFluidState(p).is(FluidTags.WATER)) {               // above or below the water at this spot: look nearby
                while (guard++ < 24 && !lv.getFluidState(p).is(FluidTags.WATER)) p.move(0, -1, 0);
                if (!lv.getFluidState(p).is(FluidTags.WATER)) return null;
            }
            int top = p.getY(); guard = 0;
            while (guard++ < 96 && lv.getFluidState(p.set(x, top + 1, z)).is(FluidTags.WATER)) top++;
            int bottom = p.set(x, top, z).getY(); guard = 0;
            while (guard++ < 128 && lv.getFluidState(p.set(x, bottom - 1, z)).is(FluidTags.WATER)) bottom--;
            return new int[]{bottom, top};
        }
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
                if (hold > 40) breath = -self.random.nextInt(breathInterval() / 2 + 1);   // linger a moment, then the next breath comes a little later or sooner
            }
        }
    }
}

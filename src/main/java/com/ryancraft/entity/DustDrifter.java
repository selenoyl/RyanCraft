package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.EnumSet;

/**
 * Dust drifter: a jellyfish-like thing of the moon, pale as moonlight, floating slowly a few blocks over the craters,
 * pulsing as it goes. Harmless, and a little luminous dust falls from it when it's struck down.
 */
public class DustDrifter extends PathfinderMob implements CritterVisual {
    public DustDrifter(EntityType<? extends DustDrifter> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.FLYING_SPEED, 0.18).add(Attributes.MOVEMENT_SPEED, 0.12);
    }

    @Override protected PathNavigation createNavigation(Level level) { FlyingPathNavigation n = new FlyingPathNavigation(this, level); n.setCanFloat(true); return n; }
    @Override protected void registerGoals() { goalSelector.addGoal(1, new DriftGoal()); }
    @Override public int getVariant() { return 0; }
    @Override public boolean isFlyingVisual() { return true; }
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
    @Override public boolean isInWater() { return true; }                       // so the critter model pulses like a jellyfish swimming

    @Override
    public void aiStep() {
        super.aiStep();
        setDeltaMovement(getDeltaMovement().add(0, Math.sin(tickCount * 0.08) * 0.004, 0));   // a slow pulse up and down
    }

    /** Float to a spot a few blocks over the ground nearby, now and then. */
    class DriftGoal extends Goal {
        DriftGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return getNavigation().isDone() && random.nextInt(40) == 0; }
        @Override public void start() {
            int x = (int) getX() + random.nextInt(21) - 10, z = (int) getZ() + random.nextInt(21) - 10;
            int ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            getNavigation().moveTo(x + 0.5, ground + 2 + random.nextInt(5), z + 0.5, 1.0);
        }
        @Override public boolean canContinueToUse() { return !getNavigation().isDone(); }
    }
}

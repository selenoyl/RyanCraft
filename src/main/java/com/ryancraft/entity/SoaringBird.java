package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Big soaring birds (eagles, hawks, vultures, owls). They ride the air in slow circles high above the ground, then come
 * down to perch on a treetop or a hill. They swoop on prey, and take off if you walk up on them.
 */
public abstract class SoaringBird extends CritterAnimal implements FlyingAnimal {
    protected SoaringBird(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);   // we manage gravity ourselves (see aiStep)
    }

    /** Height above the ground it circles at, and the circle's radius. */
    protected abstract double soarHeight();
    protected double soarRadius() { return 14; }
    /** Whether it's the time of day to be up and about (owls only at night). */
    protected boolean active() { return true; }
    /** Whether it may go off soaring at all (a tame roc stays with its rider). */
    protected boolean canSoar() { return true; }
    /** How long a soaring spell lasts, in ticks. */
    protected int soarTime() { return 600 + this.random.nextInt(1200); }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5, true));
        this.goalSelector.addGoal(2, new SoarGoal());
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        if (perchesInTrees()) this.goalSelector.addGoal(4, new PerchInTreeGoal(this, 1.0, 120, 18));   // between soars, a big bird sits up in a tree
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        addPrey();
    }

    /** Whether it comes down to sit in trees between soars (rays and rocs keep to the open sky). */
    protected boolean perchesInTrees() { return true; }

    /** Add target goals for whatever it hunts. */
    protected void addPrey() {}

    private boolean soaring;

    @Override
    public void aiStep() {
        super.aiStep();
        if (!soaring && this.isNoGravity()) this.setNoGravity(false);    // out of a soar or a flight: let gravity (and gliding) take over
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));   // glide down, never drop
        this.fallDistance = 0;
    }

    @Override public boolean isFlying() { return !this.onGround(); }
    @Override public boolean isFlyingVisual() { return airborne(); }

    private boolean playerNear() {
        return this.level().getNearestPlayer(this, 8.0) instanceof Player p && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(p) && !p.isCrouching();
    }

    /** Circle high overhead for a while, then come down to a perch. */
    class SoarGoal extends Goal {
        private double cx, cz, angle, dir;
        private int time;

        SoarGoal() { this.setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (!canSoar() || SoaringBird.this.getTarget() != null || !active() && !playerNear()) return false;
            return playerNear() || SoaringBird.this.random.nextInt(SoaringBird.this.onGround() ? 400 : 20) == 0;
        }

        @Override public boolean canContinueToUse() { return canSoar() && time > 0 && SoaringBird.this.getTarget() == null && (active() || playerNear()); }

        @Override public void start() {
            soaring = true;
            time = soarTime();
            cx = SoaringBird.this.getX(); cz = SoaringBird.this.getZ();
            angle = SoaringBird.this.random.nextDouble() * Math.PI * 2;
            dir = SoaringBird.this.random.nextBoolean() ? 1 : -1;
            SoaringBird.this.setDeltaMovement(SoaringBird.this.getDeltaMovement().add(0, 0.4, 0));   // a few beats to get airborne
        }

        @Override public void tick() {
            time--;
            SoaringBird self = SoaringBird.this;
            double r = soarRadius();
            angle += dir * 0.35 / r;                                 // about a third of a block per tick around the circle
            cx += (self.random.nextDouble() - 0.5) * 0.05; cz += (self.random.nextDouble() - 0.5) * 0.05;
            double tx = cx + Math.cos(angle) * r, tz = cz + Math.sin(angle) * r;
            int ground = self.level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(tx), (int) Math.floor(tz));
            double ty = Math.min(ground + soarHeight() + Math.sin(angle * 0.5) * 2, self.level().getMaxY() - 4);
            // Steer straight on the velocity: glide toward the next point on the circle, climbing or sinking gently
            Vec3 to = new Vec3(tx - self.getX(), Math.max(-0.6, Math.min(0.6, (ty - self.getY()) * 0.15)), tz - self.getZ());
            Vec3 want = new Vec3(to.x, 0, to.z).normalize().scale(0.35).add(0, to.y * 0.5, 0);
            self.setNoGravity(true);
            self.setDeltaMovement(self.getDeltaMovement().lerp(want, 0.08));
            Vec3 v = self.getDeltaMovement();
            float yaw = (float) (Math.atan2(v.z, v.x) * (180 / Math.PI)) - 90f;
            self.setYRot(yaw); self.yBodyRot = yaw; self.yHeadRot = yaw;
        }

        @Override public void stop() {
            soaring = false;
            SoaringBird.this.setNoGravity(false);
            // Come down on a treetop or a high spot nearby
            BlockPos here = SoaringBird.this.blockPosition();
            int x = here.getX() + SoaringBird.this.random.nextInt(17) - 8, z = here.getZ() + SoaringBird.this.random.nextInt(17) - 8;
            int y = SoaringBird.this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            SoaringBird.this.getNavigation().moveTo(x + 0.5, y, z + 0.5, 1.0);
        }
    }
}

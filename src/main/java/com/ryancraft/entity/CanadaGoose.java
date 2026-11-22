package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Canada goose: noisy flocks that graze fields and paddle about ponds. Famously bad-tempered: get near the goslings and
 * the adults come at you hissing.
 */
public class CanadaGoose extends CritterAnimal {
    public CanadaGoose(EntityType<? extends CanadaGoose> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    private boolean guardingGoslings(LivingEntity target) {
        if (this.isBaby() || !(target instanceof Player p) || p.isCreative() || p.isSpectator()) return false;
        return this.distanceTo(target) < 6 && !this.level().getEntitiesOfClass(CanadaGoose.class, this.getBoundingBox().inflate(8), CanadaGoose::isBaby).isEmpty();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new StayWithFlockGoal<>(this, CanadaGoose.class, 1.05, 20, 5));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0, 60) {
            @Override protected Vec3 getPosition() {
                if (CanadaGoose.this.random.nextFloat() < 0.5f) {
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                    for (int i = 0; i < 16; i++) {
                        p.set(getBlockX() + random.nextInt(25) - 12, getBlockY() + random.nextInt(5) - 3, getBlockZ() + random.nextInt(25) - 12);
                        if (level().getFluidState(p).is(FluidTags.WATER) && level().getBlockState(p.above()).isAir()) return Vec3.atBottomCenterOf(p);
                    }
                }
                return super.getPosition();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CanadaGoose.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (t, l) -> guardingGoslings(t)));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && !this.isInWater() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));
        this.fallDistance = 0;
        LivingEntity t = this.getTarget();
        if (t != null && this.getLastHurtByMob() != t && !guardingGoslings(t) && this.distanceTo(t) > 8) this.setTarget(null);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(ItemTags.CHICKEN_FOOD); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.CANADA_GOOSE, l, p); }
    @Override protected SoundEvent getAmbientSound() { return this.getTarget() != null ? RyanSounds.GOOSE_HISS : RyanSounds.GOOSE_HONK; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.GOOSE_HONK; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.GOOSE_HONK; }
    @Override public int getAmbientSoundInterval() { return 300; }
    @Override public boolean isFlyingVisual() { return airborne(); }
}

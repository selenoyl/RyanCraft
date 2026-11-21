package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Moose: loners of cold wetlands and spruce forest. They don't run from you, they run AT you:
 * get close (closer still to a cow with a calf) and they charge. They wade happily.
 */
public class Moose extends BigGame {
    public Moose(EntityType<? extends Moose> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 44.0).add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.7).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override protected String maleName() { return "bull"; }
    @Override protected float maleChance() { return 0.45f; }
    @Override protected float spookDistance() { return 0.1f; }        // never spooked
    @Override protected boolean fightsBack() { return !this.isBaby(); }
    @Override protected int herdSize() { return 3; }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.isBaby() && crowding(target)));
    }

    private boolean crowding(LivingEntity target) {
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        double reach = target.isCrouching() ? 3.0 : 5.0;
        boolean calf = !this.level().getEntitiesOfClass(Moose.class, this.getBoundingBox().inflate(8), Moose::isBaby).isEmpty();
        return this.distanceTo(target) < (calf ? reach * 1.8 : reach);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        LivingEntity t = this.getTarget();
        if (t != null && !crowding(t) && this.getLastHurtByMob() != t && this.distanceTo(t) > 9) this.setTarget(null);
    }

    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.MOOSE, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.MOOSE_CALL; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.RAVAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 1.1f; }
    @Override public int getAmbientSoundInterval() { return 700; }
}

package com.ryancraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Jellyfish: drifts on the currents of warm seas, pulsing slowly up and down. Brush against one and it stings. */
public class Jellyfish extends OceanAnimal {
    private int stingCooldown;

    public Jellyfish(EntityType<? extends Jellyfish> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.15);
    }

    @Override protected double swimSpeed() { return 0.3; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.isInWater()) {                                   // a slow pulse: drift up, sink back
            double pulse = Math.sin(this.tickCount * 0.08) * 0.012;
            this.setDeltaMovement(this.getDeltaMovement().add(0, pulse, 0));
        }
        if (stingCooldown > 0) { stingCooldown--; return; }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2), e -> !(e instanceof Jellyfish))) {
            if (e.hurtServer(level, this.damageSources().mobAttack(this), 1.0f)) e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
            stingCooldown = 30;
        }
    }

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.SLIME_HURT_SMALL; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.SLIME_DEATH_SMALL; }
}

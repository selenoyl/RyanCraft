package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/** Pronghorn: the fastest thing on the plains. Spooks from far off and outruns anything; snorts the alarm to the herd. */
public class Pronghorn extends BigGame {
    public Pronghorn(EntityType<? extends Pronghorn> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override protected String maleName() { return "buck"; }
    @Override protected float maleChance() { return 0.35f; }
    @Override protected float spookDistance() { return 22f; }
    @Override protected double fleeSpeed() { return 2.3; }
    @Override protected boolean fightsBack() { return false; }
    @Override protected int herdSize() { return 7; }

    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.PRONGHORN, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.PRONGHORN_SNORT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.PRONGHORN_SNORT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GOAT_DEATH; }
    @Override public int getAmbientSoundInterval() { return 600; }
}

package com.ryancraft.entity;

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

/** Black-tailed deer of the Pacific coast forests: smaller and greyer than the whitetail, big ears, forked antlers. */
public class BlackTailedDeer extends BigGame {
    public BlackTailedDeer(EntityType<? extends BlackTailedDeer> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override protected boolean stagedAntlers() { return true; }
    @Override protected String maleName() { return "buck"; }
    @Override protected float spookDistance() { return 14f; }
    @Override protected double fleeSpeed() { return 1.55; }
    @Override protected boolean fightsBack() { return false; }
    @Override protected int herdSize() { return 4; }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.BLACK_TAILED_DEER, l, p); }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.GOAT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GOAT_DEATH; }
}

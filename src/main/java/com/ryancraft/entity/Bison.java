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

/** American bison: great herds on the plains. Slow to spook, but hurt one and the adults charge. */
public class Bison extends BigGame {
    public Bison(EntityType<? extends Bison> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8).add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override protected int variantCount() { return 1; }
    @Override protected String maleName() { return null; }
    @Override protected String variantName() { return null; }
    @Override protected float spookDistance() { return 5f; }
    @Override protected boolean fightsBack() { return !this.isBaby(); }
    @Override protected int herdSize() { return 9; }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.BISON, l, p); }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.COW_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.RAVAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.6f; }
    @Override public int getAmbientSoundInterval() { return 500; }
}

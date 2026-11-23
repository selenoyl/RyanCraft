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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Sky ray: a great pale ray that glides in wide, slow circles over the Skylands islands. Harmless. */
public class SkyRay extends SoaringBird {
    public SkyRay(EntityType<? extends SkyRay> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override protected double soarHeight() { return 18; }
    @Override protected double soarRadius() { return 26; }
    @Override protected int soarTime() { return 2400 + this.random.nextInt(2400); }    // mostly aloft
    @Override protected void addPrey() {}

    @Override public boolean isFood(ItemStack s) { return false; }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PHANTOM_FLAP; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.DOLPHIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.DOLPHIN_DEATH; }
    @Override public float getVoicePitch() { return 0.6f; }
    @Override public int getAmbientSoundInterval() { return 400; }
}

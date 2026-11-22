package com.ryancraft.entity;

import com.ryancraft.bag.Baggable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Ruby-throated hummingbird: a buzzing green jewel that zips between flowers. Catch it in a bundle for the bird cage. */
public class Hummingbird extends SmallFlyer implements Baggable {
    public Hummingbird(EntityType<? extends Hummingbird> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 2.0).add(Attributes.FLYING_SPEED, 0.7).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.BIRD; }
    @Override public boolean isFood(ItemStack s) { return s.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.BEE_LOOP; }
    @Override protected float getSoundVolume() { return 0.3f; }
    @Override public float getVoicePitch() { return 1.8f; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public int getAmbientSoundInterval() { return 200; }
}

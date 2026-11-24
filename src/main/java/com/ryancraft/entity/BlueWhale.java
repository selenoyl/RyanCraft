package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** Blue whale: the biggest animal there is. Glides slowly through the deep ocean well below the surface, sings, and every couple of minutes rises to blow a towering spout. Harmless. */
public class BlueWhale extends OceanAnimal {
    public BlueWhale(EntityType<? extends BlueWhale> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.6).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override protected int breathInterval() { return 2400; }      // about every two minutes (a little random)
    @Override protected int cruiseDepth() { return 12; }
    @Override protected float spoutSize() { return 3.0f; }
    @Override protected double swimSpeed() { return 0.7; }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.WHALE_SONG; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.DOLPHIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.DOLPHIN_DEATH; }
    @Override protected float getSoundVolume() { return 4.0f; }
    @Override public float getVoicePitch() { return 1.0f; }
    @Override public int getAmbientSoundInterval() { return 500; }
}

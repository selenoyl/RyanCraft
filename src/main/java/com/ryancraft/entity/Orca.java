package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/** Orca: travels and hunts in pods through cold seas, calling to each other; surfaces to blow. Leaves people be unless attacked. */
public class Orca extends OceanAnimal {
    public Orca(EntityType<? extends Orca> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.MOVEMENT_SPEED, 1.2).add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override protected int breathInterval() { return 900; }
    @Override protected float spoutSize() { return 1.2f; }
    @Override protected double swimSpeed() { return 1.0; }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(4, new StayWithFlockGoal<>(this, Orca.class, 1.1, 32, 8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(Orca.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractFish.class, 200, true, false, null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Squid.class, 300, true, false, null));
    }

    @Override protected SoundEvent getAmbientSound() { return RyanSounds.ORCA_CALL; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.DOLPHIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.DOLPHIN_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.9f; }
    @Override public int getAmbientSoundInterval() { return 300; }
}

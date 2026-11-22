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

/** Red-tailed hawk: circles over fields and woodland edges with that screaming call, dropping on small game. */
public class RedTailedHawk extends SoaringBird {
    public RedTailedHawk(EntityType<? extends RedTailedHawk> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override protected double soarHeight() { return 18; }
    @Override protected double soarRadius() { return 12; }



    @Override
    protected void addPrey() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.rabbit.Rabbit.class, 400, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Chipmunk.class, 400, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Squirrel.class, 400, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Songbird.class, 400, true, false, (t, l) -> !this.isBaby()));
    }

    @Override public boolean isFood(ItemStack s) { return s.is(net.minecraft.world.item.Items.RABBIT) || s.is(net.minecraft.world.item.Items.CHICKEN); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.HAWK; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 1.15f; }
    @Override public int getAmbientSoundInterval() { return 600; }
}

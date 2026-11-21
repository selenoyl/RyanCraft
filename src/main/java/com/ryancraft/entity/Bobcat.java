package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bobcat: a shy wildcat you rarely see. It slips away from people, climbs trees, stalks rabbits, chickens,
 * squirrels and chipmunks, and screams at night (a sound that'll raise the hair on your neck). Corner it and it fights.
 */
public class Bobcat extends CritterAnimal {
    public Bobcat(EntityType<? extends Bobcat> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override protected boolean climbs() { return true; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 12.0f, 1.1, 1.5,
                e -> this.getTarget() == null && this.getLastHurtByMob() != e));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Rabbit.class, 120, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Chicken.class, 200, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Squirrel.class, 200, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Chipmunk.class, 200, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turkey.class, 400, true, false, (t, l) -> !this.isBaby() && t.isBaby()));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.isBaby()) return;
        long t = level.getDayTime() % 24000L;
        if (t > 13000L && t < 23000L && this.random.nextInt(4000) == 0) this.playSound(RyanSounds.BOBCAT_SCREAM, 3.0f, 0.95f + this.random.nextFloat() * 0.1f);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.RABBIT) || s.is(Items.CHICKEN); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.BOBCAT, l, p); }
    @Override protected SoundEvent getAmbientSound() { return this.getTarget() != null ? RyanSounds.BOBCAT_GROWL : null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.BOBCAT_GROWL; }
    @Override protected SoundEvent getDeathSound() { return net.minecraft.sounds.SoundEvents.OCELOT_DEATH; }
}

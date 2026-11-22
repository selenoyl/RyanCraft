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
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Cougar: the big cat of the mountains and deep woods. Seldom seen; it keeps away from people and stalks deer,
 * sheep, goats and rabbits. Corner or hurt it and it fights hard. Climbs, and screams at night like a bobcat, only deeper.
 */
public class Cougar extends CritterAnimal {
    public Cougar(EntityType<? extends Cougar> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 28.0).add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override protected boolean climbs() { return true; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.45f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.45, true));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 16.0f, 1.1, 1.4,
                e -> this.getTarget() == null && this.getLastHurtByMob() != e));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Deer.class, 300, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, BlackTailedDeer.class, 300, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Sheep.class, 300, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Goat.class, 300, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, 200, true, false, (t, l) -> !this.isBaby()));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.isBaby()) return;
        long t = level.getDayTime() % 24000L;
        if (t > 13000L && t < 23000L && this.random.nextInt(5000) == 0) this.playSound(RyanSounds.BOBCAT_SCREAM, 4.0f, 0.7f);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.MUTTON) || s.is(com.ryancraft.RyanItems.VENISON); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.COUGAR, l, p); }
    @Override protected SoundEvent getAmbientSound() { return this.getTarget() != null ? RyanSounds.BOBCAT_GROWL : null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.BOBCAT_GROWL; }
    @Override protected SoundEvent getDeathSound() { return net.minecraft.sounds.SoundEvents.OCELOT_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.7f; }
}

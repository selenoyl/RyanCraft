package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import com.ryancraft.bag.Baggable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Northern bobwhite: coveys of plump quail in field edges that whistle their own name and burst into the air when flushed. */
public class Bobwhite extends CritterAnimal implements Baggable {
    public Bobwhite(EntityType<? extends Bobwhite> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override protected boolean skittish() { return true; }
    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.BIRD; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.8, this::isFood, true) {
            @Override public boolean canUse() { return !Bobwhite.this.isTrusting() && super.canUse(); }
        });
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 7.0f, 1.3, 1.6,
                e -> !this.isTrusting() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new StayWithFlockGoal<>(this, Bobwhite.class, 1.1, 16, 3));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Flush: a startled covey bursts up into the air
        if (!this.isTrusting() && this.onGround() && this.random.nextInt(20) == 0
                && this.level().getNearestPlayer(this, 3.0) instanceof Player p && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(p)) {
            Vec3 away = this.position().subtract(p.position()).normalize().scale(0.5);
            this.setDeltaMovement(away.x, 0.6, away.z);
            this.playSound(SoundEvents.PARROT_FLY, 1.0f, 1.2f);
        }
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));
        this.fallDistance = 0;
    }

    @Override public boolean isFlyingVisual() { return !this.onGround(); }
    @Override public boolean isFood(ItemStack s) { return s.is(ItemTags.CHICKEN_FOOD); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.BOBWHITE, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.BOBWHITE_CALL; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.CHICKEN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.CHICKEN_DEATH; }
    @Override public int getAmbientSoundInterval() { return 400; }
}

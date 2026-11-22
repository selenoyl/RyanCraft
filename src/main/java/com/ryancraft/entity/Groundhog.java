package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Groundhog (woodchuck): a chunky field-edge grazer that whistles an alarm and waddles off when you come near. */
public class Groundhog extends CritterAnimal {
    public Groundhog(EntityType<? extends Groundhog> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.24);
    }

    @Override protected boolean skittish() { return true; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.8, this::isFood, true) {
            @Override public boolean canUse() { return !Groundhog.this.isTrusting() && super.canUse(); }
        });
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.2, 1.5,
                e -> !this.isTrusting() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Coyote.class, 10.0f, 1.3, 1.6));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.CARROT) || s.is(Items.BEETROOT) || s.is(Items.APPLE); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.GROUNDHOG, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.SQUIRREL_CHATTER; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.SQUIRREL_CHATTER; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.SQUIRREL_CHATTER; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.75f; }
    @Override public int getAmbientSoundInterval() { return 400; }
}

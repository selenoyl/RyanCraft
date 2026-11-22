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

/**
 * Eastern chipmunk: a quick, striped little ground critter that darts off chirping "chip-chip-chip".
 * Shy like an ocelot: it bolts when you come near. Hold out nuts or seeds and keep still and it creeps up to you;
 * feed it and it may decide to trust you. Scoop it up with a bundle and keep it in a chipmunk cage.
 */
public class Chipmunk extends CritterAnimal implements com.ryancraft.bag.Baggable {
    public Chipmunk(EntityType<? extends Chipmunk> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.33);
    }

    @Override protected boolean skittish() { return true; }
    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.CRITTER; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Coyote.class, 10.0f, 1.1, 1.3));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Bobcat.class, 10.0f, 1.1, 1.3));
        // Bait: hold out food and stay still; move and it scampers off
        this.goalSelector.addGoal(3, new TemptGoal(this, 0.8, this::isFood, true) {
            @Override public boolean canUse() { return !Chipmunk.this.isTrusting() && super.canUse(); }
        });
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.1, 1.3,
                e -> !this.isTrusting() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack s) { return s.is(com.ryancraft.RyanItems.SHELLED_WALNUT) || s.is(Items.SWEET_BERRIES) || s.is(Items.WHEAT_SEEDS) || s.is(Items.PUMPKIN_SEEDS) || s.is(Items.MELON_SEEDS); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.CHIPMUNK, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.CHIPMUNK_CHIP; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.CHIPMUNK_CHIP; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.CHIPMUNK_CHIP; }
    @Override public int getAmbientSoundInterval() { return 200; }
}

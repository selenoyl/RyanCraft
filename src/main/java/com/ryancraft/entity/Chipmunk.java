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

/** Eastern chipmunk: a quick, striped little ground critter that darts off chirping "chip-chip-chip". */
public class Chipmunk extends CritterAnimal {
    public Chipmunk(EntityType<? extends Chipmunk> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.33);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.9));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.5, 1.9,
                e -> !e.isCrouching() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Coyote.class, 10.0f, 1.5, 1.9));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.0, this::isFood, true));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack s) { return s.is(Items.SWEET_BERRIES) || s.is(Items.WHEAT_SEEDS) || s.is(Items.PUMPKIN_SEEDS) || s.is(Items.MELON_SEEDS); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.CHIPMUNK, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.CHIPMUNK_CHIP; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.CHIPMUNK_CHIP; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.CHIPMUNK_CHIP; }
    @Override public int getAmbientSoundInterval() { return 200; }
}

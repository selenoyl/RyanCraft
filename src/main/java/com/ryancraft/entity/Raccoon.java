package com.ryancraft.entity;

import com.ryancraft.RyanItems;
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

/** Masked bandit. Climbs trees, keeps its distance, and steals any food you leave lying around. */
public class Raccoon extends CritterAnimal {
    public Raccoon(EntityType<? extends Raccoon> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override protected boolean climbs() { return true; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, true));
        this.goalSelector.addGoal(4, new EatDroppedFoodGoal(this, 1.2, this::isFood));
        this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.2, 1.5,
                e -> !e.isCrouching() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack s) {
        return s.is(Items.SWEET_BERRIES) || s.is(Items.EGG) || s.is(Items.COD) || s.is(Items.SALMON) || s.is(Items.APPLE)
                || s.is(Items.BREAD) || s.is(RyanItems.FISH_FOOD);
    }

    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.RACCOON, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.RACCOON_CHITTER; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.RACCOON_CHITTER; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.RACCOON_CHITTER; }
}

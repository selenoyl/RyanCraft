package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Busy creek engineer. Swims well, slaps its tail in alarm, and leaves gnawed sticks lying about. */
public class Beaver extends CritterAnimal {
    private int stickTime;

    public Beaver(EntityType<? extends Beaver> type, Level level) {
        super(type, level);
        stickTime = this.random.nextInt(6000) + 4000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && this.isAlive() && !this.isBaby() && --stickTime <= 0) {
            this.spawnAtLocation(level, Items.STICK);
            stickTime = this.random.nextInt(6000) + 4000;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInWater()) this.playSound(RyanSounds.BEAVER_SLAP, 1.2f, 1.0f);
        return super.hurtServer(level, source, amount);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(ItemTags.SAPLINGS) || s.is(Items.APPLE); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.BEAVER, l, p); }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.MINK_SQUEAK; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.MINK_SQUEAK; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.7f; }
}

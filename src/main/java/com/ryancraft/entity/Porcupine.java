package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Porcupine: slow and unbothered, it doesn't run. Hit one bare-handed (or bite it) and you get a face full of quills. */
public class Porcupine extends CritterAnimal {
    public Porcupine(EntityType<? extends Porcupine> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.16);
    }

    @Override protected boolean climbs() { return true; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /** Quills: whatever hits it up close gets stuck too. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) < 9.0) {
            attacker.hurtServer(level, this.damageSources().thorns(this), 2.0f);
            this.playSound(SoundEvents.THORNS_HIT, 1.0f, 1.2f);
        }
        return hurt;
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.APPLE) || s.is(Items.SWEET_BERRIES); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.PORCUPINE, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.RACCOON_CHITTER; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.RACCOON_CHITTER; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.RACCOON_CHITTER; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.7f; }
}

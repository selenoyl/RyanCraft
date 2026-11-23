package com.ryancraft.entity;

import com.ryancraft.moon.MoonBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Crater crab: a dust-grey crab that lies buried in the moon dust (unseen, still) until you come within a few blocks,
 * then bursts out and pinches. After a while left alone it digs back in.
 */
public class CraterCrab extends CritterAnimal {
    private int restless;

    public CraterCrab(EntityType<? extends CraterCrab> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 4.0).add(Attributes.FOLLOW_RANGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, false));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override public boolean canUse() { return !isInvisible() && super.canUse(); }
        });
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (p, l) -> p.distanceToSqr(this) < 16));
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        boolean onDust = level.getBlockState(blockPosition().below()).is(MoonBlocks.MOON_DUST);
        if (getTarget() != null) {                                              // out of the dust to pinch
            if (isInvisible()) { setInvisible(false); playSound(SoundEvents.SAND_BREAK, 1f, 0.8f); }
            restless = 200;
        } else if (!isInvisible() && onDust && --restless <= 0 && getNavigation().isDone()) {
            setInvisible(true); playSound(SoundEvents.SAND_PLACE, 0.8f, 0.9f);            // and back in
        }
        if (isInvisible()) { getNavigation().stop(); setDeltaMovement(0, getDeltaMovement().y, 0); }
    }

    @Override public boolean isFood(ItemStack s) { return false; }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.TURTLE_HURT_BABY; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.TURTLE_DEATH_BABY; }
}

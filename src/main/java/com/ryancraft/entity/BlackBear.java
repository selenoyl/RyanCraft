package com.ryancraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.core.BlockPos;

/**
 * American black bear. Minds its own business, raids dropped berries and fish,
 * but fights back hard, and mothers charge anyone who gets close to their cubs.
 */
public class BlackBear extends CritterAnimal {
    public BlackBear(EntityType<? extends BlackBear> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override protected int variantCount() { return 2; }   // 0 black, 1 cinnamon

    @Override
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        return this.random.nextInt(7) == 0 ? 1 : 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, this::isFood, false));
        this.goalSelector.addGoal(4, new EatDroppedFoodGoal(this, 1.1, this::isFood));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.15));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Protective mothers
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 20, true, true,
                (target, level) -> !this.isBaby() && !level.getEntitiesOfClass(BlackBear.class,
                        target.getBoundingBox().inflate(6), BlackBear::isBaby).isEmpty()));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.playSound(SoundEvents.POLAR_BEAR_WARNING, 1.0f, 0.8f);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean isFood(ItemStack s) {
        return s.is(Items.SWEET_BERRIES) || s.is(Items.HONEYCOMB) || s.is(Items.HONEY_BOTTLE) || s.is(Items.SALMON) || s.is(Items.COD)
                || s.is(com.ryancraft.RyanItems.FISH_FOOD);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return offspring(RyanEntities.BLACK_BEAR, level, partner); }

    @Override protected SoundEvent getAmbientSound() { return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : SoundEvents.POLAR_BEAR_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.POLAR_BEAR_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.POLAR_BEAR_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.85f; }
    @Override protected String variantName() { return getVariant() == 1 ? "cinnamon" : null; }
}

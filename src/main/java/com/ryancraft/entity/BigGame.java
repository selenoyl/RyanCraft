package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Herd grazers (elk, moose, pronghorn). Variant 0 is a female, 1 a male with antlers or horns.
 * They keep their distance from people, though sneaking lets you get much closer (it's hunting, after all).
 * Some fight back when hurt instead of running.
 */
public abstract class BigGame extends CritterAnimal {
    protected BigGame(EntityType<? extends Animal> type, Level level) { super(type, level); }

    @Override protected int variantCount() { return 2; }
    protected float maleChance() { return 0.4f; }
    public boolean isMale() { return getVariant() == 1; }
    /** "bull", "buck"... */
    protected abstract String maleName();
    @Override protected String variantName() { return isMale() && !this.isBaby() ? maleName() : null; }

    @Override
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) { return this.random.nextFloat() < maleChance() ? 1 : 0; }

    /** How far away a walking (not sneaking) player spooks it. */
    protected abstract float spookDistance();
    protected double fleeSpeed() { return 1.6; }
    /** Turns and fights when hurt (otherwise it bolts). */
    protected abstract boolean fightsBack();
    protected int herdSize() { return 6; }

    private boolean spookedBy(LivingEntity e) {
        if (this.getTarget() != null) return false;
        return !e.isCrouching() || this.distanceTo(e) < spookDistance() * 0.4f;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, fleeSpeed()) {
            @Override public boolean canUse() { return !BigGame.this.fightsBack() && super.canUse(); }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true) {
            @Override public boolean canUse() { return BigGame.this.fightsBack() && super.canUse(); }
        });
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, spookDistance(), 1.1, fleeSpeed(), this::spookedBy));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.1, this::isFood, false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.15));
        this.goalSelector.addGoal(7, new StayWithFlockGoal<>(this, herdClass(), 1.05, 24, herdSize()));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override public boolean canUse() { return BigGame.this.fightsBack() && super.canUse(); }
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends BigGame> Class<T> herdClass() { return (Class<T>) this.getClass(); }

    @Override
    public void aiStep() {
        super.aiStep();
        // Lose interest once the fight is over and the attacker has backed off
        LivingEntity t = this.getTarget();
        if (t != null && this.level() instanceof ServerLevel && this.distanceTo(t) > 16) this.setTarget(null);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.WHEAT) || s.is(Items.HAY_BLOCK); }
}

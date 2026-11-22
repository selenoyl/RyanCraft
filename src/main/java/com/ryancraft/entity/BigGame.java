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
    // ---- antlers (bulls and bucks of species with staged racks): grow like the whitetail's
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ANTLERS =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BigGame.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> RACK =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BigGame.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ANTLER_COLOR =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BigGame.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    public static final int MAX_ANTLERS = 4;
    private static final int DAY = 24000;
    private int antlerAge, nextStage = -1;

    protected BigGame(EntityType<? extends Animal> type, Level level) { super(type, level); }

    /** Whether this species' males carry staged antlers (spikes -> forks -> full rack, rare non-typicals). */
    protected boolean stagedAntlers() { return false; }
    public int getAntlers() { return this.entityData.get(ANTLERS); }
    public int getRack() { return this.entityData.get(RACK); }
    public int getAntlerColor() { return this.entityData.get(ANTLER_COLOR); }
    public void setAntlers(int stage) { this.entityData.set(ANTLERS, Math.max(0, Math.min(MAX_ANTLERS, stage))); }

    @Override public int antlerStage() { return stagedAntlers() && isMale() ? getAntlers() : -1; }
    @Override public int antlerRack() { return getRack(); }
    /** Cows/does use texture 0; bulls/bucks 1 + antler colour (normal, dark, chocolate). */
    @Override public int textureIndex() { return stagedAntlers() ? (isMale() ? 1 + getAntlerColor() : 0) : getVariant(); }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANTLERS, 0);
        builder.define(RACK, 0);
        builder.define(ANTLER_COLOR, 0);
    }

    private int stageLength() { return 14 * DAY + this.random.nextInt(7 * DAY); }

    /** On a wild spawn: most bulls are young; a few carry trophies; one in a few dozen trophies is non-typical. */
    protected void rollAntlers() {
        int roll = this.random.nextInt(100);
        int stage = roll < 30 ? 1 : roll < 60 ? 2 : roll < 86 ? 3 : 4;
        setAntlers(stage);
        if (stage == MAX_ANTLERS && this.random.nextInt(30) == 0) this.entityData.set(RACK, 1);
        int c = this.random.nextInt(100);
        this.entityData.set(ANTLER_COLOR, c < 72 ? 0 : c < 92 ? 1 : 2);
        nextStage = stageLength();
        antlerAge = this.random.nextInt(nextStage);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d,
                                                                   net.minecraft.world.entity.EntitySpawnReason reason, net.minecraft.world.entity.SpawnGroupData data) {
        var result = super.finalizeSpawn(level, d, reason, data);
        if (stagedAntlers()) rollAntlers();
        return result;
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) {
        super.addAdditionalSaveData(out);
        if (!stagedAntlers()) return;
        out.putInt("Antlers", getAntlers()); out.putInt("Rack", getRack()); out.putInt("AntlerColor", getAntlerColor());
        out.putInt("AntlerAge", antlerAge); out.putInt("NextAntlerStage", nextStage);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) {
        super.readAdditionalSaveData(in);
        if (!stagedAntlers()) return;
        setAntlers(in.getIntOr("Antlers", 1));
        this.entityData.set(RACK, in.getIntOr("Rack", 0));
        this.entityData.set(ANTLER_COLOR, in.getIntOr("AntlerColor", 0));
        antlerAge = in.getIntOr("AntlerAge", 0);
        nextStage = in.getIntOr("NextAntlerStage", -1);
    }

    /** Bulls named by their rack: "Trophy Bull Elk", "Non-typical Bull Elk". */
    @Override
    protected net.minecraft.network.chat.Component getTypeName() {
        if (stagedAntlers() && isMale() && !this.isBaby() && getAntlers() == MAX_ANTLERS)
            return net.minecraft.network.chat.Component.translatable(this.getType().getDescriptionId() + (getRack() > 0 ? ".nontypical" : ".trophy"));
        return super.getTypeName();
    }

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
        // Antlers grow while the animal is loaded: a stage every 2-3 in-game weeks
        if (stagedAntlers() && !this.level().isClientSide() && isMale() && !this.isBaby() && getAntlers() < MAX_ANTLERS) {
            if (nextStage < 0) nextStage = stageLength();
            if (++antlerAge >= nextStage) {
                setAntlers(getAntlers() + 1);
                if (getAntlers() == MAX_ANTLERS && this.random.nextInt(30) == 0) this.entityData.set(RACK, 1);
                antlerAge = 0; nextStage = stageLength();
            }
        }
        // Lose interest once the fight is over and the attacker has backed off
        LivingEntity t = this.getTarget();
        if (t != null && this.level() instanceof ServerLevel && this.distanceTo(t) > 16) this.setTarget(null);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.WHEAT) || s.is(Items.HAY_BLOCK); }
}

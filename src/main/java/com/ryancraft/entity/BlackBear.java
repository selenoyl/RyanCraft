package com.ryancraft.entity;

import com.ryancraft.RyanItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * American black bear.
 * <ul>
 *   <li>Peaceful most of the time. Get within a few blocks (or near her cubs) and she fights.</li>
 *   <li>Beaten down to half health, she breaks off and runs, keeping clear of players until she has healed.</li>
 *   <li>Mothers roam the woods with one or two cubs, and cubs stay cubs for four in-game days.</li>
 *   <li>Grown bears go their own way; now and then (every week or two, only where bears are scarce)
 *       a sow has a new litter.</li>
 * </ul>
 */
public class BlackBear extends CritterAnimal {
    /** Cubs stay cubs for 4 in-game days (vanilla babies grow up in 1). */
    public static final int CUB_AGE = -96000;
    private static final int DAY = 24000;
    private static final int MAX_BEARS_NEARBY = 4;

    private boolean fleeing;
    private int litterCooldown = -1;
    private int healTimer;

    public BlackBear(EntityType<? extends BlackBear> type, Level level) { super(type, level); }

    /** How close you can get before she fights (the grizzly is far less patient). */
    protected double crowdReach(LivingEntity target) { return target.isCrouching() ? 2.5 : 4.0; }
    /** Health fraction at which she breaks off and runs. */
    protected float fleeAt() { return 0.5f; }
    @SuppressWarnings("unchecked")
    protected EntityType<? extends BlackBear> cubType() { return (EntityType<? extends BlackBear>) this.getType(); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.FOLLOW_RANGE, 14.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override protected int variantCount() { return 2; }   // 0 black, 1 cinnamon
    @Override protected String variantName() { return getVariant() == 1 ? "cinnamon" : null; }

    @Override
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        return this.random.nextInt(7) == 0 ? 1 : 0;
    }

    public boolean isFleeing() { return fleeing; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 16.0f, 1.2, 1.5, e -> this.fleeing));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true) {
            @Override public boolean canUse() { return !BlackBear.this.fleeing && super.canUse(); }
            @Override public boolean canContinueToUse() { return !BlackBear.this.fleeing && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, this::isFood, false));
        this.goalSelector.addGoal(5, new EatDroppedFoodGoal(this, 1.1, this::isFood));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.15));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override public boolean canUse() { return !BlackBear.this.fleeing && !BlackBear.this.isBaby() && super.canUse(); }
        });
        // Too close for comfort, or too close to her cubs
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.fleeing && !this.isBaby() && crowding(target)));
    }

    private boolean crowding(LivingEntity target) {
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        double reach = crowdReach(target);
        if (this.distanceTo(target) < reach) return true;
        return !this.level().getEntitiesOfClass(BlackBear.class, target.getBoundingBox().inflate(8), BlackBear::isBaby).isEmpty()
                && this.distanceTo(target) < 12;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;

        // Fight until half health, then run; stop running once mostly healed
        if (!fleeing && !this.isBaby() && this.getHealth() <= this.getMaxHealth() * fleeAt()) {
            fleeing = true;
            this.setTarget(null);
            this.playSound(SoundEvents.POLAR_BEAR_HURT, 1.0f, 0.7f);
        } else if (fleeing && this.getHealth() >= this.getMaxHealth() * 0.75f) {
            fleeing = false;
        }
        if (fleeing && this.getTarget() != null) this.setTarget(null);

        // Lose interest once the intruder backs off
        LivingEntity target = this.getTarget();
        if (target != null && !crowding(target) && this.getLastHurtByMob() != target && this.distanceTo(target) > 10) {
            this.setTarget(null);
        }

        // Slow natural healing when calm
        if (this.getTarget() == null && this.getHealth() < this.getMaxHealth() && ++healTimer >= 100) {
            healTimer = 0;
            this.heal(1.0f);
        }

        // Rare wild litters, only where bears are scarce
        if (!this.isBaby() && !fleeing && this.tickCount % 20 == 0) {
            if (litterCooldown < 0) litterCooldown = 5 * DAY + this.random.nextInt(5 * DAY);
            litterCooldown -= 20;
            if (litterCooldown <= 0) {
                litterCooldown = tryWildLitter(level) ? 7 * DAY + this.random.nextInt(7 * DAY) : DAY;
            }
        }
    }

    private boolean tryWildLitter(ServerLevel level) {
        int bears = level.getEntitiesOfClass(this.getClass(), this.getBoundingBox().inflate(96)).size();
        boolean hasCubs = !level.getEntitiesOfClass(BlackBear.class, this.getBoundingBox().inflate(16), BlackBear::isBaby).isEmpty();
        if (bears >= MAX_BEARS_NEARBY || hasCubs || this.random.nextInt(2) != 0) return false;
        spawnCubs(level, 1 + this.random.nextInt(2));
        return true;
    }

    private void spawnCubs(ServerLevelAccessor level, int count) {
        for (int i = 0; i < count; i++) {
            BlackBear cub = cubType().create(level.getLevel(), EntitySpawnReason.BREEDING);
            if (cub == null) continue;
            cub.setVariant(this.random.nextInt(4) == 0 ? (getVariant() ^ 1) : getVariant());
            cub.setAge(CUB_AGE);
            cub.snapTo(this.getX() + this.random.nextDouble() - 0.5, this.getY(), this.getZ() + this.random.nextDouble() - 0.5,
                    this.random.nextFloat() * 360f, 0f);
            level.addFreshEntity(cub);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        // Wild sows usually have cubs in tow
        if ((reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) && !this.isBaby()
                && this.random.nextFloat() < 0.65f) {
            spawnCubs(level, 1 + this.random.nextInt(2));
        }
        return result;
    }

    /** Cubs from breeding or litters also take the long road to adulthood. */
    @Override
    public void setBaby(boolean baby) {
        this.setAge(baby ? CUB_AGE : 0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.playSound(SoundEvents.POLAR_BEAR_WARNING, 1.0f, 0.8f);
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("LitterCooldown", litterCooldown);
        out.putBoolean("Fleeing", fleeing);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        litterCooldown = in.getIntOr("LitterCooldown", -1);
        fleeing = in.getBooleanOr("Fleeing", false);
    }

    @Override
    public boolean isFood(ItemStack s) {
        return s.is(Items.SWEET_BERRIES) || s.is(Items.HONEYCOMB) || s.is(Items.HONEY_BOTTLE) || s.is(Items.SALMON) || s.is(Items.COD)
                || s.is(RyanItems.FISH_FOOD);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return offspring(cubType(), level, partner); }

    @Override protected SoundEvent getAmbientSound() { return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : SoundEvents.POLAR_BEAR_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.POLAR_BEAR_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.POLAR_BEAR_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * (this.isBaby() ? 1.0f : 0.85f); }
}

package com.ryancraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Skittish and fast. Runs from anyone who walks up to it; sneak to get close.
 * Drops venison. Tempted and bred with wheat or apples.
 */
public class Deer extends RyanAnimal {
    /** 0 none (does, young), 1 spikes, 2 forks, 3 six-to-eight point, 4 ten-point trophy. */
    public static final int MAX_ANTLERS = 4;
    private static final int DAY = 24000;
    /** A stage takes 2-3 in-game weeks of the deer being loaded. */
    private static final int MIN_STAGE_TICKS = 14 * DAY, MAX_STAGE_TICKS = 21 * DAY;
    private static final EntityDataAccessor<Integer> ANTLERS = SynchedEntityData.defineId(Deer.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DROP_TINE = SynchedEntityData.defineId(Deer.class, EntityDataSerializers.BOOLEAN);
    /** 0 typical, 1 cactus, 2 palmated, 3 lopsided (the last three are super rare, stage 4 only). */
    private static final EntityDataAccessor<Integer> RACK = SynchedEntityData.defineId(Deer.class, EntityDataSerializers.INT);
    /** 0 normal, 1 dark, 2 chocolate. */
    private static final EntityDataAccessor<Integer> ANTLER_COLOR = SynchedEntityData.defineId(Deer.class, EntityDataSerializers.INT);
    public static final String[] RACK_NAMES = {"typical", "cactus", "palmated", "lopsided"};

    private int antlerAge;
    private int nextStage = -1;

    public Deer(EntityType<? extends Deer> type, Level level) {
        super(type, level);
    }

    private Player spookedBy;
    private int spookedTicks;

    /** How far away this deer notices a player. */
    public double noticeDistance(Player p) {
        if (p.isCreative() || p.isSpectator()) return 0;
        double d = p.isCrouching() ? 5.0 : p.isSprinting() ? 24.0 : 14.0;
        d *= 1.0 - 0.15 * Camouflage.matchingPieces(p);          // full matching set: 40%
        long t = this.level().getDayTime() % 24000L;
        if (t > 13000L && t < 23000L) d *= 0.75;                   // harder to spot you at night
        if (this.level().isRaining()) d *= 0.85;                   // rain covers your noise and scent
        return d;
    }

    private boolean noticed(net.minecraft.world.entity.LivingEntity e) {
        if (!(e instanceof Player p)) return false;
        if (spookedTicks > 0 && p == spookedBy) return true;      // the herd took flight
        double dist = this.distanceTo(p);
        boolean seen = dist < noticeDistance(p) && (dist < 6 || this.hasLineOfSight(p));
        if (seen) alarmHerd(p);
        return seen;
    }

    /** One deer flags, they all run. */
    private void alarmHerd(Player p) {
        for (Deer other : this.level().getEntitiesOfClass(Deer.class, this.getBoundingBox().inflate(16), o -> o != this)) {
            other.spookedBy = p;
            other.spookedTicks = 100;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (spookedTicks > 0 && --spookedTicks == 0) spookedBy = null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANTLERS, 0);
        builder.define(DROP_TINE, false);
        builder.define(RACK, 0);
        builder.define(ANTLER_COLOR, 0);
    }

    public int getAntlers() { return this.entityData.get(ANTLERS); }
    public boolean hasDropTine() { return this.entityData.get(DROP_TINE); }
    public int getRack() { return this.entityData.get(RACK); }
    public int getAntlerColor() { return this.entityData.get(ANTLER_COLOR); }
    public void setRack(int rack) { this.entityData.set(RACK, Math.max(0, Math.min(3, rack))); }
    public void setAntlerColor(int c) { this.entityData.set(ANTLER_COLOR, Math.max(0, Math.min(2, c))); }

    /** Antler colour for a new deer: mostly normal, some dark, a few chocolate. */
    private int rollAntlerColor() {
        int r = this.random.nextInt(100);
        return r < 72 ? 0 : r < 94 ? 1 : 2;
    }

    /** Super-rare racks, rolled when a buck reaches (or spawns at) stage 4. */
    private int rollRareRack() {
        int r = this.random.nextInt(1000);
        return r < 20 ? 1 : r < 36 ? 2 : r < 60 ? 3 : 0;     // cactus 2%, palmated 1.6%, lopsided 2.4% of trophies
    }

    public void setAntlers(int stage, boolean dropTine) {
        stage = Math.max(0, Math.min(MAX_ANTLERS, stage));
        this.entityData.set(ANTLERS, stage);
        this.entityData.set(DROP_TINE, dropTine && stage >= 3);
        // Bigger racks come on bigger bodies
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) scale.setBaseValue(0.9 + 0.04 * stage);
    }

    private int stageLength() {
        return MIN_STAGE_TICKS + this.random.nextInt(MAX_STAGE_TICKS - MIN_STAGE_TICKS);
    }

    /** Antlers grow while the deer is loaded: one stage every 2-3 in-game weeks. */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || this.isBaby() || getAntlers() >= MAX_ANTLERS) return;
        if (nextStage < 0) nextStage = stageLength();
        if (++antlerAge >= nextStage) {
            int stage = getAntlers() + 1;
            // A few bucks turn non-typical as their rack fills out
            boolean drop = hasDropTine() || (stage >= 3 && this.random.nextInt(25) == 0);
            setAntlers(stage, drop);
            if (stage == MAX_ANTLERS && getRack() == 0) setRack(rollRareRack());
            antlerAge = 0;
            nextStage = stageLength();
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        var result = super.finalizeSpawn(level, difficulty, reason, data);
        if (!this.isBaby() && reason != EntitySpawnReason.BREEDING) {
            // Wild deer are mostly young; big bucks take some hunting (about 1 in 25 is a trophy)
            int roll = this.random.nextInt(100);
            int stage = roll < 38 ? 0 : roll < 66 ? 1 : roll < 86 ? 2 : roll < 96 ? 3 : 4;
            setAntlers(stage, stage >= 3 && this.random.nextInt(33) == 0);
            if (stage == MAX_ANTLERS) setRack(rollRareRack());
            nextStage = stageLength();
            antlerAge = this.random.nextInt(nextStage);   // stagger growth across the herd
        } else {
            setAntlers(0, false);
        }
        setAntlerColor(rollAntlerColor());
        return result;
    }

    /** Hunted bucks leave their rack: a skull mount that matches it exactly. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        boolean byPlayer = source.getEntity() instanceof Player || this.getLastHurtByPlayer() != null;
        if (byPlayer && !this.isBaby() && getAntlers() >= 1) {
            this.spawnAtLocation(level, com.ryancraft.item.DeerSkullMountItem.create(getAntlers(), hasDropTine(), getRack(), getAntlerColor()));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Antlers", getAntlers());
        out.putBoolean("DropTine", hasDropTine());
        out.putInt("AntlerAge", antlerAge);
        out.putInt("NextAntlerStage", nextStage);
        out.putInt("Rack", getRack());
        out.putInt("AntlerColor", getAntlerColor());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setAntlers(in.getIntOr("Antlers", 0), in.getBooleanOr("DropTine", false));
        antlerAge = in.getIntOr("AntlerAge", 0);
        nextStage = in.getIntOr("NextAntlerStage", -1);
        setRack(in.getIntOr("Rack", 0));
        setAntlerColor(in.getIntOr("AntlerColor", 0));
    }

    @Override
    protected net.minecraft.network.chat.Component getTypeName() {
        if (getRack() > 0 && getAntlers() == MAX_ANTLERS)
            return net.minecraft.network.chat.Component.translatable("entity.ryancraft.deer." + RACK_NAMES[getRack()]);
        if (hasDropTine()) return net.minecraft.network.chat.Component.translatable("entity.ryancraft.deer.drop_tine");
        if (getAntlers() == MAX_ANTLERS) return net.minecraft.network.chat.Component.translatable("entity.ryancraft.deer.trophy");
        return super.getTypeName();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8));
        // Skittish: bolts the moment it notices you. Sneak, come at night, wear biome camo.
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 26.0f, 1.5, 1.9, this::noticed));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, this::isFood, true));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.APPLE);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Deer fawn = RyanEntities.DEER.create(level, EntitySpawnReason.BREEDING);
        if (fawn != null) fawn.setAntlerColor(this.random.nextInt(100) < 72 ? 0 : this.random.nextInt(100) < 80 ? 1 : 2);
        return fawn;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // deer are quiet
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GOAT_DEATH;
    }
}

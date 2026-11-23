package com.ryancraft.entity;

import com.ryancraft.RyanCraft;
import com.ryancraft.bag.Baggable;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Mudlings: little biome soldiers made of their biome's own stuff. Wild ones live at their camps and scatter from
 * players; scoop them up in a bundle (4 to a bundle) and set them down elsewhere. Dye a mudling to put it on that
 * colour's team: mudlings of different teams fight on their own. The loser crumbles into its biome's materials; the
 * winner gets a victory, and with it a little more health and bite (up to five).
 */
public class Mudling extends PathfinderMob implements Baggable {
    /** The biome kinds, in texture order. health, damage, speed. */
    public enum Kind {
        SOD("sod", 8, 2.0, 0.28), TWIG("twig", 6, 1.5, 0.34), BARK("bark", 12, 2.0, 0.24), MUD("mud", 9, 1.5, 0.22),
        SANDSTONE("sandstone", 11, 2.0, 0.24), TERRACOTTA("terracotta", 10, 3.0, 0.25), SNOWPACK("snowpack", 6, 1.0, 0.30),
        VINE("vine", 7, 2.0, 0.32), GRAVEL("gravel", 9, 2.0, 0.26), SHELL("shell", 10, 1.5, 0.24), PEBBLE("pebble", 10, 2.0, 0.25),
        CINDER("cinder", 7, 2.0, 0.30);
        public final String id; public final double health, damage, speed;
        Kind(String id, double health, double damage, double speed) { this.id = id; this.health = health; this.damage = damage; this.speed = speed; }
        public static Kind of(int i) { return values()[Math.floorMod(i, values().length)]; }
        /** Type matchups: an advantaged mudling hits half again as hard. */
        public boolean beats(Kind o) {
            return switch (this) {
                case MUD -> o == SANDSTONE || o == CINDER;
                case SANDSTONE -> o == SNOWPACK || o == VINE;
                case SNOWPACK -> o == TWIG || o == CINDER;
                case TWIG -> o == MUD || o == SHELL;
                case CINDER -> o == TWIG || o == BARK || o == VINE;
                case SHELL -> o == CINDER || o == PEBBLE;
                case BARK -> o == GRAVEL || o == SOD;
                case GRAVEL -> o == SANDSTONE || o == TERRACOTTA;
                case TERRACOTTA -> o == SHELL || o == SOD;
                case PEBBLE -> o == GRAVEL || o == SNOWPACK;
                case VINE -> o == MUD || o == PEBBLE;
                case SOD -> o == MUD || o == TWIG;
            };
        }
    }
    /** Rare colourings (about 1 in 20): 0 plain, 1 gilded, 2 mossy, 3 charred. */
    public static final String[] VARIANTS = {"", "gilded", "mossy", "charred"};

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TEAM = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WINS = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);

    public Mudling(EntityType<? extends Mudling> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(KIND, 0); b.define(VARIANT, 0); b.define(TEAM, -1); b.define(WINS, 0);
    }

    public Kind kind() { return Kind.of(entityData.get(KIND)); }
    public int variant() { return entityData.get(VARIANT); }
    /** The team colour (a DyeColor id), or -1 for a wild, teamless mudling. */
    public int team() { return entityData.get(TEAM); }
    public int wins() { return entityData.get(WINS); }

    public void setKind(Kind k) { entityData.set(KIND, k.ordinal()); applyStats(); }
    public void setVariant(int v) { entityData.set(VARIANT, v); }

    private void applyStats() {
        Kind k = kind(); int w = Math.min(5, wins());
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(k.health + 2 * w);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(k.damage + 0.5 * w);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(k.speed);
    }

    // ------------------------------------------------------------------ behaviour
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
            @Override public boolean canUse() { return kind() != Kind.SNOWPACK && super.canUse(); }
        });
        goalSelector.addGoal(1, new SnowballGoal());
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.0, 1.25, e -> team() < 0));   // wild ones scatter
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mudling.class, 10, true, false,
                (t, level) -> t instanceof Mudling m && isEnemy(m)));
    }

    public boolean isEnemy(Mudling m) { return team() >= 0 && m.team() >= 0 && m.team() != team() && m.isAlive(); }

    /** The snowpack scout throws snowballs from a few blocks off instead of closing in. */
    private class SnowballGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int cooldown;
        SnowballGoal() { setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return kind() == Kind.SNOWPACK && getTarget() != null && getTarget().isAlive(); }
        @Override public void tick() {
            LivingEntity t = getTarget(); if (t == null) return;
            getLookControl().setLookAt(t, 30, 30);
            double d = distanceToSqr(t);
            if (d > 36) getNavigation().moveTo(t, 1.1); else getNavigation().stop();
            if (--cooldown <= 0 && d < 100 && hasLineOfSight(t)) {
                cooldown = 30;
                Snowball s = new Snowball(level(), Mudling.this, new ItemStack(net.minecraft.world.item.Items.SNOWBALL));
                double dx = t.getX() - getX(), dy = t.getY(0.4) - s.getY(), dz = t.getZ() - getZ();
                s.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.15, dz, 1.0f, 4f);
                level().addFreshEntity(s);
                playSound(SoundEvents.SNOWBALL_THROW, 0.5f, 1.6f);
                if (level() instanceof ServerLevel sl) t.hurtServer(sl, damageSources().thrown(s, Mudling.this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * matchup(t));
            }
        }
    }

    private float matchup(Entity target) { return target instanceof Mudling m && kind().beats(m.kind()) ? 1.5f : 1.0f; }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        float dmg = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * matchup(target);
        boolean hit = target.hurtServer(level, damageSources().mobAttack(this), dmg);
        if (hit && target instanceof LivingEntity le) {
            switch (kind()) {
                case MUD -> le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);       // slows enemies
                case GRAVEL -> le.knockback(0.8, getX() - le.getX(), getZ() - le.getZ());                // knocks foes back
                case CINDER -> le.igniteForSeconds(3);                                                  // sets things alight
                default -> {}
            }
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // shield wall and shells: the sandstone legionary and shell guard shrug off a third of every blow
        if (kind() == Kind.SANDSTONE || kind() == Kind.SHELL) amount *= 0.67f;
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (source.getEntity() instanceof Mudling winner && winner.isAlive() && level() instanceof ServerLevel) winner.addWin();
        super.die(source);
    }

    private void addWin() {
        entityData.set(WINS, wins() + 1); applyStats(); setHealth(getMaxHealth());
        playSound(SoundEvents.PLAYER_LEVELUP, 0.4f, 1.8f);
    }

    /** Losers crumble into their biome's materials. */
    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKill) {
        net.minecraft.world.item.Item[] mats = switch (kind()) {
            case SOD -> new net.minecraft.world.item.Item[]{Items.DIRT, Items.WHEAT_SEEDS, Items.DANDELION};
            case TWIG -> new net.minecraft.world.item.Item[]{Items.STICK, Items.OAK_SAPLING, Items.OAK_LEAVES};
            case BARK -> new net.minecraft.world.item.Item[]{Items.SPRUCE_LOG, Items.SWEET_BERRIES, Items.STICK};
            case MUD -> new net.minecraft.world.item.Item[]{Items.MUD, Items.CLAY_BALL, Items.LILY_PAD};
            case SANDSTONE -> new net.minecraft.world.item.Item[]{Items.SAND, Items.SANDSTONE, Items.DEAD_BUSH};
            case TERRACOTTA -> new net.minecraft.world.item.Item[]{Items.TERRACOTTA, Items.ORANGE_TERRACOTTA, Items.RED_TERRACOTTA};
            case SNOWPACK -> new net.minecraft.world.item.Item[]{Items.SNOWBALL, Items.ICE, Items.PACKED_ICE};
            case VINE -> new net.minecraft.world.item.Item[]{Items.VINE, Items.COCOA_BEANS, Items.BAMBOO};
            case GRAVEL -> new net.minecraft.world.item.Item[]{Items.GRAVEL, Items.FLINT, Items.COBBLESTONE};
            case SHELL -> new net.minecraft.world.item.Item[]{Items.SAND, Items.NAUTILUS_SHELL, Items.KELP};
            case PEBBLE -> new net.minecraft.world.item.Item[]{Items.COBBLESTONE, Items.COAL, Items.IRON_NUGGET};
            case CINDER -> new net.minecraft.world.item.Item[]{Items.NETHERRACK, Items.QUARTZ, Items.BLAZE_POWDER};
        };
        spawnAtLocation(level, new ItemStack(mats[0], 1 + random.nextInt(2)));
        spawnAtLocation(level, new ItemStack(mats[1]));
        if (random.nextInt(kind() == Kind.SHELL || kind() == Kind.CINDER || kind() == Kind.PEBBLE ? 4 : 2) == 0) spawnAtLocation(level, new ItemStack(mats[2]));
    }

    // ------------------------------------------------------------------ dye a team
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DyeItem dye) {
            DyeColor c = dye.getDyeColor();
            if (team() == c.getId()) return InteractionResult.PASS;
            if (!level().isClientSide()) {
                entityData.set(TEAM, c.getId()); setPersistenceRequired();
                stack.consume(1, player);
                level().playSound(null, this, SoundEvents.DYE_USE, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // ------------------------------------------------------------------ spawning, saving
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        if (reason != EntitySpawnReason.BUCKET && reason != EntitySpawnReason.LOAD) {
            setKind(MudlingKinds.forBiome(level.getBiome(blockPosition())));
            setVariant(random.nextInt(20) == 0 ? 1 + random.nextInt(3) : 0);
        }
        return super.finalizeSpawn(level, diff, reason, data);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Kind", entityData.get(KIND)); out.putInt("Variant", variant()); out.putInt("Team", team()); out.putInt("Wins", wins());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        entityData.set(KIND, in.getIntOr("Kind", 0)); entityData.set(VARIANT, in.getIntOr("Variant", 0));
        entityData.set(TEAM, in.getIntOr("Team", -1)); entityData.set(WINS, in.getIntOr("Wins", 0));
        applyStats();
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }                     // camps don't refill: keep them
    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.NONE; }

    @Override protected @Nullable SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.MUD_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.MUD_BREAK; }
    @Override public float getVoicePitch() { return 1.6f + random.nextFloat() * 0.2f; }
}

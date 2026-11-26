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
 * winner gets a victory, and with it a little more health and bite (up to five). Each is born a little different: its
 * health, attack and speed are rolled around its kind's (0.85 to 1.15). Use an empty hand on one to see its health over
 * its head; sneak and do it to look it over. After five minutes without a scratch it slowly heals back to full.
 */
public class Mudling extends PathfinderMob implements Baggable {
    /** The biome kinds, in texture order. health, damage, speed. */
    public enum Kind {
        SOD("sod", 8, 2.0, 0.28), TWIG("twig", 6, 1.5, 0.34), BARK("bark", 12, 2.0, 0.24), MUD("mud", 9, 1.5, 0.22),
        SANDSTONE("sandstone", 11, 2.0, 0.24), TERRACOTTA("terracotta", 10, 3.0, 0.25), SNOWPACK("snowpack", 6, 1.0, 0.30),
        VINE("vine", 7, 2.0, 0.32), GRAVEL("gravel", 9, 2.0, 0.26), SHELL("shell", 10, 1.5, 0.24), PEBBLE("pebble", 10, 2.0, 0.25),
        CINDER("cinder", 7, 2.0, 0.30), KITE("kite", 6, 1.5, 0.32);
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
                case CINDER -> o == TWIG || o == BARK || o == VINE || o == KITE;
                case SHELL -> o == CINDER || o == PEBBLE;
                case BARK -> o == GRAVEL || o == SOD;
                case GRAVEL -> o == SANDSTONE || o == TERRACOTTA || o == KITE;
                case TERRACOTTA -> o == SHELL || o == SOD;
                case PEBBLE -> o == GRAVEL || o == SNOWPACK;
                case VINE -> o == MUD || o == PEBBLE;
                case SOD -> o == MUD || o == TWIG;
                case KITE -> o == SOD || o == PEBBLE || o == SNOWPACK;          // strikes from above
            };
        }
    }
    /** Rare colourings (about 1 in 20): 0 plain, 1 gilded, 2 mossy, 3 charred. */
    public static final String[] VARIANTS = {"", "gilded", "mossy", "charred"};

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TEAM = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WINS = SynchedEntityData.defineId(Mudling.class, EntityDataSerializers.INT);

    /** Each mudling's own rolls on its kind's health, attack and speed (0.85 to 1.15 each), set when it's born. */
    private float rollHealth = 1f, rollAttack = 1f, rollSpeed = 1f;
    /** Ticks since it was last hurt: after five quiet minutes it slowly heals back to full. */
    private int calm;
    /** Ticks left showing its health above its head, and the name to put back afterwards. */
    private int showHealth; private net.minecraft.network.chat.@Nullable Component ownName; private boolean ownNameVisible;
    public static final int HEAL_AFTER = 20 * 60 * 5, HEAL_EVERY = 40;
    /** Gear, one of each, handed over by right-clicking: leather armour, a flint spearhead, a feather to glide on and a
     *  coonskin cap for morale (bits of {@link #gear}). It drops again when the mudling crumbles. */
    public static final int ARMOUR = 1, SPEAR = 2, GLIDE = 4, CAP = 8;
    private int gear;
    private static int gearFor(ItemStack s) {
        return s.is(Items.LEATHER) ? ARMOUR : s.is(Items.FLINT) ? SPEAR : s.is(Items.FEATHER) ? GLIDE
                : s.is(com.ryancraft.RyanItems.COONSKIN_CAP) ? CAP : 0;
    }
    public boolean has(int g) { return (gear & g) != 0; }

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

    private static final double HEALTH_SCALE = 1.5, DAMAGE_SCALE = 1.0 / 3.0;

    private void applyStats() {
        Kind k = kind(); int w = Math.min(5, wins());
        // tougher and softer-hitting than their kind's raw numbers (1.5x health, a third of the damage) so battles last;
        // the bonuses from wins and gear are scaled the same way so they keep their weight
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.round(HEALTH_SCALE * (k.health * rollHealth + 2 * w + (has(CAP) ? 3 : 0))));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(DAMAGE_SCALE * (k.damage * rollAttack + 0.5 * w + (has(SPEAR) ? 1.0 : 0)));
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(k.speed * rollSpeed * (has(CAP) ? 1.08 : 1.0));
    }

    // ------------------------------------------------------------------ behaviour
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SquadCombatGoal());
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.0, 1.25, e -> team() < 0));   // wild ones scatter
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mudling.class, 10, true, false,
                (t, level) -> t instanceof Mudling m && isEnemy(m)));
    }

    public boolean isEnemy(Mudling m) { return team() >= 0 && m.team() >= 0 && m.team() != team() && m.isAlive(); }

    // ------------------------------------------------------------------ squad combat
    /** A mudling's part in a fight, rolled at birth (weighted by its kind) and kept for life. */
    public enum Role { VANGUARD, FLANKER, SKIRMISHER, GUARD }
    /** What its squad is doing right now, called by the squad's most battle-hardened member and changed every so often. */
    public enum Tactic { CHARGE, PINCER, HOLD, FOCUS }
    private Role role = Role.VANGUARD; private Tactic tactic = Tactic.CHARGE; private int tacticFor;
    public Role role() { return role; }

    private void rollRole() {
        Role[] pool = switch (kind()) {
            case BARK, SANDSTONE, SHELL, TERRACOTTA -> new Role[]{Role.VANGUARD, Role.VANGUARD, Role.GUARD, Role.GUARD, Role.FLANKER};
            case TWIG, VINE, KITE -> new Role[]{Role.FLANKER, Role.FLANKER, Role.SKIRMISHER, Role.SKIRMISHER, Role.VANGUARD};
            case SNOWPACK, CINDER -> new Role[]{Role.SKIRMISHER, Role.SKIRMISHER, Role.FLANKER, Role.GUARD};
            case MUD, PEBBLE -> new Role[]{Role.GUARD, Role.GUARD, Role.VANGUARD, Role.SKIRMISHER};
            default -> Role.values();
        };
        role = pool[random.nextInt(pool.length)];
    }

    private java.util.List<Mudling> allies(double r) {
        return level().getEntitiesOfClass(Mudling.class, getBoundingBox().inflate(r), m -> m != this && m.isAlive() && team() >= 0 && m.team() == team());
    }
    private java.util.List<Mudling> enemies(double r) {
        return level().getEntitiesOfClass(Mudling.class, getBoundingBox().inflate(r), this::isEnemy);
    }
    /** Seniority in the squad: wins first, then health; the senior one calls the tactic. */
    private double rank() { return wins() * 100 + getMaxHealth() + getId() * 1e-6; }

    /** Squadmates come to help when one is attacked (called from hurtServer). */
    private void callForHelp(LivingEntity attacker) {
        for (Mudling a : allies(10)) {
            LivingEntity t = a.getTarget();
            if (t == null || !t.isAlive() || a.distanceToSqr(t) > a.distanceToSqr(attacker) + 16 || a.random.nextInt(3) == 0) a.setTarget(attacker);
        }
    }

    /** The snowpack scout's snowball: thrown from a few blocks off. */
    private void throwSnowball(LivingEntity t) {
        Snowball s = new Snowball(level(), Mudling.this, new ItemStack(net.minecraft.world.item.Items.SNOWBALL));
        double dx = t.getX() - getX(), dy = t.getY(0.4) - s.getY(), dz = t.getZ() - getZ();
        s.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.15, dz, 1.0f, 4f);
        level().addFreshEntity(s);
        playSound(SoundEvents.SNOWBALL_THROW, 0.5f, 1.6f);
        if (level() instanceof ServerLevel sl) t.hurtServer(sl, damageSources().thrown(s, Mudling.this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * matchup(t));
    }

    /**
     * How a mudling fights in a squad. Every second or so it re-reads the battle: its squad's tactic (called by the most
     * seasoned member, and changed every 8-15 seconds, so no two fights go the same), its own role, its health and who's
     * where. Then it picks a target (the nearest, the one the squad is ganging up on, the weakest, or whoever's at its
     * guard's back) and a way to go at it:
     * <ul><li>vanguards go straight in;</li>
     * <li>flankers circle round to come at the target from the side or behind (wide, on a pincer);</li>
     * <li>skirmishers dart in, strike and fall back to strike again;</li>
     * <li>guards stay by their weakest squadmate and set on whatever comes near it;</li>
     * <li>on "hold the line" the squad stands its ground until the enemy comes close, then counters;</li>
     * <li>snowpack scouts keep their distance and throw;</li>
     * <li>and a badly hurt mudling falls back behind its friends for a while, unless it's alone.</li></ul>
     * Choices carry a little randomness, so the same squads won't fight the same way twice.
     */
    private class SquadCombatGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int think, attackCd, retreatFor, skirmishBack;
        private double flankSide = 1;
        SquadCombatGoal() { setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return getTarget() != null && getTarget().isAlive() && team() >= 0; }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { think = 0; flankSide = random.nextBoolean() ? 1 : -1; }
        @Override public void stop() { getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        private void updateTactic(java.util.List<Mudling> squad) {
            if (tacticFor > 0) return;                                          // counted down every tick, in tick()
            Mudling leader = Mudling.this;
            for (Mudling a : squad) if (a.rank() > leader.rank()) leader = a;
            if (leader == Mudling.this) {                                       // I call it
                Tactic[] all = Tactic.values(); Tactic next = all[random.nextInt(all.length)];
                if (next == tactic && random.nextBoolean()) next = all[random.nextInt(all.length)];
                tactic = next;
                if (squad.size() < 2 && tactic == Tactic.PINCER) tactic = Tactic.CHARGE;   // no pincer with no one to pinch with
                if (squad.isEmpty() && tactic == Tactic.HOLD) tactic = Tactic.CHARGE;      // a line of one holds nothing
            } else tactic = leader.tactic;
            tacticFor = 160 + random.nextInt(140);
        }

        private LivingEntity pickTarget(java.util.List<Mudling> squad, java.util.List<Mudling> foes) {
            LivingEntity cur = getTarget();
            if (foes.isEmpty()) return cur;
            Mudling best = null; double bestScore = Double.MAX_VALUE;
            Mudling weakAlly = null;
            if (role == Role.GUARD) for (Mudling a : squad) if (weakAlly == null || a.getHealth() / a.getMaxHealth() < weakAlly.getHealth() / weakAlly.getMaxHealth()) weakAlly = a;
            for (Mudling f : foes) {
                double score = Math.sqrt(distanceToSqr(f));
                if (tactic == Tactic.FOCUS) {
                    int onIt = 0; for (Mudling a : squad) if (a.getTarget() == f) onIt++;
                    score -= onIt * 3 + (1 - f.getHealth() / f.getMaxHealth()) * 6;
                }
                if (role == Role.FLANKER && f.kind() == Kind.SNOWPACK) score -= 4;   // flankers go for the throwers at the back
                if (role == Role.GUARD && weakAlly != null) score = Math.sqrt(weakAlly.distanceToSqr(f));
                if (kind().beats(f.kind())) score -= 2;                         // and for a matchup it wins
                if (f == cur) score -= 1.5;                                     // a little stubborn, so it doesn't dither
                score += random.nextDouble() * 2;
                if (score < bestScore) { bestScore = score; best = f; }
            }
            return best;
        }

        @Override public void tick() {
            LivingEntity t = getTarget(); if (t == null) return;
            tacticFor--;
            if (--think <= 0) {
                think = 15 + random.nextInt(10);
                java.util.List<Mudling> squad = allies(16), foes = enemies(16);
                updateTactic(squad);
                LivingEntity pick = pickTarget(squad, foes);
                if (pick != null && pick != t) { setTarget(pick); t = pick; }
                if (retreatFor <= 0 && getHealth() < getMaxHealth() * 0.3f && !squad.isEmpty() && random.nextInt(3) > 0) retreatFor = 60 + random.nextInt(60);
            }
            getLookControl().setLookAt(t, 30, 30);
            double d = distanceToSqr(t);
            attackCd--;
            if (retreatFor > 0) { retreatFor--; fallBack(t); return; }

            if (kind() == Kind.SNOWPACK) {                                      // keep a throwing distance
                if (d > 49) getNavigation().moveTo(t, 1.1); else if (d < 12) away(t, 4, 1.15); else getNavigation().stop();
                if (attackCd <= 0 && d < 100 && hasLineOfSight(t)) { attackCd = 26 + random.nextInt(10); throwSnowball(t); }
                return;
            }
            boolean standoff = t instanceof Mudling m && m.tactic == Tactic.HOLD;  // both sides holding? then someone has to go
            if (tactic == Tactic.HOLD && d > 36 && role != Role.FLANKER && !standoff) {      // stand our ground until they come
                getNavigation().stop(); return;
            }
            if (skirmishBack > 0) { skirmishBack--; away(t, 4, 1.2); return; }
            switch (role) {
                case FLANKER -> {
                    double wide = tactic == Tactic.PINCER ? 4.5 : 3.0;
                    net.minecraft.world.phys.Vec3 to = t.position().subtract(position());
                    net.minecraft.world.phys.Vec3 side = new net.minecraft.world.phys.Vec3(-to.z, 0, to.x).normalize().scale(wide * flankSide);
                    if (d > 9) getNavigation().moveTo(t.getX() + side.x + to.normalize().x * 1.5, t.getY(), t.getZ() + side.z + to.normalize().z * 1.5, 1.3);
                    else getNavigation().moveTo(t, 1.3);
                }
                case GUARD -> {
                    Mudling weak = null;
                    for (Mudling a : allies(10)) if (weak == null || a.getHealth() < weak.getHealth()) weak = a;
                    if (weak != null && weak.distanceToSqr(t) > 36 && distanceToSqr(weak) > 9) getNavigation().moveTo(weak, 1.15);
                    else getNavigation().moveTo(t, 1.2);
                }
                default -> getNavigation().moveTo(t, role == Role.VANGUARD && tactic == Tactic.CHARGE ? 1.35 : 1.2);
            }
            if (attackCd <= 0 && isWithinMeleeAttackRange(t) && hasLineOfSight(t) && level() instanceof ServerLevel sl) {
                attackCd = 18 + random.nextInt(6);
                swing(InteractionHand.MAIN_HAND); doHurtTarget(sl, t);
                if (role == Role.SKIRMISHER) skirmishBack = 20 + random.nextInt(20);   // hit and fall back
            }
        }

        private void away(LivingEntity from, double dist, double speed) {
            net.minecraft.world.phys.Vec3 v = position().subtract(from.position()); v = new net.minecraft.world.phys.Vec3(v.x, 0, v.z).normalize().scale(dist);
            getNavigation().moveTo(getX() + v.x, getY(), getZ() + v.z, speed);
        }
        /** Back behind the squad: toward its middle, away from the enemy. */
        private void fallBack(LivingEntity from) {
            java.util.List<Mudling> squad = allies(16);
            if (squad.isEmpty()) { retreatFor = 0; return; }
            double cx = 0, cz = 0; for (Mudling a : squad) { cx += a.getX(); cz += a.getZ(); }
            cx /= squad.size(); cz /= squad.size();
            net.minecraft.world.phys.Vec3 away = new net.minecraft.world.phys.Vec3(cx - from.getX(), 0, cz - from.getZ()).normalize().scale(3);
            getNavigation().moveTo(cx + away.x, getY(), cz + away.z, 1.25);
        }
    }

    /** The cloud kite rides the air: it drifts down instead of falling and never takes fall damage. */
    @Override
    public void travel(net.minecraft.world.phys.Vec3 input) {
        super.travel(input);
        if (kind() != Kind.KITE || onGround() || isInWater()) return;
        net.minecraft.world.phys.Vec3 v = getDeltaMovement();
        if (v.y < -0.08) setDeltaMovement(v.x * 1.02, -0.08, v.z * 1.02);
        resetFallDistance();
    }

    /** Kites hop high into the air when they charge. */
    @Override
    protected float getJumpPower() { return super.getJumpPower() * (kind() == Kind.KITE ? 1.6f : 1f); }

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
        if (has(ARMOUR)) amount *= 0.7f;                                       // leather armour
        if (has(GLIDE) && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) return false;
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            calm = 0; if (showHealth > 0) showHealthTag();
            if (source.getEntity() instanceof Mudling m && isEnemy(m)) callForHelp(m);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        if (source.getEntity() instanceof Mudling winner && winner.isAlive() && level() instanceof ServerLevel) winner.addWin();
        super.die(source);
    }

    private void addWin() {
        entityData.set(WINS, Math.min(5, wins() + 1)); applyStats(); heal(getMaxHealth() * 0.5f);   // a win heals half, not all, so the first kill doesn't decide the fight
        playSound(SoundEvents.PLAYER_LEVELUP, 0.4f, 1.8f);
    }

    /** Losers crumble into their biome's materials. */
    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKill) {
        if (has(ARMOUR)) spawnAtLocation(level, new ItemStack(Items.LEATHER));      // its gear falls with it
        if (has(SPEAR)) spawnAtLocation(level, new ItemStack(Items.FLINT));
        if (has(GLIDE)) spawnAtLocation(level, new ItemStack(Items.FEATHER));
        if (has(CAP)) spawnAtLocation(level, new ItemStack(com.ryancraft.RyanItems.COONSKIN_CAP));
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
            case KITE -> new net.minecraft.world.item.Item[]{com.ryancraft.sky.Skylands.SKYSTONE.asItem(), Items.FEATHER, Items.STRING};
        };
        spawnAtLocation(level, new ItemStack(mats[0], 1 + random.nextInt(2)));
        spawnAtLocation(level, new ItemStack(mats[1]));
        if (random.nextInt(kind() == Kind.SHELL || kind() == Kind.CINDER || kind() == Kind.PEBBLE ? 4 : 2) == 0) spawnAtLocation(level, new ItemStack(mats[2]));
    }

    // ------------------------------------------------------------------ healing, and its health shown on request
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() || !isAlive()) return;
        if (has(GLIDE) && !onGround() && getDeltaMovement().y < -0.12)          // the feather: a gentle glide down
            setDeltaMovement(getDeltaMovement().x, -0.12, getDeltaMovement().z);
        if (calm < HEAL_AFTER) calm++;
        else if (getHealth() < getMaxHealth() && tickCount % HEAL_EVERY == 0) {     // five quiet minutes: a slow trickle back to full
            heal(1f); if (showHealth > 0) showHealthTag();
        }
        if (showHealth > 0 && --showHealth == 0) {                                  // put its own name back
            setCustomName(ownName); setCustomNameVisible(ownNameVisible);
        }
    }

    /** Hearts over its head for a few seconds: "Sod Mudling ❤ 7/9". */
    private void showHealthTag() {
        if (showHealth == 0) { ownName = getCustomName(); ownNameVisible = isCustomNameVisible(); }
        showHealth = 60;
        net.minecraft.network.chat.Component base = ownName != null ? ownName : getType().getDescription();
        setCustomName(base.copy().append(net.minecraft.network.chat.Component.literal(String.format(" \u2764 %d/%d", (int) Math.ceil(getHealth()), (int) getMaxHealth()))
                .withStyle(net.minecraft.ChatFormatting.RED)));
        setCustomNameVisible(true);
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
        int g = gearFor(stack);
        if (g != 0 && !has(g)) {                                               // kit it out
            if (!level().isClientSide()) {
                gear |= g; applyStats(); stack.consume(1, player); setPersistenceRequired();
                level().playSound(null, this, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 1.0f, 1.4f);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.isEmpty() && !player.isShiftKeyDown()) {                    // a quick look: its health over its head
            if (!level().isClientSide()) showHealthTag();
            return InteractionResult.SUCCESS;
        }
        if (stack.isEmpty() && player.isShiftKeyDown()) {                     // sneak and look it over: its stats
            if (!level().isClientSide()) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.ryancraft.mudling_stats",
                    getDisplayName().copy().append(", " + role.name().toLowerCase()), (int) getMaxHealth(), rating(rollHealth), String.format("%.1f", getAttributeValue(Attributes.ATTACK_DAMAGE)),
                    rating(rollAttack), rating(rollSpeed), wins(), gearList()), true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private net.minecraft.network.chat.Component gearList() {
        java.util.List<String> g = new java.util.ArrayList<>();
        if (has(ARMOUR)) g.add("armour"); if (has(SPEAR)) g.add("spear"); if (has(GLIDE)) g.add("feather"); if (has(CAP)) g.add("cap");
        return net.minecraft.network.chat.Component.literal(g.isEmpty() ? "none" : String.join(", ", g));
    }

    /** How a roll compares with its kind's usual: a word for each. */
    private static net.minecraft.network.chat.Component rating(float r) {
        String k = r < 0.93f ? "weak" : r < 0.98f ? "fair" : r <= 1.02f ? "average" : r <= 1.07f ? "good" : "great";
        return net.minecraft.network.chat.Component.translatable("message.ryancraft.mudling_rating." + k);
    }

    // ------------------------------------------------------------------ spawning, saving
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        if (reason != EntitySpawnReason.BUCKET && reason != EntitySpawnReason.LOAD) {
            // the plain spawn egg hatches any kind (a kind's own egg sets its kind afterwards); otherwise the biome decides
            setKind(reason == EntitySpawnReason.SPAWN_ITEM_USE ? Kind.values()[random.nextInt(Kind.values().length)]
                    : MudlingKinds.forBiome(level.getBiome(blockPosition())));
            setVariant(random.nextInt(20) == 0 ? 1 + random.nextInt(3) : 0);
            rollStats(); rollRole(); setHealth(getMaxHealth());
        }
        return super.finalizeSpawn(level, diff, reason, data);
    }

    /** Roll this mudling's own stats: each a little above or below its kind's, clustered near the middle. */
    private void rollStats() {
        rollHealth = roll(); rollAttack = roll(); rollSpeed = roll(); applyStats();
    }
    private float roll() { return 1f + (random.nextFloat() + random.nextFloat() - 1f) * 0.15f; }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Kind", entityData.get(KIND)); out.putInt("Variant", variant()); out.putInt("Team", team()); out.putInt("Wins", wins());
        out.putInt("Calm", calm); out.putInt("Gear", gear); out.putInt("Role", role.ordinal());
        out.putFloat("RollHealth", rollHealth); out.putFloat("RollAttack", rollAttack); out.putFloat("RollSpeed", rollSpeed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        entityData.set(KIND, in.getIntOr("Kind", 0)); entityData.set(VARIANT, in.getIntOr("Variant", 0));
        entityData.set(TEAM, in.getIntOr("Team", -1)); entityData.set(WINS, Math.min(5, in.getIntOr("Wins", 0)));
        calm = in.getIntOr("Calm", 0); gear = in.getIntOr("Gear", 0);
        if (in.getInt("Role").isPresent()) role = Role.values()[Math.floorMod(in.getIntOr("Role", 0), Role.values().length)]; else rollRole();
        // saved while its health was showing? take the hearts back off its name
        if (getCustomName() != null && getCustomName().getString().contains(" \u2764 ")) {
            String n = getCustomName().getString(); n = n.substring(0, n.indexOf(" \u2764 "));
            if (n.equals(getType().getDescription().getString())) { setCustomName(null); setCustomNameVisible(false); }
            else setCustomName(net.minecraft.network.chat.Component.literal(n));
        }
        rollHealth = in.getFloatOr("RollHealth", 1f); rollAttack = in.getFloatOr("RollAttack", 1f); rollSpeed = in.getFloatOr("RollSpeed", 1f);
        applyStats();
        if (in.getBooleanOr("FullHealth", false)) setHealth(getMaxHealth());          // fresh from a kind's spawn egg
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }                     // camps don't refill: keep them
    @Override public int bagCapacity() { return 4; }
    /** Middle-click gives this kind's own spawn egg, not whichever mudling egg the game finds first. */
    @Override
    public ItemStack getPickResult() {
        net.minecraft.world.item.Item egg = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(com.ryancraft.RyanCraft.id("mudling_" + kind().id + "_spawn_egg"));
        return egg == Items.AIR ? super.getPickResult() : new ItemStack(egg);
    }

    @Override public CageKind cageKind() { return CageKind.NONE; }

    @Override protected @Nullable SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.MUD_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.MUD_BREAK; }
    @Override public float getVoicePitch() { return 1.6f + random.nextFloat() * 0.2f; }
}

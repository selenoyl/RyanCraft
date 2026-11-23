package com.ryancraft.entity;

import com.ryancraft.moon.MoonBlocks;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Great Moon Rabbit: the moon's boss, a giant crowned rabbit of moon-dust fur wielding a mortar staff. It hops about
 * the arena between attacks: a heavy staff swing up close, a ground slam that rings out across the floor and throws you
 * up into the low gravity, meteors batted at you from range (hit them back like ghast fireballs), and packs of angry
 * moon rabbits, one in four a brute. Below half health it goes into a frenzy. It always drops the jade crown and the
 * Moon Mortar (loot table), and its minions vanish when it dies.
 */
public class GreatMoonRabbit extends Rabbit {
    /** Ticks left in the staff-swing animation (the client draws the staff from it). */
    public static final EntityDataAccessor<Integer> SWING = SynchedEntityData.defineId(GreatMoonRabbit.class, EntityDataSerializers.INT);
    private final ServerBossEvent bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10);
    private final List<UUID> minions = new ArrayList<>();
    private int hopCd = 40, swingCd, slamCd = 120, meteorCd = 60, summonCd = 200;

    public GreatMoonRabbit(EntityType<? extends GreatMoonRabbit> type, Level level) { super(type, level); xpReward = 100; }

    public static AttributeSupplier.Builder createAttributes() {
        return Rabbit.createAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.SCALE, 6.0);                                                   // a giant: model, gear and hitbox together
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(SWING, 0); }
    public int swing() { return entityData.get(SWING); }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 32f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean frenzy() { return getHealth() < getMaxHealth() / 2; }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bar.setProgress(getHealth() / getMaxHealth());
        if (swing() > 0) entityData.set(SWING, swing() - 1);
        LivingEntity t = getTarget();
        if (t == null || !t.isAlive()) return;
        getLookControl().setLookAt(t, 30f, 30f);
        boolean f = frenzy(); double d = distanceTo(t);
        hopCd--; swingCd--; slamCd--; meteorCd--; summonCd--;
        if (onGround() && hopCd <= 0) {                                         // a big floaty hop, toward and around you
            Vec3 to = t.position().subtract(position()); Vec3 h = new Vec3(to.x, 0, to.z).normalize();
            Vec3 side = new Vec3(-h.z, 0, h.x).scale((random.nextDouble() - 0.5) * 1.6);
            Vec3 dir = (d < 5 ? side.subtract(h.scale(0.4)) : h.add(side)).normalize();
            setDeltaMovement(dir.x * 0.75, f ? 0.95 : 0.8, dir.z * 0.75); needsSync = true;
            hopCd = f ? 22 : 40 + random.nextInt(20);
        }
        if (d < 4.5 && swingCd <= 0) {                                          // the mortar staff, swung wide
            entityData.set(SWING, 10);
            if (t.hurtServer(level, damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE)))
                t.knockback(2.2, getX() - t.getX(), getZ() - t.getZ());
            playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);
            swingCd = f ? 20 : 30;
        }
        if (d < 9 && onGround() && slamCd <= 0) {                               // ground slam: a ring bursts out across the floor
            entityData.set(SWING, 10);
            var dust = new BlockParticleOption(ParticleTypes.BLOCK, MoonBlocks.MOON_DUST.defaultBlockState());
            for (int r = 1; r <= 7; r++)
                for (int a = 0; a < r * 8; a++) {
                    double ang = a * Math.PI * 2 / (r * 8);
                    level.sendParticles(dust, getX() + Math.cos(ang) * r, getY() + 0.1, getZ() + Math.sin(ang) * r, 2, 0.1, 0.05, 0.1, 0.05);
                }
            for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(7, 2, 7), p -> p.onGround() && !p.isSpectator())) {
                p.hurtServer(level, damageSources().mobAttack(this), 6f);
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.9, 0)); p.hurtMarked = true;
            }
            level.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.4f, 0.5f);
            slamCd = f ? 100 : 160;
        }
        if (d > 6 && meteorCd <= 0) {                                            // bat a baby meteor (or a few) at you
            entityData.set(SWING, 10);
            int n = f ? 2 + random.nextInt(2) : 1;
            for (int i = 0; i < n; i++) {
                Vec3 aim = t.getEyePosition().subtract(getEyePosition()).normalize()
                        .add((random.nextDouble() - 0.5) * 0.15 * i, (random.nextDouble() - 0.5) * 0.1 * i, (random.nextDouble() - 0.5) * 0.15 * i);
                BossMeteor m = new BossMeteor(level, this, aim);
                m.setPos(getX() + aim.x * 2, getEyeY() - 0.5, getZ() + aim.z * 2);
                level.addFreshEntity(m);
            }
            playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.5f, 0.5f);
            meteorCd = f ? 60 : 100;
        }
        minions.removeIf(id -> !(level.getEntity(id) instanceof AngryMoonRabbit a) || !a.isAlive());
        if (summonCd <= 0 && minions.size() < 6) {                               // a pack of angry moon rabbits
            int n = 3 + random.nextInt(2);
            for (int i = 0; i < n; i++) {
                AngryMoonRabbit a = RyanEntities.ANGRY_MOON_RABBIT.create(level, EntitySpawnReason.MOB_SUMMONED);
                if (a == null) continue;
                double ang = random.nextDouble() * Math.PI * 2;
                a.snapTo(getX() + Math.cos(ang) * 3, getY() + 0.5, getZ() + Math.sin(ang) * 3, random.nextFloat() * 360f, 0f);
                if (random.nextInt(4) == 0) a.makeBrute();
                a.setTarget(t);
                level.addFreshEntity(a); minions.add(a.getUUID());
                level.sendParticles(ParticleTypes.POOF, a.getX(), a.getY() + 0.3, a.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            }
            summonCd = f ? 300 : 400;
        }
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel level)
            for (UUID id : minions) { Entity e = level.getEntity(id); if (e != null) e.discard(); }
        super.die(source);
    }

    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bar.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean requiresCustomPersistence() { return true; }
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
    @Override public boolean isFood(ItemStack s) { return false; }
    @Override public boolean canMate(Animal o) { return false; }
    @Override public @Nullable Rabbit getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override public boolean checkSpawnRules(LevelAccessor l, EntitySpawnReason r) { return r != EntitySpawnReason.NATURAL; }
    @Override protected float getJumpPower() { return super.getJumpPower() * 1.6f; }
}

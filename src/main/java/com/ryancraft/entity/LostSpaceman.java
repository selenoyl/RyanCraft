package com.ryancraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Lost spaceman: a long-dead Steve in an old spacesuit, his skull grinning behind a tinted visor (skins in lavender,
 * white, blue, red, orange and green). They shamble over the moon's craters at night, slow and floaty in the low
 * gravity; the sun doesn't burn them, water doesn't turn them, and none of them are babies or ride chickens.
 */
public class LostSpaceman extends Zombie {
    public static final String[] SUITS = {"lavender", "white", "blue", "red", "orange", "green"};
    private static final EntityDataAccessor<Integer> SUIT = SynchedEntityData.defineId(LostSpaceman.class, EntityDataSerializers.INT);

    public LostSpaceman(EntityType<? extends LostSpaceman> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.18).add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(SUIT, 0); }
    public int suit() { return entityData.get(SUIT); }

    @Override protected boolean isSunSensitive() { return false; }
    @Override protected boolean convertsInWater() { return false; }
    @Override public boolean isBaby() { return false; }
    @Override public void setBaby(boolean baby) {}
    @Override protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {}

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, data);
        if (isPassenger()) { var v = getVehicle(); stopRiding(); if (v != null) v.discard(); }     // no chicken jockeys
        entityData.set(SUIT, random.nextInt(4) == 0 ? 1 + random.nextInt(SUITS.length - 1) : 0);   // mostly the old lavender suits
        return out;
    }

    /** Out on the craters by night. */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL) return super.checkSpawnRules(level, reason);
        return level instanceof ServerLevelAccessor s && s.getLevel().isDarkOutside() && level.canSeeSky(blockPosition());
    }

    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putInt("Suit", suit()); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); entityData.set(SUIT, in.getIntOr("Suit", 0)); }
}

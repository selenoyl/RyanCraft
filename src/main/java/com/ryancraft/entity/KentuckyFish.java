package com.ryancraft.entity;

import com.ryancraft.RyanItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/** Bluegill, crappie, largemouth bass and channel catfish: creek and pond fish. One class, four entity types. */
public class KentuckyFish extends AbstractFish implements CritterVisual {
    public KentuckyFish(EntityType<? extends KentuckyFish> type, Level level) {
        super(type, level);
    }

    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> LENGTH =
            net.minecraft.network.syncher.SynchedEntityData.defineId(KentuckyFish.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);

    @Override public int getVariant() { return 0; }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(LENGTH, 0f); }

    /** Length in inches (rolled when it first appears). */
    public float length() { float l = this.entityData.get(LENGTH); return l > 0 ? l : com.ryancraft.fish.FishSize.spec(this.getType()).average(); }
    public void setLength(float l) { this.entityData.set(LENGTH, l); }

    /** Drawn to size: a 20-inch bass is bigger than a 12-inch one. */
    @Override public float renderScale() { return Math.max(0.55f, Math.min(1.9f, length() / com.ryancraft.fish.FishSize.spec(this.getType()).average())); }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d,
                                                                    EntitySpawnReason reason, net.minecraft.world.entity.SpawnGroupData data) {
        if (this.entityData.get(LENGTH) <= 0) setLength(com.ryancraft.fish.FishSize.spec(this.getType()).roll(this.random));
        return super.finalizeSpawn(level, d, reason, data);
    }

    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) { super.addAdditionalSaveData(out); out.putFloat("Length", this.entityData.get(LENGTH)); }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) { super.readAdditionalSaveData(in); setLength(in.getFloatOr("Length", 0f)); }

    @Override
    public void saveToBucketTag(ItemStack bucket) {
        super.saveToBucketTag(bucket);
        float l = length();
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA, bucket, t -> t.putFloat("Length", l));
        bucket.set(com.ryancraft.fish.FishSize.LENGTH, l);
    }

    @Override
    public void loadFromBucketTag(net.minecraft.nbt.CompoundTag tag) {
        super.loadFromBucketTag(tag);
        setLength(tag.getFloatOr("Length", com.ryancraft.fish.FishSize.spec(this.getType()).roll(this.random)));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(RyanItems.bucketFor(this.getType()));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            return level.getFluidState(this.blockPosition()).is(FluidTags.WATER)
                    && level.getFluidState(this.blockPosition().above()).is(FluidTags.WATER);
        }
        return super.checkSpawnRules(level, reason);
    }

    @Override protected SoundEvent getFlopSound() { return SoundEvents.COD_FLOP; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.COD_AMBIENT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.COD_DEATH; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.COD_HURT; }
}

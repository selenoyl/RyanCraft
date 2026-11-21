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

    @Override public int getVariant() { return 0; }

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

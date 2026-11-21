package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Shared base: spawns like vanilla farm animals (on grass-type blocks, in daylight-level light),
 * without needing to touch the private spawn placement registry.
 */
public abstract class RyanAnimal extends Animal {
    protected RyanAnimal(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return super.checkSpawnRules(level, reason);
        }
        BlockPos below = this.blockPosition().below();
        return level.getBlockState(below).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.getRawBrightness(this.blockPosition(), 0) > 8
                && !level.getBlockState(this.blockPosition()).liquid()
                && super.checkSpawnRules(level, reason);
    }
}

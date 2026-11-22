package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * Zebra: a horse variant. It's a horse in every way (tame it, saddle it, armour it, ride it) wearing a zebra coat,
 * found in herds on the savanna. Zebras breed with zebras.
 */
public class Zebra extends Horse {
    public Zebra(EntityType<? extends Zebra> type, Level level) { super(type, level); }

    /** No horse markings over the stripes. */
    @Override public Markings getMarkings() { return Markings.NONE; }

    @Override
    public boolean canMate(Animal other) {
        return other != this && other instanceof Zebra z && this.isInLove() && z.isInLove() && this.isTamed() && z.isTamed()
                && !this.isBaby() && !z.isBaby();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Zebra foal = RyanEntities.ZEBRA.create(level, EntitySpawnReason.BREEDING);
        if (foal != null && partner instanceof AbstractHorse h) this.setOffspringAttributes(partner, foal);
        return foal;
    }

    /** On the grassy savanna floor in daylight, like the other animals. */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return super.checkSpawnRules(level, reason);
        BlockPos below = this.blockPosition().below();
        return level.getBlockState(below).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(this.blockPosition(), 0) > 8;
    }
}

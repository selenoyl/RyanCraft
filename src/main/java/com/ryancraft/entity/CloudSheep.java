package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Cloud sheep: the Skylands' extra-puffy sheep. Its wool billows out like a cloud (an extra puff layer over the usual
 * fleece, client/render/CloudSheepPuffLayer), it shears for more wool, dyes like any sheep, breeds with cloud sheep,
 * and drifts down gently when it falls, so it never takes fall damage.
 */
public class CloudSheep extends Sheep {
    private static final double DRIFT_FALL = -0.08;

    public CloudSheep(EntityType<? extends CloudSheep> type, Level level) { super(type, level); }

    @Override
    public void travel(Vec3 input) {
        super.travel(input);
        if (onGround() || isInWater()) return;
        Vec3 v = getDeltaMovement();
        if (v.y < DRIFT_FALL) setDeltaMovement(v.x, DRIFT_FALL, v.z);
        resetFallDistance();
    }

    /** Twice the fleece: one to two extra wool on top of the usual shearing. */
    @Override
    public void shear(ServerLevel level, SoundSource source, ItemStack shears) {
        super.shear(level, source, shears);
        var wool = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(getColor().getName() + "_wool"));
        int extra = 1 + random.nextInt(2);
        for (int i = 0; i < extra; i++) spawnAtLocation(level, new ItemStack(wool), 1.0f);
    }

    @Override
    public boolean canMate(Animal other) {
        return other != this && other instanceof CloudSheep && this.isInLove() && other.isInLove();
    }

    @Override
    public @Nullable Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Sheep mixed = super.getBreedOffspring(level, partner);          // vanilla works out the lamb's colour
        CloudSheep lamb = RyanEntities.CLOUD_SHEEP.create(level, EntitySpawnReason.BREEDING);
        if (lamb != null && mixed != null) lamb.setColor(mixed.getColor());
        return lamb;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return super.checkSpawnRules(level, reason);
        BlockPos below = this.blockPosition().below();
        return level.getBlockState(below).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(this.blockPosition(), 0) > 8;
    }
}

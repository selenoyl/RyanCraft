package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Base for the Kentucky critters: colour variants, inheritance, optional tree climbing. */
public abstract class CritterAnimal extends RyanAnimal implements CritterVisual {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(CritterAnimal.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(CritterAnimal.class, EntityDataSerializers.BOOLEAN);

    protected CritterAnimal(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    /** Number of colour variants (textures). */
    protected int variantCount() { return 1; }

    /** Pick a variant for a wild spawn at this spot. */
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        return this.random.nextInt(variantCount());
    }

    /** Scampers up walls and tree trunks like a spider. */
    protected boolean climbs() { return false; }

    /** Translation suffix for this variant's name ("fox", "cinnamon"...), or null for the base name. */
    protected String variantName() { return null; }

    @Override
    protected net.minecraft.network.chat.Component getTypeName() {
        String suffix = variantName();
        return suffix == null ? super.getTypeName()
                : net.minecraft.network.chat.Component.translatable(this.getType().getDescriptionId() + "." + suffix);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(CLIMBING, false);
    }

    @Override
    public int getVariant() { return this.entityData.get(VARIANT); }

    public void setVariant(int v) { this.entityData.set(VARIANT, Math.floorMod(v, Math.max(1, variantCount()))); }

    @Override
    public boolean isClimbingVisual() { return this.entityData.get(CLIMBING); }

    @Override
    public boolean onClimbable() {
        return climbs() ? this.isClimbingVisual() : super.onClimbable();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && climbs()) {
            this.entityData.set(CLIMBING, this.horizontalCollision);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Variant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setVariant(in.getIntOr("Variant", 0));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        if (reason != EntitySpawnReason.BREEDING) {
            setVariant(chooseVariant(level, this.blockPosition()));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** Babies take after one parent. */
    protected <T extends CritterAnimal> T offspring(EntityType<T> type, ServerLevel level, AgeableMob partner) {
        T child = type.create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            int v = (partner instanceof CritterAnimal other && this.random.nextBoolean()) ? other.getVariant() : this.getVariant();
            child.setVariant(v);
        }
        return child;
    }
}

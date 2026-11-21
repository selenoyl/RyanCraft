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

    // ---- shy until fed (like ocelots): bolts from people until you win it over with food
    private boolean trusting;

    /** Wild ones run from people until fed (see {@link #isTrusting()}). */
    protected boolean skittish() { return false; }
    public boolean isTrusting() { return trusting; }
    public void setTrusting(boolean t) { trusting = t; }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (skittish() && !trusting && isFood(stack) && player.distanceToSqr(this) < 9.0) {
            this.usePlayerItem(player, hand, stack);
            if (!this.level().isClientSide()) {
                if (this.random.nextInt(3) == 0) { trusting = true; this.level().broadcastEntityEvent(this, (byte) 41); }
                else this.level().broadcastEntityEvent(this, (byte) 40);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 40 || id == 41) {
            var particle = id == 41 ? net.minecraft.core.particles.ParticleTypes.HEART : net.minecraft.core.particles.ParticleTypes.SMOKE;
            for (int i = 0; i < 7; i++) {
                this.level().addParticle(particle, this.getRandomX(1.0), this.getRandomY() + 0.3, this.getRandomZ(1.0),
                        this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
            }
        } else super.handleEntityEvent(id);
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

    public void setVariant(int v) {
        int variant = Math.floorMod(v, Math.max(1, variantCount()));
        this.entityData.set(VARIANT, variant);
        onVariantSet(variant);
    }

    /** Called whenever the variant is set (spawn, birth, load). */
    protected void onVariantSet(int variant) {}

    @Override
    public boolean isClimbingVisual() { return this.entityData.get(CLIMBING) && !this.onGround(); }

    @Override
    public boolean onClimbable() {
        return climbs() ? this.entityData.get(CLIMBING) : super.onClimbable();
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
        if (trusting) out.putBoolean("Trusting", true);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setVariant(in.getIntOr("Variant", 0));
        trusting = in.getBooleanOr("Trusting", false);
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
            child.setTrusting(this.trusting || (partner instanceof CritterAnimal o && o.trusting));
        }
        return child;
    }
}

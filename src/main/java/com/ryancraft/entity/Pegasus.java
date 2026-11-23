package com.ryancraft.entity;

import com.ryancraft.RyanCraft;
import com.ryancraft.mixin.HorseVariantInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Pegasus: a winged horse of the Skylands. A horse in every way (tame, saddle, armour, breed with pegasi), but it jumps
 * twice as high (its wings add a fixed boost on top of an ordinary horse's jump strength, so foals inherit normally) and
 * glides: falling, it spreads its wings, drifts down slowly and never takes fall damage. Ridden, it glides forward.
 * Pegasi are white or grey. A pegasus can also breed with an ordinary horse: the foal is a winged hybrid in any horse
 * coat (mixin/HorseBreedPegasusMixin handles the horse's side of the pairing).
 */
public class Pegasus extends Horse {
    /** Jump velocity x sqrt(2) is twice the jump height. */
    private static final AttributeModifier WINGS = new AttributeModifier(RyanCraft.id("pegasus_wings"), Math.sqrt(2) - 1,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final double GLIDE_FALL = -0.12, GLIDE_SPEED = 0.42;
    /** A pure pegasus is always white or grey; a hybrid (pegasus x horse) can wear any horse coat. */
    private static final Variant[] COATS = {Variant.WHITE, Variant.GRAY};
    /** Client: how far the wings are spread (0 folded, 1 open). */
    public float wingSpread, prevWingSpread;

    public Pegasus(EntityType<? extends Pegasus> type, Level level) {
        super(type, level);
        AttributeInstance jump = getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) jump.addOrReplacePermanentModifier(WINGS);
    }

    /** On the ground or just about to land: a solid block right underfoot (works even when onGround isn't synced). */
    private boolean grounded() {
        if (onGround()) return true;
        BlockPos below = BlockPos.containing(getX(), getY() - 0.2, getZ());
        return !level().getBlockState(below).getCollisionShape(level(), below).isEmpty();
    }

    public boolean gliding() { return !onGround() && !isInWater() && getDeltaMovement().y < 0; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            prevWingSpread = wingSpread;
            float target = grounded() || isInWater() ? 0f : 1f;
            wingSpread += (target - wingSpread) * 0.3f;
        }
    }

    @Override
    public void travel(Vec3 input) {
        super.travel(input);
        if (onGround() || isInWater()) return;
        Vec3 v = getDeltaMovement();
        if (v.y < GLIDE_FALL) {
            double h = Math.hypot(v.x, v.z), x = v.x, z = v.z;
            if (getControllingPassenger() instanceof Player && h < GLIDE_SPEED) {   // ridden: glide on forward
                Vec3 fwd = Vec3.directionFromRotation(0, getYRot());
                x += (fwd.x * GLIDE_SPEED - x) * 0.15; z += (fwd.z * GLIDE_SPEED - z) * 0.15;
            }
            setDeltaMovement(x, GLIDE_FALL, z);
        }
        resetFallDistance();
    }

    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, data);
        ((HorseVariantInvoker) this).ryancraft$setVariantAndMarkings(COATS[random.nextInt(COATS.length)],
                random.nextInt(3) == 0 ? Markings.values()[random.nextInt(Markings.values().length)] : Markings.NONE);
        return out;
    }

    /** Pegasi breed with pegasi and with ordinary horses (not zebras, which keep to their own). */
    @Override
    public boolean canMate(Animal other) {
        return other != this && other instanceof Horse h && !(other instanceof Zebra) && this.isInLove() && h.isInLove()
                && this.isTamed() && h.isTamed() && !this.isBaby() && !h.isBaby();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Pegasus foal = RyanEntities.PEGASUS.create(level, EntitySpawnReason.BREEDING);
        if (foal == null || !(partner instanceof Horse mate)) return foal;
        this.setOffspringAttributes(partner, foal);
        Variant coat; Markings marks;
        if (mate instanceof Pegasus) {                                  // two pegasi: a white or grey pegasus
            coat = COATS[random.nextInt(COATS.length)];
            marks = random.nextBoolean() ? this.getMarkings() : mate.getMarkings();
        } else {                                                        // a hybrid: any coat, any pattern
            Variant[] all = Variant.values(); Markings[] m = Markings.values();
            coat = random.nextBoolean() ? mate.getVariant() : all[random.nextInt(all.length)];
            marks = random.nextInt(3) == 0 ? mate.getMarkings() : m[random.nextInt(m.length)];
        }
        ((HorseVariantInvoker) foal).ryancraft$setVariantAndMarkings(coat, marks);
        return foal;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return super.checkSpawnRules(level, reason);
        BlockPos below = this.blockPosition().below();
        return level.getBlockState(below).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(this.blockPosition(), 0) > 8;
    }
}

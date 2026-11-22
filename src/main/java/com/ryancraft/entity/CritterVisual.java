package com.ryancraft.entity;

/** What the shared critter renderer needs to know about an animal. */
public interface CritterVisual {
    int getVariant();
    default boolean isPlayingDead() { return false; }
    default boolean isFlyingVisual() { return false; }
    /** Extra size factor for this one animal (fish are drawn to their length). */
    default float renderScale() { return 1.0f; }
    /** Sitting down (tamed and told to stay). */
    default boolean isSittingVisual() { return false; }
    /** Really in the air: the client doesn't always know onGround for a still mob, so look for ground just below. */
    default boolean airborne() {
        if (!(this instanceof net.minecraft.world.entity.LivingEntity e)) return false;
        if (isSittingVisual() || e.isInWater()) return false;
        return !e.onGround() && e.level().noCollision(e, e.getBoundingBox().move(0, -0.15, 0));
    }
    default boolean isClimbingVisual() { return false; }
    /** Which texture to draw (defaults to the variant). */
    default int textureIndex() { return getVariant(); }
    /** Antler stage for models with staged antler parts (-1 = none). */
    default int antlerStage() { return -1; }
    /** Rare rack (0 = typical). */
    default int antlerRack() { return 0; }
}

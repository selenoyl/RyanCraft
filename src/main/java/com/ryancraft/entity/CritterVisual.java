package com.ryancraft.entity;

/** What the shared critter renderer needs to know about an animal. */
public interface CritterVisual {
    int getVariant();
    default boolean isPlayingDead() { return false; }
    default boolean isFlyingVisual() { return false; }
    default boolean isClimbingVisual() { return false; }
    /** Which texture to draw (defaults to the variant). */
    default int textureIndex() { return getVariant(); }
    /** Antler stage for models with staged antler parts (-1 = none). */
    default int antlerStage() { return -1; }
    /** Rare rack (0 = typical). */
    default int antlerRack() { return 0; }
}

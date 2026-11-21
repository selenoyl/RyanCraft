package com.ryancraft.entity;

/** What the shared critter renderer needs to know about an animal. */
public interface CritterVisual {
    int getVariant();
    default boolean isPlayingDead() { return false; }
    default boolean isFlyingVisual() { return false; }
    default boolean isClimbingVisual() { return false; }
}

package com.ryancraft.bag;

/**
 * A small mob that can be scooped into a bundle (a "mob bag"). One kind of mob per bag; once a bundle holds
 * mobs it holds nothing else, and the mobs never come out as items, only back into the world or into a cage.
 * Mudlings will join in later (up to 16 of one kind per bag).
 */
public interface Baggable {
    /** How many of this mob fit in one bundle. */
    int bagCapacity();

    /** Which cage it can live in. */
    CageKind cageKind();

    enum CageKind { CRITTER, BIRD, NONE }
}

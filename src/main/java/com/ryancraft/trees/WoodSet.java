package com.ryancraft.trees;

import net.minecraft.world.level.block.Block;

import java.util.List;

/** Every block made from one species, matching the vanilla wood families. */
public record WoodSet(TreeSpecies species, Block log, Block wood, Block strippedLog, Block strippedWood, Block planks,
                      Block leaves, Block sapling, Block slab, Block stairs, Block fence, Block fenceGate,
                      Block door, Block trapdoor, Block button, Block pressurePlate) {
    public List<Block> all() {
        return List.of(log, wood, strippedLog, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor,
                pressurePlate, button, leaves, sapling);
    }
}

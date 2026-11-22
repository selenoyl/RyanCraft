package com.ryancraft.pipes;

/** The pipe materials, Tekkit-style. Item pipes move items; fluid pipes move water and lava. */
public enum PipeType {
    /** Pulls items out of the inventory it touches each time an engine pulses it. */
    WOODEN("wooden", 0.045f, false),
    COBBLESTONE("cobblestone", 0.045f, false),
    STONE("stone", 0.06f, false),
    /** Speeds items up (they keep the speed for a while after). */
    GOLD("gold", 0.08f, false),
    /** Sends everything out of one side; turn it with a wrench. */
    IRON("iron", 0.05f, false),
    /** Sorts: each side has a filter row; matching items go out that side. */
    DIAMOND("diamond", 0.06f, false),
    /** Sucks up items lying nearby. */
    OBSIDIAN("obsidian", 0.05f, false),
    /** Waterproofed stone pipe: carries water and lava. */
    FLUID("fluid", 0f, true);

    public final String id;
    public final float baseSpeed;
    public final boolean fluid;

    PipeType(String id, float baseSpeed, boolean fluid) { this.id = id; this.baseSpeed = baseSpeed; this.fluid = fluid; }
}

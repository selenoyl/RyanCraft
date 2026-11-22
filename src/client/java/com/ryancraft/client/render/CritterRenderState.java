package com.ryancraft.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class CritterRenderState extends LivingEntityRenderState {
    public int variant;
    public boolean flying;
    /** Sitting: legs tucked, body down. */
    public boolean sitting;
    /** Gaining height this tick (soaring birds flap instead of gliding). */
    public boolean rising;
    public boolean playingDead;
    public boolean climbing;
    public int texture;
    public int antlers = -1;
    public int rack;
}

package com.ryancraft.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class CritterRenderState extends LivingEntityRenderState {
    public int variant;
    public boolean flying;
    public boolean playingDead;
    public boolean climbing;
}

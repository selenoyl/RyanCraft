package com.ryancraft.client.mudling;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MudlingRenderState extends LivingEntityRenderState {
    public int kind, variant, team = -1, wins;
    public float attackTime;
}

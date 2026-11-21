package com.ryancraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** Old-school animation: head follows look direction, legs swing in opposite pairs. */
public abstract class BoxyModel extends EntityModel<LivingEntityRenderState> {
    protected final ModelPart head;

    protected BoxyModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
    }

    protected static float swing(LivingEntityRenderState state, boolean opposite) {
        float phase = opposite ? Mth.PI : 0.0f;
        return Mth.cos(state.walkAnimationPos * 0.6662f + phase) * 1.4f * state.walkAnimationSpeed;
    }
}

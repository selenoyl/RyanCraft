package com.ryancraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import com.ryancraft.client.model.CritterModel;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Draws a tame crow perched on the player's shoulder, the way vanilla draws parrots. */
public class CrowOnShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final RenderStateDataKey<Byte> CROWS = RenderStateDataKey.create(() -> "ryancraft:crow_shoulders");
    private static final Identifier TEXTURE = RyanCraft.id("textures/entity/crow.png");
    private final CritterModel model;

    public CrowOnShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet models) {
        super(parent);
        this.model = new CritterModel(models.bakeLayer(CritterModel.layer("crow")), "crow");
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        Byte bits = ((FabricRenderState) state).getData(CROWS);
        if (bits == null || bits == 0) return;
        if ((bits & 1) != 0) one(pose, collector, light, state, true);
        if ((bits & 2) != 0) one(pose, collector, light, state, false);
    }

    private void one(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, boolean left) {
        pose.pushPose();
        pose.translate(left ? 0.4f : -0.4f, state.isCrouching ? -1.3f : -1.5f, 0.0f);
        CritterRenderState s = new CritterRenderState();
        s.ageInTicks = state.ageInTicks;
        model.setupAnim(s);
        collector.submitModel(model, s, pose, model.renderType(TEXTURE), light, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        pose.popPose();
    }
}

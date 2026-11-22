package com.ryancraft.client.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.pipes.TankBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

/** The fluid level inside a glass tank. */
public class TankRenderer implements BlockEntityRenderer<TankBlockEntity, TankRenderer.State> {
    public static class State extends BlockEntityRenderState { TextureAtlasSprite sprite; int colour; float fill; }
    public TankRenderer(BlockEntityRendererProvider.Context ctx) {}
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TankBlockEntity be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        s.sprite = null;
        if (be.tank.amount > 0 && !be.tank.variant.isBlank()) {
            s.sprite = FluidVariantRendering.getSprite(be.tank.variant);
            s.colour = FluidVariantRendering.getColor(be.tank.variant, be.getLevel(), be.getBlockPos());
            s.fill = (float) be.tank.amount / (FluidConstants.BUCKET * 16);
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        if (s.sprite == null) return;
        TextureAtlasSprite sp = s.sprite; int col = s.colour, light = s.lightCoords;
        float top = 0.02f + 0.96f * Math.min(1f, s.fill);
        out.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS),
                (p, vc) -> FluidBoxes.box(p, vc, sp, col, light, 2.4f / 16, 0.02f, 2.4f / 16, 13.6f / 16, top, 13.6f / 16));
    }
}

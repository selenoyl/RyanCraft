package com.ryancraft.client.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.pipes.TankBlock;
import com.ryancraft.pipes.TankBlockEntity;
import com.ryancraft.pipes.VatBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** The fluid inside a glass tank (seamless through a stack) or an open vat. */
public class TankRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, TankRenderer.State> {
    public static class State extends BlockEntityRenderState { TextureAtlasSprite sprite; int colour; float x0, y0, z0, x1, y1, z1; }
    public TankRenderer(BlockEntityRendererProvider.Context ctx) {}
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(T be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        s.sprite = null;
        SingleVariantStorage<FluidVariant> tank = be instanceof TankBlockEntity t ? t.tank : be instanceof VatBlockEntity v ? v.tank : null;
        if (tank == null || tank.amount <= 0 || tank.variant.isBlank()) return;
        float fill = Math.min(1f, (float) tank.amount / tank.getCapacity());
        s.sprite = FluidVariantRendering.getSprite(tank.variant);
        s.colour = FluidVariantRendering.getColor(tank.variant, be.getLevel(), be.getBlockPos());
        if (be instanceof TankBlockEntity) {
            boolean up = be.getBlockState().getValue(TankBlock.UP), down = be.getBlockState().getValue(TankBlock.DOWN);
            float bottom = down ? 0f : 2.01f / 16, top = up ? 1f : 13.99f / 16;
            s.x0 = s.z0 = 2.01f / 16; s.x1 = s.z1 = 13.99f / 16; s.y0 = bottom; s.y1 = bottom + (top - bottom) * fill;
        } else {
            s.x0 = s.z0 = 3.01f / 16; s.x1 = s.z1 = 12.99f / 16; s.y0 = 1.01f / 16; s.y1 = s.y0 + (13.5f / 16) * fill;
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        if (s.sprite == null) return;
        TextureAtlasSprite sp = s.sprite; int col = s.colour, light = s.lightCoords;
        out.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS),
                (p, vc) -> FluidBoxes.box(p, vc, sp, col, light, s.x0, s.y0, s.z0, s.x1, s.y1, s.z1));
    }
}

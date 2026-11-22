package com.ryancraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.fish.FishPlaqueBlock;
import com.ryancraft.fish.FishPlaqueBlockEntity;
import com.ryancraft.fish.FishSize;
import com.ryancraft.fish.FishingLog;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** The mounted fish, lying level across the plaque, with its length engraved beneath. */
public class FishPlaqueRenderer implements BlockEntityRenderer<FishPlaqueBlockEntity, FishPlaqueRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState(); boolean has; Direction facing = Direction.NORTH;
        FormattedCharSequence label; int labelWidth; boolean trophy;
    }
    private final ItemModelResolver items; private final Font font;
    public FishPlaqueRenderer(BlockEntityRendererProvider.Context ctx) { items = ctx.itemModelResolver(); font = ctx.font(); }
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(FishPlaqueBlockEntity be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        s.facing = be.getBlockState().getValue(FishPlaqueBlock.FACING);
        s.has = !be.fish.isEmpty();
        if (!s.has) return;
        items.updateForTopItem(s.item, be.fish, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        Float len = be.fish.get(FishSize.LENGTH);
        s.trophy = FishSize.isTrophy(be.fish);
        s.label = Component.literal(len == null ? "" : FishingLog.inches(len)).getVisualOrderText();
        s.labelWidth = font.width(s.label);
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        if (!s.has) return;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - s.facing.toYRot()));      // local -z now points out of the plaque
        pose.pushPose();
        pose.translate(0, 0.07, 0.35);
        pose.mulPose(Axis.ZP.rotationDegrees(45));                            // fish icons are drawn on the diagonal
        pose.scale(0.54f, 0.54f, 0.54f);
        s.item.submit(pose, out, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
        pose.translate(0, -0.25, 0.364);
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.0085f, -0.0085f, 0.0085f);
        out.submitText(pose, -s.labelWidth / 2f, 0, s.label, false, Font.DisplayMode.POLYGON_OFFSET, s.lightCoords,
                s.trophy ? 0xFFF0C040 : 0xFFE8DCC0, 0, 0);
        pose.popPose();
    }
}

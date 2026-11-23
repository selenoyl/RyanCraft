package com.ryancraft.client.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.RyanCraft;
import com.ryancraft.storage.RyanChestBlock;
import com.ryancraft.storage.RyanChestBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the special chests on vanilla's chest model with our own textures (assets/ryancraft/textures/entity/chest),
 * lid and all. See-through chests are drawn cutout and double-sided, like glass, with up to nine of their stacks turning slowly inside.
 */
public class RyanChestRenderer implements BlockEntityRenderer<RyanChestBlockEntity, RyanChestRenderer.State> {
    public static class State extends BlockEntityRenderState {
        float angle, open; RyanChestBlock.Kind kind = RyanChestBlock.Kind.OBSIDIAN; float spin;
        final List<ItemStackRenderState> shown = new ArrayList<>();
    }
    private static final int SHOW = 9;
    private final ChestModel model; private final MaterialSet materials; private final ItemModelResolver items;

    public RyanChestRenderer(BlockEntityRendererProvider.Context ctx) {
        model = new ChestModel(ctx.bakeLayer(ModelLayers.CHEST)); materials = ctx.materials(); items = ctx.itemModelResolver();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(RyanChestBlockEntity be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        s.kind = be.kind();
        s.angle = be.getBlockState().hasProperty(RyanChestBlock.FACING) ? be.getBlockState().getValue(RyanChestBlock.FACING).toYRot() : 0;
        s.open = be.getOpenNess(partial);
        s.shown.clear();
        if (!s.kind.seeThrough || be.getLevel() == null) return;
        s.spin = (be.getLevel().getGameTime() + partial) * 2f;
        for (ItemStack st : be.contents()) {
            if (st.isEmpty()) continue;
            ItemStackRenderState r = new ItemStackRenderState();
            items.updateForTopItem(r, st, ItemDisplayContext.GROUND, be.getLevel(), null, 0);
            s.shown.add(r);
            if (s.shown.size() >= SHOW) break;
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(-s.angle));
        pose.translate(-0.5f, -0.5f, -0.5f);
        if (s.kind.seeThrough) pose.translate(0f, 0.002f, 0f);           // drawn double-sided: lift it off the floor so the bottom doesn't flicker
        if (!s.shown.isEmpty()) {                                   // the contents, in a 3 x 3 on the chest floor
            for (int i = 0; i < s.shown.size(); i++) {
                pose.pushPose();
                pose.translate(0.28f + (i % 3) * 0.22f, 0.22f, 0.28f + (i / 3) * 0.22f);
                pose.mulPose(Axis.YP.rotationDegrees(s.spin + i * 40));
                pose.scale(0.5f, 0.5f, 0.5f);
                s.shown.get(i).submit(pose, out, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                pose.popPose();
            }
        }
        float o = 1 - s.open; o = 1 - o * o * o;
        Material mat = Sheets.CHEST_MAPPER.apply(RyanCraft.id(s.kind.id));
        var type = s.kind.seeThrough ? mat.renderType(RenderTypes::entityCutoutNoCull) : mat.renderType(RenderTypes::entityCutout);
        out.submitModel(model, o, pose, type, s.lightCoords, OverlayTexture.NO_OVERLAY, -1, materials.get(mat), 0, s.breakProgress);
        pose.popPose();
    }
}

package com.ryancraft.client.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.pipes.PipeBlock;
import com.ryancraft.pipes.PipeBlockEntity;
import com.ryancraft.pipes.TravelingItem;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Items riding through pipes, like the good old days; and the fluid sloshing inside fluid pipes. */
public class PipeRenderer implements BlockEntityRenderer<PipeBlockEntity, PipeRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final List<ItemStackRenderState> items = new ArrayList<>();
        final List<double[]> where = new ArrayList<>();
        TextureAtlasSprite fluid;
        int colour;
        float fill;
        final boolean[] sides = new boolean[6];
        float spin;
    }

    private final ItemModelResolver items;
    public PipeRenderer(BlockEntityRendererProvider.Context ctx) { this.items = ctx.itemModelResolver(); }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(PipeBlockEntity be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        s.items.clear(); s.where.clear();
        int seed = 0;
        for (TravelingItem t : new ArrayList<>(be.items)) {
            ItemStackRenderState r = new ItemStackRenderState();
            items.updateForTopItem(r, t.stack, ItemDisplayContext.GROUND, be.getLevel(), null, seed++);
            float extra = (t.to == null && t.progress >= 0.5f) || t.progress >= 1f ? 0f : t.speed * partial;
            s.items.add(r); s.where.add(t.position(extra));
        }
        s.spin = be.getLevel() == null ? 0 : (be.getLevel().getGameTime() + partial) * 4f;
        s.fluid = null;
        if (be.amount > 0 && !be.fluid.isBlank()) {
            s.fluid = FluidVariantRendering.getSprite(be.fluid);
            s.colour = FluidVariantRendering.getColor(be.fluid, be.getLevel(), be.getBlockPos());
            s.fill = Math.min(1f, (float) be.amount / PipeBlockEntity.FLUID_CAPACITY);
            for (Direction d : Direction.values()) s.sides[d.get3DDataValue()] = be.getBlockState().getValue(PipeBlock.SIDES.get(d));
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        for (int i = 0; i < s.items.size(); i++) {
            double[] w = s.where.get(i);
            pose.pushPose();
            pose.translate(w[0], w[1] - 0.12, w[2]);
            pose.scale(0.5f, 0.5f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(s.spin));
            s.items.get(i).submit(pose, out, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        if (s.fluid != null) {
            TextureAtlasSprite sp = s.fluid; int col = s.colour, light = s.lightCoords;
            float lo = 4.6f / 16f, hi = 11.4f / 16f, top = lo + (hi - lo) * Math.max(0.15f, s.fill);
            out.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (p, vc) -> {
                FluidBoxes.box(p, vc, sp, col, light, lo, lo, lo, hi, top, hi);
                if (s.sides[Direction.DOWN.get3DDataValue()]) FluidBoxes.box(p, vc, sp, col, light, lo, 0, lo, hi, lo, hi);
                if (s.sides[Direction.UP.get3DDataValue()] && s.fill > 0.9f) FluidBoxes.box(p, vc, sp, col, light, lo, hi, lo, hi, 1, hi);
                if (s.sides[Direction.NORTH.get3DDataValue()]) FluidBoxes.box(p, vc, sp, col, light, lo, lo, 0, hi, top, lo);
                if (s.sides[Direction.SOUTH.get3DDataValue()]) FluidBoxes.box(p, vc, sp, col, light, lo, lo, hi, hi, top, 1);
                if (s.sides[Direction.WEST.get3DDataValue()]) FluidBoxes.box(p, vc, sp, col, light, 0, lo, lo, lo, top, hi);
                if (s.sides[Direction.EAST.get3DDataValue()]) FluidBoxes.box(p, vc, sp, col, light, hi, lo, lo, 1, top, hi);
            });
        }
    }
}

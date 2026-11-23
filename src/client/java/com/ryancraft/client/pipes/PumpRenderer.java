package com.ryancraft.client.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.RyanCraft;
import com.ryancraft.pipes.HandPumpBlockEntity;
import com.ryancraft.pipes.PumpBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** The moving parts of both pumps: the Archimedes screw and its intake tube, and the hand pump's handle. */
public class PumpRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, PumpRenderer.State> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("pump_parts"), "main");
    private static final Identifier TEX = RyanCraft.id("textures/entity/pump_parts.png");
    public static class State extends BlockEntityRenderState {
        boolean hand; float angle; int tube; Direction facing = Direction.NORTH;
        net.minecraft.client.renderer.texture.TextureAtlasSprite fluid; int fluidColour; float fill;
    }
    private final ModelPart screw, tube, handle;

    public PumpRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(LAYER);
        screw = root.getChild("screw"); tube = root.getChild("tube"); handle = root.getChild("handle");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        PartDefinition screw = root.addOrReplaceChild("screw", CubeListBuilder.create().texOffs(0, 0).addBox(-1, 2, -1, 2, 12, 2), PartPose.ZERO);
        for (int i = 0; i < 10; i++)                            // the spiral: blades climbing the shaft
            screw.addOrReplaceChild("blade" + i, CubeListBuilder.create().texOffs(0, 16).addBox(1, 0, -1, 5, 1, 2),
                    PartPose.offsetAndRotation(0, 2.6f + i * 1.1f, 0, 0, (float) Math.toRadians(i * 40), 0));
        root.addOrReplaceChild("tube", CubeListBuilder.create().texOffs(32, 0).addBox(-2, -16, -2, 4, 16, 4), PartPose.ZERO);
        PartDefinition handle = root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 32).addBox(-1, 0, -1, 2, 1, 14), PartPose.offset(0, 17, 0));
        handle.addOrReplaceChild("grip", CubeListBuilder.create().texOffs(32, 32).addBox(-1.5f, -0.5f, 11.5f, 3, 2, 2), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(T be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        long now = be.getLevel() == null ? 0 : be.getLevel().getGameTime();
        if (be instanceof PumpBlockEntity p) {
            s.hand = false; s.tube = p.tubeDepth;
            s.angle = p.prevSpin + (p.spin - p.prevSpin) * partial;      // spins after each pulse, and stops where it is
            s.fluid = null;                                               // the casing fills with what it's pumping
            if (p.tank.amount > 0 && !p.tank.variant.isBlank()) {
                s.fluid = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getSprite(p.tank.variant);
                s.fluidColour = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getColor(p.tank.variant, be.getLevel(), be.getBlockPos());
                s.fill = Math.min(1f, (float) p.tank.amount / (net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET * 4));
            }
        } else if (be instanceof HandPumpBlockEntity h) {
            s.hand = true; s.facing = h.spout();
            float t = now - h.lastStroke + partial;
            s.angle = t < 10 ? (float) Math.sin(t / 10f * Math.PI) * 0.45f : 0f;   // the handle dips and comes back
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        var rt = RenderTypes.entityCutout(TEX);
        // Model parts are shared by every pump and drawn later: never move the parts, put each pump's motion in its pose.
        if (!s.hand && s.fluid != null) {
            var sp = s.fluid; int col = s.fluidColour, light = s.lightCoords; float y1 = (2.01f + 11.98f * s.fill) / 16;
            out.submitCustomGeometry(pose, RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS),
                    (p, vc) -> FluidBoxes.box(p, vc, sp, col, light, 2.01f / 16, 2.01f / 16, 2.01f / 16, 13.99f / 16, y1, 13.99f / 16));
        }
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        if (s.hand) {
            pose.mulPose(Axis.YP.rotationDegrees(180 - s.facing.toYRot()));   // turn with the spout
            pose.translate(0, 17 / 16f, 0); pose.mulPose(Axis.XP.rotation(-s.angle)); pose.translate(0, -17 / 16f, 0);
            out.submitModelPart(handle, pose, rt, s.lightCoords, OverlayTexture.NO_OVERLAY, null);
        } else {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotation(-s.angle));                            // threads climb, carrying the water up
            out.submitModelPart(screw, pose, rt, s.lightCoords, OverlayTexture.NO_OVERLAY, null);
            pose.popPose();
            if (s.tube > 1) {
                pose.pushPose();
                pose.scale(1, s.tube - 1, 1);
                out.submitModelPart(tube, pose, rt, s.lightCoords, OverlayTexture.NO_OVERLAY, null);
                pose.popPose();
            }
        }
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen() { return true; }
}

package com.ryancraft.client.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import com.ryancraft.pipes.EngineBlock;
import com.ryancraft.pipes.EngineBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
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
import net.minecraft.world.phys.Vec3;

/**
 * The engine, drawn the old way: a base plate, a trunk, and a piston plate that pumps up and down while it runs.
 * The trunk glows blue when idle and green when working.
 */
public class EngineRenderer implements BlockEntityRenderer<EngineBlockEntity, EngineRenderer.State> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("engine"), "main");
    public static class State extends BlockEntityRenderState {
        EngineBlock.Kind kind; Direction facing; boolean running; float stroke;
    }
    private final ModelPart base, trunk, piston;

    public EngineRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(LAYER);
        base = root.getChild("base"); trunk = root.getChild("trunk"); piston = root.getChild("piston");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-8, 0, -8, 16, 4, 16), PartPose.ZERO);
        root.addOrReplaceChild("piston", CubeListBuilder.create().texOffs(0, 20).addBox(-8, 4, -8, 16, 4, 16), PartPose.ZERO);
        root.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(64, 0).addBox(-4, 4, -4, 8, 12, 8), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(EngineBlockEntity be, State s, float partial, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, cam, crumbling);
        s.kind = be.getBlockState().getBlock() instanceof EngineBlock e ? e.kind : EngineBlock.Kind.REDSTONE;
        s.facing = be.getBlockState().getValue(EngineBlock.FACING);
        s.running = be.getBlockState().getValue(EngineBlock.RUNNING);
        if (be.getLevel() != null) {                                 // light from around the engine, not inside it
            int best = s.lightCoords;
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                int l = net.minecraft.client.renderer.LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(d));
                if ((l & 0xFFFF) + (l >> 16) > (best & 0xFFFF) + (best >> 16)) best = l;
            }
            s.lightCoords = best;
        }
        float t = be.getLevel() == null ? 0 : be.getLevel().getGameTime() + partial;
        float period = s.kind == EngineBlock.Kind.REDSTONE ? 40f : 16f;
        s.stroke = s.running ? (float) (0.5 - 0.5 * Math.cos(t / period * Math.PI * 2)) : 0f;
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        Identifier tex = RyanCraft.id("textures/entity/engine/" + (s.kind == EngineBlock.Kind.REDSTONE ? "redstone" : "stirling") + (s.running ? "_hot" : "") + ".png");
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(s.facing.getRotation());                 // the piston pushes toward what it powers
        pose.translate(0, -0.5, 0);
        piston.y = s.stroke * 8f;
        var rt = RenderTypes.entityCutout(tex);
        out.submitModelPart(base, pose, rt, s.lightCoords, OverlayTexture.NO_OVERLAY, null);
        out.submitModelPart(trunk, pose, rt, s.lightCoords, OverlayTexture.NO_OVERLAY, null);
        out.submitModelPart(piston, pose, rt, s.lightCoords, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }
}

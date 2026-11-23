package com.ryancraft.client.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.RyanCraft;
import com.ryancraft.combat.CannonEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** A cannon on its carriage: two wheels, a trail resting on the ground, and the barrel on its trunnions, raised to the aim. */
public class CannonRenderer extends EntityRenderer<CannonEntity, CannonRenderer.State> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("cannon"), "main");
    private static final Identifier TEX = RyanCraft.id("textures/entity/cannon.png");

    public static class State extends EntityRenderState { float yaw, pitch, recoil; }

    public static class Model extends EntityModel<State> {
        private final ModelPart barrel;
        public Model(ModelPart root) { super(root); barrel = root.getChild("barrel"); }

        public static LayerDefinition create() {
            MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("carriage", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-5, 16, -7, 10, 4, 16)                          // the bed
                    .texOffs(0, 44).addBox(-3, 19, 7, 6, 5, 6), PartPose.ZERO);            // the trail, down on the ground
            root.addOrReplaceChild("wheels", CubeListBuilder.create()
                    .texOffs(0, 24).addBox(-7.5f, 15, -6, 2, 9, 9)
                    .texOffs(24, 24).addBox(5.5f, 15, -6, 2, 9, 9)
                    .texOffs(44, 24).addBox(-8, 18.5f, -2, 16, 2, 2), PartPose.ZERO);      // the axle
            root.addOrReplaceChild("barrel", CubeListBuilder.create()
                    .texOffs(64, 0).addBox(-2.5f, -2.5f, -14, 5, 5, 20)
                    .texOffs(64, 28).addBox(-3, -3, -15, 6, 6, 2)                          // the muzzle swell
                    .texOffs(84, 28).addBox(-3, -3, 2, 6, 6, 4)                            // the breech
                    .texOffs(104, 28).addBox(-1, -1, 6, 2, 2, 2)                           // the cascabel knob
                    .texOffs(64, 40).addBox(-4.5f, -1, -1, 9, 2, 2), PartPose.offset(0, 14, -1));   // trunnions
            return LayerDefinition.create(mesh, 128, 64);
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            barrel.xRot = s.pitch * Mth.DEG_TO_RAD;
            barrel.z += s.recoil * 2.5f;
        }
    }

    private final Model model;
    public CannonRenderer(EntityRendererProvider.Context ctx) { super(ctx); model = new Model(ctx.bakeLayer(LAYER)); shadowRadius = 0.7f; }
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(CannonEntity e, State s, float partial) {
        super.extractRenderState(e, s, partial);
        s.yaw = e.getYRot(partial); s.pitch = e.getXRot(partial); s.recoil = e.recoil(partial);
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState cam) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180 - s.yaw));
        pose.scale(-1, -1, 1);
        pose.translate(0, -1.501, 0);
        out.submitModel(model, s, pose, model.renderType(TEX), s.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, s.outlineColor, null);
        pose.popPose();
        super.submit(s, pose, out, cam);
    }
}

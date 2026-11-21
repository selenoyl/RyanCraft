package com.ryancraft.client.model;

import com.ryancraft.RyanCraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** Wild turkey: long neck, red wattle, big fanned tail. */
public class TurkeyModel extends BoxyModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("turkey"), "main");
    private final ModelPart rightLeg, leftLeg, rightWing, leftWing, tail;

    public TurkeyModel(ModelPart root) {
        super(root);
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightWing = root.getChild("right_wing");
        this.leftWing = root.getChild("left_wing");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.5f, -6, -1.5f, 3, 6, 3)     // neck
                        .texOffs(12, 0).addBox(-2, -9, -3, 4, 4, 4)          // head
                        .texOffs(28, 0).addBox(-1, -7.5f, -5, 2, 1, 2)       // beak
                        .texOffs(28, 4).addBox(-0.5f, -6.5f, -4, 1, 3, 1),   // wattle
                PartPose.offset(0, 11, -4));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-4, -4, -5, 8, 8, 10),
                PartPose.offset(0, 14, 0));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 46).addBox(-8, -12, 0, 16, 12, 1),
                PartPose.offsetAndRotation(0, 12, 4.5f, -0.3f, 0, 0));
        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(36, 16).addBox(-1, 0, -4, 1, 6, 8),
                PartPose.offset(-4, 10.5f, 0));
        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(36, 16).mirror().addBox(0, 0, -4, 1, 6, 8),
                PartPose.offset(4, 10.5f, 0));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(36, 0).addBox(-0.5f, 0, -0.5f, 1, 6, 1);
        root.addOrReplaceChild("right_leg", leg, PartPose.offset(-1.5f, 18, 1));
        root.addOrReplaceChild("left_leg", leg, PartPose.offset(1.5f, 18, 1));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.rightLeg.xRot = swing(state, false);
        this.leftLeg.xRot = swing(state, true);
        // Head bobs forward with each step, like a real bird walk
        this.head.z += Mth.sin(state.walkAnimationPos * 0.6662f * 2) * 1.2f * state.walkAnimationSpeed;
        // Strut: tail fan breathes slightly
        this.tail.xRot += Mth.sin(state.ageInTicks * 0.05f) * 0.04f;
        float flap = state.walkAnimationSpeed > 0.6f ? Mth.sin(state.ageInTicks * 1.3f) * 0.5f : 0;
        this.rightWing.zRot = Math.abs(flap);
        this.leftWing.zRot = -Math.abs(flap);
    }
}

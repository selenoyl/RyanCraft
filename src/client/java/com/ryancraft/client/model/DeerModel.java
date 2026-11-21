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

public class DeerModel extends BoxyModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("deer"), "main");
    private final ModelPart rightHind, leftHind, rightFront, leftFront;

    public DeerModel(ModelPart root) {
        super(root);
        this.rightHind = root.getChild("right_hind_leg");
        this.leftHind = root.getChild("left_hind_leg");
        this.rightFront = root.getChild("right_front_leg");
        this.leftFront = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2, -7, -2, 4, 9, 4)      // neck
                        .texOffs(16, 0).addBox(-3, -10, -6, 6, 5, 7)    // skull
                        .texOffs(42, 0).addBox(-2, -8, -9, 4, 3, 3)     // snout
                        .texOffs(56, 0).addBox(-3, -16, -2, 1, 6, 1)    // antlers
                        .texOffs(56, 0).mirror().addBox(2, -16, -2, 1, 6, 1)
                        .texOffs(56, 8).mirror(false).addBox(-6, -15, -2, 3, 1, 1)
                        .texOffs(56, 8).mirror().addBox(3, -15, -2, 3, 1, 1),
                PartPose.offset(0, 8, -7));
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 18).addBox(-4, -4, -8, 8, 8, 16),
                PartPose.offset(0, 11, 0));
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(48, 30).addBox(-1, 0, 0, 2, 3, 1),
                PartPose.offsetAndRotation(0, -4, 8, 0.4f, 0, 0));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(48, 18).addBox(-1.5f, 0, -1.5f, 3, 9, 3);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-2.5f, 15, 6));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(2.5f, 15, 6));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-2.5f, 15, -6));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(2.5f, 15, -6));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.rightHind.xRot = swing(state, false);
        this.leftHind.xRot = swing(state, true);
        this.rightFront.xRot = swing(state, true);
        this.leftFront.xRot = swing(state, false);
    }
}

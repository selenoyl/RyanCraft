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

public class BoarModel extends BoxyModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("boar"), "main");
    private final ModelPart rightHind, leftHind, rightFront, leftFront;

    public BoarModel(ModelPart root) {
        super(root);
        this.rightHind = root.getChild("right_hind_leg");
        this.leftHind = root.getChild("left_hind_leg");
        this.rightFront = root.getChild("right_front_leg");
        this.leftFront = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -6, 8, 8, 6),
                PartPose.offset(0, 12, -8));
        head.addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(28, 0).addBox(-2, 0, -9, 4, 3, 3),
                PartPose.ZERO);
        head.addOrReplaceChild("tusks",
                CubeListBuilder.create().texOffs(42, 0).addBox(-3, 0, -8, 1, 3, 1)
                        .texOffs(42, 0).mirror().addBox(2, 0, -8, 1, 3, 1),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 20).addBox(-5, -5, -8, 10, 9, 16),
                PartPose.offset(0, 13, 0));
        body.addOrReplaceChild("bristles",
                CubeListBuilder.create().texOffs(28, 6).addBox(-1, -7, -7, 2, 2, 12),
                PartPose.ZERO);
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 48).addBox(-2, 0, -2, 4, 7, 4);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-3, 17, 5));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(3, 17, 5));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-3, 17, -5));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(3, 17, -5));
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

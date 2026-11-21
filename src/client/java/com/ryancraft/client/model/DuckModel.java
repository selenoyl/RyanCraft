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

public class DuckModel extends BoxyModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("duck"), "main");
    private final ModelPart rightLeg, leftLeg, rightWing, leftWing;

    public DuckModel(ModelPart root) {
        super(root);
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightWing = root.getChild("right_wing");
        this.leftWing = root.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2, -5, -2, 4, 5, 3),
                PartPose.offset(0, 17, -3));
        head.addOrReplaceChild("bill",
                CubeListBuilder.create().texOffs(14, 0).addBox(-1.5f, -2, -4, 3, 1, 2),
                PartPose.ZERO);
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 9).addBox(-3, -3, -4, 6, 5, 8),
                PartPose.offset(0, 19, 0));
        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 22).addBox(-1, 0, -3, 1, 3, 6),
                PartPose.offset(-3, 16, 0));
        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 22).mirror().addBox(0, 0, -3, 1, 3, 6),
                PartPose.offset(3, 16, 0));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(24, 0).addBox(-0.5f, 0, -0.5f, 1, 3, 1);
        root.addOrReplaceChild("right_leg", leg, PartPose.offset(-1.5f, 21, 1));
        root.addOrReplaceChild("left_leg", leg, PartPose.offset(1.5f, 21, 1));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.rightLeg.xRot = swing(state, false);
        this.leftLeg.xRot = swing(state, true);
        float flap = state.walkAnimationSpeed * (Mth.sin(state.ageInTicks * 0.9f) * 0.3f + 0.2f);
        this.rightWing.zRot = flap;
        this.leftWing.zRot = -flap;
    }
}

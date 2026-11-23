package com.ryancraft.client.mudling;

import com.ryancraft.RyanCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** A mudling: a chunky little soldier with a big head and a hat of its biome's stuff (64x32 texture). */
public class MudlingModel extends EntityModel<MudlingRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("mudling"), "main");
    private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;

    public MudlingModel(ModelPart root) {
        super(root);
        head = root.getChild("head"); body = root.getChild("body");
        rightArm = root.getChild("right_arm"); leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg"); leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -6, -3, 6, 6, 6)
                .texOffs(24, 0).addBox(-3.5f, -7, -3.5f, 7, 2, 7), PartPose.offset(0, 15, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 12).addBox(-3, 0, -1.5f, 6, 5, 3), PartPose.offset(0, 15, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(18, 12).addBox(-1, 0, -1, 2, 5, 2), PartPose.offset(-4, 15, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(26, 12).addBox(-1, 0, -1, 2, 5, 2), PartPose.offset(4, 15, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 20).addBox(-1, 0, -1, 2, 4, 2), PartPose.offset(-1.5f, 20, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(8, 20).addBox(-1, 0, -1, 2, 4, 2), PartPose.offset(1.5f, 20, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(MudlingRenderState s) {
        super.setupAnim(s);
        head.yRot = s.yRot * Mth.DEG_TO_RAD; head.xRot = s.xRot * Mth.DEG_TO_RAD;
        float sw = Mth.cos(s.walkAnimationPos * 0.9f) * 1.2f * s.walkAnimationSpeed;
        rightLeg.xRot = sw; leftLeg.xRot = -sw;
        rightArm.xRot = -sw * 0.8f; leftArm.xRot = sw * 0.8f;
        if (s.attackTime > 0) {                                      // a swing with the right arm
            float a = Mth.sin(s.attackTime * Mth.PI);
            rightArm.xRot = -1.8f + a * 1.2f;
        }
        rightArm.zRot = 0.08f; leftArm.zRot = -0.08f;
    }
}

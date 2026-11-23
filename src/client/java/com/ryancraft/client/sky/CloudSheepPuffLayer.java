package com.ryancraft.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;

/** The cloud sheep's extra puff: a second, much fuller fleece over the usual one, tinted to the sheep's wool colour. */
public class CloudSheepPuffLayer extends RenderLayer<SheepRenderState, SheepModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("cloud_sheep_puff"), "main");
    private static final Identifier TEXTURE = RyanCraft.id("textures/entity/cloud_sheep_puff.png");
    private final SheepFurModel model;

    public CloudSheepPuffLayer(RenderLayerParent<SheepRenderState, SheepModel> parent, EntityModelSet models) {
        super(parent);
        model = new SheepFurModel(models.bakeLayer(LAYER));
    }

    /** Vanilla's fleece boxes, puffed out much further. */
    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3f, -4f, -4f, 6f, 6f, 6f, new CubeDeformation(1.8f)), PartPose.offset(0f, 6f, -8f));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4f, -10f, -7f, 8f, 16f, 6f, new CubeDeformation(4.4f)),
                PartPose.offsetAndRotation(0f, 5f, 2f, (float) Math.PI / 2f, 0f, 0f));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2f, 0f, -2f, 4f, 6f, 4f, new CubeDeformation(1.1f));
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-3f, 12f, 7f));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(3f, 12f, 7f));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-3f, 12f, -5f));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(3f, 12f, -5f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector out, int light, SheepRenderState s, float yRot, float xRot) {
        if (s.isSheared || s.isBaby || s.isInvisible) return;
        out.submitModel(model, s, pose, model.renderType(TEXTURE), light, LivingEntityRenderer.getOverlayCoords(s, 0f), s.getWoolColor(), null, s.outlineColor, null);
    }
}

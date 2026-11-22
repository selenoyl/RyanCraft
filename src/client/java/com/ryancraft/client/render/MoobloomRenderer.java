package com.ryancraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.RyanCraft;
import com.ryancraft.entity.Moobloom;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/** The vanilla cow model in sunny yellow, with (old-school) dandelions on its back and head (placed like a mooshroom's mushrooms). */
public class MoobloomRenderer extends CowRenderer {
    private static final Identifier TEXTURE = RyanCraft.id("textures/entity/moobloom.png");

    public MoobloomRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.addLayer(new Flowers(this));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EntityRenderer<Moobloom, ?> create(EntityRendererProvider.Context ctx) { return (EntityRenderer) new MoobloomRenderer(ctx); }

    @Override public Identifier getTextureLocation(CowRenderState state) { return TEXTURE; }

    private static class Flowers extends RenderLayer<CowRenderState, CowModel> {
        Flowers(RenderLayerParent<CowRenderState, CowModel> parent) { super(parent); }

        @Override
        public void submit(PoseStack pose, SubmitNodeCollector out, int light, CowRenderState state, float yRot, float xRot) {
            if (state.isBaby || state.isInvisible) return;
            BlockState flower = net.minecraft.world.level.block.Blocks.DANDELION.defaultBlockState();
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0f);
            float[][] back = {{0.2f, -0.35f, 0.5f, -48f}, {-0.15f, -0.35f, 0.05f, 30f}, {0.15f, -0.35f, -0.35f, 75f}};
            for (float[] b : back) {
                pose.pushPose();
                pose.translate(b[0], b[1], b[2]);
                pose.mulPose(Axis.YP.rotationDegrees(b[3]));
                pose.scale(-0.8f, -0.8f, 0.8f);
                pose.translate(-0.5f, -0.5f, -0.5f);
                out.submitBlock(pose, flower, light, overlay, state.outlineColor);
                pose.popPose();
            }
            pose.pushPose();
            this.getParentModel().getHead().translateAndRotate(pose);
            pose.translate(0.0f, -0.7f, -0.2f);
            pose.mulPose(Axis.YP.rotationDegrees(-78f));
            pose.scale(-0.8f, -0.8f, 0.8f);
            pose.translate(-0.5f, -0.5f, -0.5f);
            out.submitBlock(pose, flower, light, overlay, state.outlineColor);
            pose.popPose();
        }
    }
}

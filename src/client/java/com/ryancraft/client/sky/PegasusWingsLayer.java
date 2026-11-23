package com.ryancraft.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.Identifier;

/** Draws the pegasus's wings over the vanilla horse. */
public class PegasusWingsLayer extends RenderLayer<HorseRenderState, HorseModel> {
    private static final Identifier TEXTURE = RyanCraft.id("textures/entity/pegasus_wings.png");
    private final PegasusWingsModel adult, baby;

    public PegasusWingsLayer(RenderLayerParent<HorseRenderState, HorseModel> parent, EntityModelSet models) {
        super(parent);
        adult = new PegasusWingsModel(models.bakeLayer(PegasusWingsModel.LAYER));
        baby = new PegasusWingsModel(models.bakeLayer(PegasusWingsModel.BABY_LAYER));
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector out, int light, HorseRenderState s, float yRot, float xRot) {
        if (s.isInvisible) return;
        PegasusWingsModel m = s.isBaby ? baby : adult;
        out.submitModel(m, s, pose, m.renderType(TEXTURE), light, LivingEntityRenderer.getOverlayCoords(s, 0f), -1, null, s.outlineColor, null);
    }
}

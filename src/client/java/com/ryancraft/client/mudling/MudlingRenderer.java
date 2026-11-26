package com.ryancraft.client.mudling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import com.ryancraft.entity.Mudling;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

/** Draws a mudling in its biome kind's (and rare variant's) texture, with a sash in its team colour once dyed. */
public class MudlingRenderer extends MobRenderer<Mudling, MudlingRenderState, MudlingModel> {
    private static final Identifier SASH = RyanCraft.id("textures/entity/mudling/sash.png");

    public MudlingRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MudlingModel(ctx.bakeLayer(MudlingModel.LAYER)), 0.25f);
        addLayer(new SashLayer(this));
    }

    @Override public MudlingRenderState createRenderState() { return new MudlingRenderState(); }

    @Override
    public void extractRenderState(Mudling m, MudlingRenderState s, float partial) {
        super.extractRenderState(m, s, partial);
        s.kind = m.kind().ordinal(); s.variant = m.variant(); s.team = m.team(); s.wins = m.wins();
        s.attackTime = m.getAttackAnim(partial);
    }

    @Override
    public Identifier getTextureLocation(MudlingRenderState s) {
        String v = Mudling.VARIANTS[Math.floorMod(s.variant, Mudling.VARIANTS.length)];
        return RyanCraft.id("textures/entity/mudling/" + Mudling.Kind.of(s.kind).id + (v.isEmpty() ? "" : "_" + v) + ".png");
    }

    @Override
    protected void scale(MudlingRenderState s, PoseStack pose) {
        float k = 0.6f * (1 + Math.min(5, s.wins) * 0.04f);              // veterans stand a little taller
        pose.scale(k, k, k);
    }

    /** The team sash: the model again with a white sash texture, tinted to the team's dye colour. */
    static class SashLayer extends RenderLayer<MudlingRenderState, MudlingModel> {
        SashLayer(RenderLayerParent<MudlingRenderState, MudlingModel> parent) { super(parent); }

        @Override
        public void submit(PoseStack pose, SubmitNodeCollector out, int light, MudlingRenderState s, float yRot, float xRot) {
            if (s.team < 0) return;
            int colour = DyeColor.byId(s.team).getTextureDiffuseColor() | 0xFF000000;
            // drawn in a later pass (as vanilla does the wolf collar): same geometry as the body, so in the same pass it
            // could land underneath, depending on which texture the batch happened to draw first
            out.order(1).submitModel(getParentModel(), s, pose, getParentModel().renderType(SASH), light, OverlayTexture.NO_OVERLAY, colour, null, s.outlineColor, null);
        }
    }
}

package com.ryancraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import com.ryancraft.client.model.DeerModel;
import com.ryancraft.entity.Deer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Deer with antlers that match the animal's age stage (and the rare drop-tine rack). */
public class DeerRenderer extends MobRenderer<Deer, DeerRenderState, DeerModel> {
    /** Antler colour variants: normal, dark, chocolate. */
    private static final Identifier[] TEXTURES = {RyanCraft.id("textures/entity/deer.png"),
            RyanCraft.id("textures/entity/deer_dark.png"), RyanCraft.id("textures/entity/deer_chocolate.png")};

    public DeerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DeerModel(ctx.bakeLayer(DeerModel.LAYER)), 0.6f);
    }

    @Override public DeerRenderState createRenderState() { return new DeerRenderState(); }

    @Override
    public void extractRenderState(Deer deer, DeerRenderState state, float partialTick) {
        super.extractRenderState(deer, state, partialTick);
        state.antlers = deer.isBaby() ? -1 : deer.getAntlers();     // fawns are bare
        state.dropTine = deer.hasDropTine();
        state.rack = deer.getRack();
        state.antlerColor = deer.getAntlerColor();
    }

    @Override public Identifier getTextureLocation(DeerRenderState state) {
        return TEXTURES[Math.floorMod(state.antlerColor, TEXTURES.length)];
    }

    @Override
    protected void scale(DeerRenderState state, PoseStack pose) {
        if (state.isBaby) pose.scale(0.5f, 0.5f, 0.5f);
    }
}

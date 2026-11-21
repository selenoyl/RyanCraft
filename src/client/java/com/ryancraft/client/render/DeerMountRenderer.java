package com.ryancraft.client.render;

import com.ryancraft.RyanCraft;
import com.ryancraft.client.model.DeerMountModel;
import com.ryancraft.entity.DeerMount;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class DeerMountRenderer extends MobRenderer<DeerMount, DeerRenderState, DeerMountModel> {
    private static final Identifier[] TEXTURES = {RyanCraft.id("textures/entity/deer_mount.png"),
            RyanCraft.id("textures/entity/deer_mount_dark.png"), RyanCraft.id("textures/entity/deer_mount_chocolate.png")};

    public DeerMountRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DeerMountModel(ctx.bakeLayer(DeerMountModel.LAYER)), 0.0f);
    }

    @Override public DeerRenderState createRenderState() { return new DeerRenderState(); }

    @Override
    public void extractRenderState(DeerMount mount, DeerRenderState state, float partialTick) {
        super.extractRenderState(mount, state, partialTick);
        state.antlers = mount.getAntlers();
        state.dropTine = mount.hasDropTine();
        state.rack = mount.getRack();
        state.antlerColor = mount.getAntlerColor();
        state.hasRedOverlay = false;
    }

    @Override
    public Identifier getTextureLocation(DeerRenderState state) {
        return TEXTURES[Math.floorMod(state.antlerColor, TEXTURES.length)];
    }

    /** Hung trophies read better a bit larger than life. */
    @Override
    protected void scale(DeerRenderState state, com.mojang.blaze3d.vertex.PoseStack pose) {
        pose.scale(1.5f, 1.5f, 1.5f);
    }

    @Override
    protected boolean shouldShowName(DeerMount entity, double distance) { return false; }
}

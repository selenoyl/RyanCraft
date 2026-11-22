package com.ryancraft.client.render;

import com.ryancraft.RyanCraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** The deer model and antlers with the reindeer's pale coat. */
public class ReindeerRenderer extends DeerRenderer {
    private static final Identifier[] TEXTURES = {RyanCraft.id("textures/entity/reindeer.png"),
            RyanCraft.id("textures/entity/reindeer_dark.png"), RyanCraft.id("textures/entity/reindeer_chocolate.png")};

    public ReindeerRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public Identifier getTextureLocation(DeerRenderState state) {
        return TEXTURES[Math.floorMod(state.antlerColor, TEXTURES.length)];
    }
}

package com.ryancraft.client.skeleton;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

/** Vanilla skeleton renderer that picks one of the variant textures per skeleton. */
public class VariantSkeletonRenderer extends SkeletonRenderer {
    public static class State extends SkeletonRenderState {
        public int variant;
    }

    public VariantSkeletonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Skeleton skeleton, SkeletonRenderState state, float partialTick) {
        super.extractRenderState(skeleton, state, partialTick);
        if (state instanceof State s) s.variant = SkeletonVariants.pick(skeleton.getUUID());
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return state instanceof State s ? SkeletonVariants.texture(s.variant) : super.getTextureLocation(state);
    }
}

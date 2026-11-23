package com.ryancraft.client.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Adds our layers (pegasus wings, cloud sheep puff) to vanilla renderers built for our variants. */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {
    @SuppressWarnings("rawtypes")
    @Invoker("addLayer") boolean ryancraft$addLayer(RenderLayer layer);
}

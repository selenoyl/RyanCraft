package com.ryancraft.client.mixin;

import com.ryancraft.RyanCraft;
import com.ryancraft.entity.Zebra;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zebras use the vanilla horse renderer (model, saddle, armour) with their own coat. */
@Mixin(HorseRenderer.class)
public abstract class HorseRendererZebraMixin {
    @Unique private static final RenderStateDataKey<Boolean> RYANCRAFT_ZEBRA = RenderStateDataKey.create(() -> "ryancraft:zebra");
    @Unique private static final Identifier RYANCRAFT_ZEBRA_TEXTURE = RyanCraft.id("textures/entity/zebra.png");

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/Horse;Lnet/minecraft/client/renderer/entity/state/HorseRenderState;F)V", at = @At("TAIL"))
    private void ryancraft$markZebra(Horse horse, HorseRenderState state, float partialTick, CallbackInfo ci) {
        ((FabricRenderState) state).setData(RYANCRAFT_ZEBRA, horse instanceof Zebra);
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/HorseRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void ryancraft$zebraCoat(HorseRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if (Boolean.TRUE.equals(((FabricRenderState) state).getData(RYANCRAFT_ZEBRA))) cir.setReturnValue(RYANCRAFT_ZEBRA_TEXTURE);
    }
}

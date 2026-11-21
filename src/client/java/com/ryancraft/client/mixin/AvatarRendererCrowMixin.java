package com.ryancraft.client.mixin;

import com.ryancraft.client.render.CrowOnShoulderLayer;
import com.ryancraft.entity.CrowShoulders;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hands the shoulder-crow flags to the render state. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererCrowMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void ryancraft$crows(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        byte b = avatar instanceof CrowShoulders c ? c.ryancraft$crowShoulders() : 0;
        ((FabricRenderState) state).setData(CrowOnShoulderLayer.CROWS, b);
    }
}

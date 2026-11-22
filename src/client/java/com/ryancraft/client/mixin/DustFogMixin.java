package com.ryancraft.client.mixin;

import com.ryancraft.client.DustStormClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Dust storms close the fog in and tint it sandy (rusty over the badlands). */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class DustFogMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void ryancraft$dustFog(FogData data, Camera camera, ClientLevel level, float renderDistance, DeltaTracker delta, CallbackInfo ci) {
        float k = DustStormClient.intensity;
        if (k <= 0.001f) return;
        data.environmentalStart = lerp(data.environmentalStart, 0f, k);
        data.environmentalEnd = lerp(data.environmentalEnd, 44f, k);
        data.skyEnd = lerp(data.skyEnd, 44f, k);
        data.cloudEnd = lerp(data.cloudEnd, 44f, k);
    }

    @Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
    private void ryancraft$dustColour(ClientLevel level, Camera camera, int renderDistance, float partial, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(DustStormClient.fogColour(cir.getReturnValueI()));
    }

    private static float lerp(float a, float b, float k) { return a + (b - a) * k; }
}

package com.ryancraft.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Dedicated servers override enforceSecureProfile, so they need their own hook. */
@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {
    @Inject(method = "enforceSecureProfile", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noSecureProfile(CallbackInfoReturnable<Boolean> cir) {
        if (RyanConfig.get().disableChatReporting) {
            cir.setReturnValue(false);
        }
    }
}

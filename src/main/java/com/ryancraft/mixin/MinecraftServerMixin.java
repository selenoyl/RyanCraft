package com.ryancraft.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Servers running the mod don't demand signed (reportable) chat. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "enforceSecureProfile", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noSecureProfile(CallbackInfoReturnable<Boolean> cir) {
        if (RyanConfig.get().disableChatReporting) {
            cir.setReturnValue(false);
        }
    }
}

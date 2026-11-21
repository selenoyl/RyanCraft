package com.ryancraft.client.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    /** No client-side profanity filtering. */
    @Inject(method = "isTextFilteringEnabled", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noTextFilter(CallbackInfoReturnable<Boolean> cir) {
        if (RyanConfig.get().disableProfanityFilter) {
            cir.setReturnValue(false);
        }
    }
}

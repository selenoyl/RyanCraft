package com.ryancraft.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No server-side text filtering of chat, signs or book text. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "isTextFilteringEnabled", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noTextFilter(CallbackInfoReturnable<Boolean> cir) {
        if (RyanConfig.get().disableProfanityFilter) {
            cir.setReturnValue(false);
        }
    }
}

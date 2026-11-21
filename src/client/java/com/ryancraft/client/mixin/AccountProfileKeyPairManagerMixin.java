package com.ryancraft.client.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.client.multiplayer.AccountProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Never fetch a chat-signing key pair. Without one, chat goes out unsigned and can't be
 * attached to a player report.
 */
@Mixin(AccountProfileKeyPairManager.class)
public abstract class AccountProfileKeyPairManagerMixin {
    @Inject(method = "prepareKeyPair", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noKeyPair(CallbackInfoReturnable<Object> cir) {
        if (RyanConfig.get().disableChatReporting) {
            cir.setReturnValue(CompletableFuture.completedFuture(Optional.empty()));
        }
    }
}

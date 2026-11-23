package com.ryancraft.mixin;

import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No "experimental" warning on worlds in the world list just because they have the moon (see StableWorldgenMixin). */
@Mixin(LevelSummary.class)
public abstract class StableLevelSummaryMixin {
    @Inject(method = "isExperimental", at = @At("HEAD"), cancellable = true)
    private void ryancraft$notExperimental(CallbackInfoReturnable<Boolean> cir) { cir.setReturnValue(false); }
}

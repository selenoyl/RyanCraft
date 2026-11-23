package com.ryancraft.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The moon is a data-driven dimension, which vanilla marks "experimental", so every world would open with the
 * "Worlds using Experimental Settings are not supported" warning. Our worldgen is stable: report it as such.
 * (TerraBlender used to hide this warning for us.)
 */
@Mixin(PrimaryLevelData.class)
public abstract class StableWorldgenMixin {
    @Inject(method = "worldGenSettingsLifecycle", at = @At("HEAD"), cancellable = true)
    private void ryancraft$stable(CallbackInfoReturnable<Lifecycle> cir) { cir.setReturnValue(Lifecycle.stable()); }
}

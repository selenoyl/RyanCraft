package com.ryancraft.mixin;

import com.ryancraft.moon.MoonPhysics;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Low gravity on the moon, for everything that falls (see MoonPhysics). */
@Mixin(Entity.class)
public abstract class MoonGravityMixin {
    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void ryancraft$moonGravity(CallbackInfoReturnable<Double> cir) {
        double k = MoonPhysics.gravityScale((Entity) (Object) this);
        if (k != 1.0) cir.setReturnValue(cir.getReturnValue() * k);
    }
}

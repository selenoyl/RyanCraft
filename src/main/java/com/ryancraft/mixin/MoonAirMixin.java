package com.ryancraft.mixin;

import com.ryancraft.moon.MoonPhysics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No air on the moon: breath runs out as if underwater, then you suffocate; falls hurt less in the low gravity. */
@Mixin(LivingEntity.class)
public abstract class MoonAirMixin {
    @Shadow public abstract boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount);

    /** Out of water your breath would come back; on the moon it keeps running down instead. */
    @Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noAir(int air, CallbackInfoReturnable<Integer> cir) {
        if (MoonPhysics.breathless((LivingEntity) (Object) this)) cir.setReturnValue(air - 1);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void ryancraft$suffocate(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!MoonPhysics.breathless(self)) return;
        if (self.getAirSupply() >= self.getMaxAirSupply()) self.setAirSupply(self.getMaxAirSupply() - 1);   // vanilla only "refills" below full
        if (!(self.level() instanceof ServerLevel level) || self.getAirSupply() > -20) return;
        self.setAirSupply(0);
        hurtServer(level, self.damageSources().drown(), 2.0f);
    }

    @Inject(method = "calculateFallDamage", at = @At("RETURN"), cancellable = true)
    private void ryancraft$softLanding(double distance, float multiplier, CallbackInfoReturnable<Integer> cir) {
        if (MoonPhysics.onMoon((LivingEntity) (Object) this)) cir.setReturnValue((int) Math.floor(cir.getReturnValue() * MoonPhysics.LIVING_GRAVITY));
    }
}

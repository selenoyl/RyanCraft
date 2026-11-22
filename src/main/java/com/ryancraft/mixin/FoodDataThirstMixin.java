package com.ryancraft.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ryancraft.thirst.Thirst;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Thirst: with the thirst bar empty you stop healing naturally, just as you do when starving. */
@Mixin(FoodData.class)
public abstract class FoodDataThirstMixin {
    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private boolean ryancraft$noRegenWhenParched(ServerPlayer player, float amount) {
        return !Thirst.enabled() || Thirst.get(player).level() > 0;
    }
}

package com.ryancraft.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WeatherCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** /weather clear also ends a dust storm. */
@Mixin(WeatherCommand.class)
public abstract class WeatherClearMixin {
    @Inject(method = "setClear", at = @At("HEAD"))
    private static void ryancraft$clearDust(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        com.ryancraft.weather.DustStorms.clearCommand(source.getServer());
    }
}

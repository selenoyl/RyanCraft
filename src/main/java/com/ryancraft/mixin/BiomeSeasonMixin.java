package com.ryancraft.mixin;

import com.ryancraft.season.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Seasons: temperate biomes cool down through autumn into winter (snow, ice), and their grass and leaves change colour. */
@Mixin(Biome.class)
public abstract class BiomeSeasonMixin {
    @Inject(method = "getTemperature(Lnet/minecraft/core/BlockPos;I)F", at = @At("RETURN"), cancellable = true)
    private void ryancraft$seasonTemperature(BlockPos pos, int seaLevel, CallbackInfoReturnable<Float> cir) {
        float off = Seasons.temperatureOffset((Biome) (Object) this);
        if (off != 0) cir.setReturnValue(cir.getReturnValueF() + off);
    }

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void ryancraft$seasonGrass(double x, double z, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Seasons.grass((Biome) (Object) this, cir.getReturnValueI()));
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void ryancraft$seasonFoliage(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Seasons.foliage((Biome) (Object) this, cir.getReturnValueI()));
    }
}

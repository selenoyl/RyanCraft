package com.ryancraft.mixin;

import com.ryancraft.deep.DeepCaves;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla floods every cave below y=-54 with lava. In a world deepened by the Abyss (min y below -64), keep that band
 * down to -64, then let the Abyss's caverns hold ordinary aquifer water and air, with lava only near its floor.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class DeepFluidMixin {
    @Inject(method = "createFluidPicker", at = @At("RETURN"), cancellable = true)
    private static void ryancraft$abyssFluids(NoiseGeneratorSettings settings, CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        if (settings.noiseSettings().minY() >= DeepCaves.TOP) return;
        Aquifer.FluidPicker vanilla = cir.getReturnValue();
        Aquifer.FluidStatus water = vanilla.computeFluid(0, settings.seaLevel(), 0);
        cir.setReturnValue((x, y, z) -> y < DeepCaves.TOP && y >= DeepCaves.LAVA_BELOW ? water : vanilla.computeFluid(x, y, z));
    }
}

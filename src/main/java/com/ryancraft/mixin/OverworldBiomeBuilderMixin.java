package com.ryancraft.mixin;

import com.mojang.datafixers.util.Pair;
import com.ryancraft.worldgen.BiomeVariants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Our in-house TerraBlender: where vanilla gives a patch of climate to a biome listed in {@link BiomeVariants#SLOTS},
 * split the patch into a grid (humidity across, weirdness down) and give each cell to the next biome in the list.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
    @Unique private static final ThreadLocal<Boolean> ryancraft$splitting = ThreadLocal.withInitial(() -> false);

    @Shadow
    private void addSurfaceBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out, Climate.Parameter temperature, Climate.Parameter humidity,
                                 Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter weirdness, float offset, ResourceKey<Biome> biome) {
        throw new AssertionError();
    }

    @Inject(method = "addSurfaceBiome", at = @At("HEAD"), cancellable = true)
    private void ryancraft$variants(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out, Climate.Parameter temperature, Climate.Parameter humidity,
                                    Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter weirdness, float offset,
                                    ResourceKey<Biome> biome, CallbackInfo ci) {
        if (ryancraft$splitting.get()) return;
        List<ResourceKey<Biome>> cells = BiomeVariants.SLOTS.get(biome);
        if (cells == null || cells.size() < 2) return;
        int n = cells.size(), cols = (int) Math.ceil(Math.sqrt(n)), rows = (int) Math.ceil(n / (double) cols);
        if (humidity.max() - humidity.min() < cols) cols = 1;
        if (weirdness.max() - weirdness.min() < rows) rows = 1;
        if (cols * rows < 2) return;                                         // nothing to split: leave it vanilla
        ci.cancel();
        ryancraft$splitting.set(true);
        try {
            int k = 0;
            for (int r = 0; r < rows; r++) {
                Climate.Parameter w = ryancraft$part(weirdness, r, rows);
                for (int c = 0; c < cols; c++) {
                    Climate.Parameter h = ryancraft$part(humidity, (c + r) % cols, cols);  // offset each row, so cells don't line up in columns
                    addSurfaceBiome(out, temperature, h, continentalness, erosion, w, offset, cells.get(k++ % n));
                }
            }
        } finally {
            ryancraft$splitting.set(false);
        }
    }

    /**
     * List the Abyss's caverns with the overworld (so their features generate), at a depth no real point ever reaches:
     * climate never picks them; DeepBiomeMixin hands them out below y=-64.
     */
    @Inject(method = "addUndergroundBiomes", at = @At("TAIL"))
    private void ryancraft$abyssCaverns(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out, CallbackInfo ci) {
        Climate.Parameter any = Climate.Parameter.span(-1.0f, 1.0f);
        for (ResourceKey<Biome> b : com.ryancraft.deep.DeepCaves.BIOMES)
            out.accept(Pair.of(Climate.parameters(any, any, any, any, Climate.Parameter.point(9.0f), any, 0.0f), b));
    }

    @Unique
    private static Climate.Parameter ryancraft$part(Climate.Parameter p, int i, int of) {
        long span = p.max() - p.min();
        return new Climate.Parameter(p.min() + span * i / of, i == of - 1 ? p.max() : p.min() + span * (i + 1) / of);
    }
}

package com.ryancraft.mixin;

import com.ryancraft.deep.DeepCaves;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Below y=-64 (only in a world the Abyss has deepened) the overworld's biome is one of the Abyss's five caverns, picked
 * from the world's own climate at that spot, so they come in big seed-dependent regions: cold ground is frozen, wet
 * ground overgrown, dry ground toxic, and the rest splits by erosion into the hollow dark and plain abyssal caverns.
 * A belt of plain abyssal cavern always separates one special kind from another.
 * Only biome sources that list the caverns (the overworld: see OverworldBiomeBuilderMixin) are affected.
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class DeepBiomeMixin {
    @Unique private Map<ResourceKey<Biome>, Holder<Biome>> ryancraft$caverns;
    /** How wide (in climate units) the belt of plain abyssal cavern between two kinds is. */
    @Unique private static final float BORDER = 0.07f;

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
    private void ryancraft$abyss(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (QuartPos.toBlock(y) >= DeepCaves.TOP) return;
        if (ryancraft$caverns == null) {
            Map<ResourceKey<Biome>, Holder<Biome>> m = new HashMap<>();
            for (Holder<Biome> h : ((BiomeSource) (Object) this).possibleBiomes())
                h.unwrapKey().filter(DeepCaves.BIOMES::contains).ifPresent(k -> m.put(k, h));
            ryancraft$caverns = m;
        }
        if (ryancraft$caverns.size() < DeepCaves.BIOMES.size()) return;
        Climate.TargetPoint p = sampler.sample(x, QuartPos.fromBlock(0), z);
        float t = Climate.unquantizeCoord(p.temperature()), h = Climate.unquantizeCoord(p.humidity()), e = Climate.unquantizeCoord(p.erosion());
        // each special cavern sits well inside its own climate, with a wide margin of plain abyssal cavern round it, so
        // you always pass through ordinary rocky caves between one kind and the next
        float m = BORDER;
        ResourceKey<Biome> k = t < -0.5f - m ? DeepCaves.FROZEN_CAVERNS
                : t < -0.5f + m ? DeepCaves.ABYSSAL_CAVERNS
                : h > 0.35f + m ? DeepCaves.SUNKEN_WILDS
                : h > 0.35f - m ? DeepCaves.ABYSSAL_CAVERNS
                : h < -0.1f - m ? DeepCaves.TOXIC_CAVES
                : h < -0.1f + m ? DeepCaves.ABYSSAL_CAVERNS
                : e < -0.35f - m ? DeepCaves.HOLLOW_DARK
                : DeepCaves.ABYSSAL_CAVERNS;
        cir.setReturnValue(ryancraft$caverns.get(k));
    }
}

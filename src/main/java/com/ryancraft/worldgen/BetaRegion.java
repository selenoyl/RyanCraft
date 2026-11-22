package com.ryancraft.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * A slice of the overworld that uses vanilla's climate layout but swaps in the Beta biomes,
 * so they sit where you'd expect: Rainforest among jungles, Tundra among snowfields, etc.
 */
public class BetaRegion extends Region {
    public BetaRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(Biomes.JUNGLE, RyanBiomes.RAINFOREST);
            builder.replaceBiome(Biomes.SPARSE_JUNGLE, RyanBiomes.RAINFOREST);
            builder.replaceBiome(Biomes.FOREST, RyanBiomes.SEASONAL_FOREST);
            // (the Autumn Forest no longer generates: every temperate forest turns with the seasons instead)
            builder.replaceBiome(Biomes.PLAINS, RyanBiomes.SHRUBLAND);
            builder.replaceBiome(Biomes.SAVANNA, RyanBiomes.SHRUBLAND);
            builder.replaceBiome(Biomes.SNOWY_PLAINS, RyanBiomes.TUNDRA);
        });
    }
}

package com.ryancraft.worldgen;

import com.ryancraft.RyanCraft;
import com.ryancraft.entity.RyanEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.function.Predicate;

/**
 * The four Beta-era biomes. Their terrain features, colours and climate live in
 * data/ryancraft/worldgen/biome/*.json; their mob spawns are added here with Fabric API
 * (so the JSON stays simple), and {@link RyanTerraBlender} places them in the overworld.
 */
public final class RyanBiomes {
    public static final ResourceKey<Biome> RAINFOREST = key("rainforest");
    public static final ResourceKey<Biome> SEASONAL_FOREST = key("seasonal_forest");
    public static final ResourceKey<Biome> SHRUBLAND = key("shrubland");
    public static final ResourceKey<Biome> TUNDRA = key("tundra");

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, RyanCraft.id(name));
    }

    public static void init() {
        var all = BiomeSelectors.includeByKey(RAINFOREST, SEASONAL_FOREST, SHRUBLAND, TUNDRA);
        var temperate = BiomeSelectors.includeByKey(RAINFOREST, SEASONAL_FOREST, SHRUBLAND);

        // Standard overworld night-time monsters and cave life
        spawn(all, MobCategory.AMBIENT, EntityType.BAT, 10, 8, 8);
        spawn(all, MobCategory.UNDERGROUND_WATER_CREATURE, EntityType.GLOW_SQUID, 10, 4, 6);
        spawn(all, MobCategory.MONSTER, EntityType.SPIDER, 100, 4, 4);
        spawn(all, MobCategory.MONSTER, EntityType.ZOMBIE, 95, 4, 4);
        spawn(all, MobCategory.MONSTER, EntityType.ZOMBIE_VILLAGER, 5, 1, 1);
        spawn(all, MobCategory.MONSTER, EntityType.CREEPER, 100, 4, 4);
        spawn(all, MobCategory.MONSTER, EntityType.SLIME, 100, 4, 4);
        spawn(all, MobCategory.MONSTER, EntityType.ENDERMAN, 10, 1, 4);
        spawn(all, MobCategory.MONSTER, EntityType.WITCH, 5, 1, 1);
        spawn(temperate, MobCategory.MONSTER, EntityType.SKELETON, 100, 4, 4);

        // Farm animals in the temperate ones
        spawn(temperate, MobCategory.CREATURE, EntityType.SHEEP, 12, 4, 4);
        spawn(temperate, MobCategory.CREATURE, EntityType.PIG, 10, 4, 4);
        spawn(temperate, MobCategory.CREATURE, EntityType.CHICKEN, 10, 4, 4);
        spawn(temperate, MobCategory.CREATURE, EntityType.COW, 8, 4, 4);

        // Biome specials
        var rainforest = BiomeSelectors.includeByKey(RAINFOREST);
        spawn(rainforest, MobCategory.CREATURE, EntityType.PARROT, 40, 1, 2);
        spawn(rainforest, MobCategory.MONSTER, EntityType.OCELOT, 2, 1, 3);
        spawn(BiomeSelectors.includeByKey(SHRUBLAND), MobCategory.CREATURE, EntityType.HORSE, 5, 2, 6);
        var tundra = BiomeSelectors.includeByKey(TUNDRA);
        spawn(tundra, MobCategory.CREATURE, EntityType.RABBIT, 10, 2, 3);
        spawn(tundra, MobCategory.CREATURE, EntityType.POLAR_BEAR, 1, 1, 2);
        spawn(tundra, MobCategory.MONSTER, EntityType.SKELETON, 20, 4, 4);
        spawn(tundra, MobCategory.MONSTER, EntityType.STRAY, 80, 4, 4);

        // New animals: in the new biomes and in fitting vanilla ones
        spawn(BiomeSelectors.includeByKey(SEASONAL_FOREST, RAINFOREST,
                        Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA),
                MobCategory.CREATURE, RyanEntities.BOAR, 8, 2, 4);
        spawn(BiomeSelectors.includeByKey(SEASONAL_FOREST, SHRUBLAND, TUNDRA,
                        Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.MEADOW),
                MobCategory.CREATURE, RyanEntities.DEER, 8, 2, 4);
        spawn(BiomeSelectors.includeByKey(RAINFOREST, SHRUBLAND,
                        Biomes.RIVER, Biomes.PLAINS, Biomes.SWAMP, Biomes.MANGROVE_SWAMP),
                MobCategory.CREATURE, RyanEntities.DUCK, 10, 2, 4);
    }

    private static void spawn(Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> where,
                              MobCategory category, EntityType<?> type, int weight, int min, int max) {
        BiomeModifications.addSpawn(where, category, type, weight, min, max);
    }

    private RyanBiomes() {}
}

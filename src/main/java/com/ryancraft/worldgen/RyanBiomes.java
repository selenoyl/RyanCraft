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
        kentucky();
        bigGame();
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

        // Wild turkeys: hardwood forests, forest edges and meadows, like back home in Kentucky
        spawn(BiomeSelectors.includeByKey(SEASONAL_FOREST, SHRUBLAND,
                        Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST,
                        Biomes.FLOWER_FOREST, Biomes.MEADOW, Biomes.PLAINS, Biomes.WINDSWEPT_FOREST),
                MobCategory.CREATURE, RyanEntities.TURKEY, 9, 4, 7);
    }

    /** Native Kentucky wildlife. */
    public static void kentucky() {
        var hardwoods = new net.minecraft.resources.ResourceKey[]{SEASONAL_FOREST, Biomes.FOREST, Biomes.BIRCH_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.FLOWER_FOREST, Biomes.WINDSWEPT_FOREST};
        spawn(sel(hardwoods, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                MobCategory.CREATURE, RyanEntities.BLACK_BEAR, 3, 1, 1);
        spawn(sel(hardwoods, Biomes.SWAMP, RAINFOREST), MobCategory.CREATURE, RyanEntities.RACCOON, 6, 1, 3);
        spawn(sel(new net.minecraft.resources.ResourceKey[]{Biomes.PLAINS, Biomes.MEADOW, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.FOREST, SHRUBLAND, SEASONAL_FOREST}),
                MobCategory.CREATURE, RyanEntities.SKUNK, 4, 1, 2);
        spawn(BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.SWAMP, Biomes.MANGROVE_SWAMP), MobCategory.CREATURE, RyanEntities.MINK, 6, 1, 2);
        spawn(sel(hardwoods, Biomes.SWAMP, Biomes.PLAINS, RAINFOREST), MobCategory.CREATURE, RyanEntities.OPOSSUM, 5, 1, 3);
        spawn(BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.SWAMP, Biomes.TAIGA), MobCategory.CREATURE, RyanEntities.BEAVER, 6, 1, 3);
        spawn(sel(hardwoods, Biomes.PLAINS, Biomes.MEADOW, Biomes.SAVANNA, SHRUBLAND), MobCategory.CREATURE, RyanEntities.SQUIRREL, 10, 2, 3);
        spawn(sel(hardwoods, Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.SWAMP, SHRUBLAND, RAINFOREST),
                MobCategory.CREATURE, RyanEntities.SONGBIRD, 10, 2, 3);
        // Coyotes: open country and woodland edges, in loose packs
        spawn(sel(new net.minecraft.resources.ResourceKey[]{Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.SAVANNA,
                        Biomes.BADLANDS, Biomes.FOREST, Biomes.BIRCH_FOREST, SHRUBLAND, SEASONAL_FOREST}),
                MobCategory.CREATURE, RyanEntities.COYOTE, 5, 3, 5);
        spawn(sel(hardwoods, Biomes.TAIGA), MobCategory.CREATURE, RyanEntities.CHIPMUNK, 8, 2, 3);
        var creeks = BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
        spawn(creeks, MobCategory.WATER_AMBIENT, RyanEntities.BLUEGILL, 15, 3, 6);
        spawn(creeks, MobCategory.WATER_AMBIENT, RyanEntities.CRAPPIE, 10, 3, 5);
        spawn(creeks, MobCategory.WATER_AMBIENT, RyanEntities.LARGEMOUTH_BASS, 8, 1, 3);
        spawn(creeks, MobCategory.WATER_AMBIENT, RyanEntities.CHANNEL_CATFISH, 6, 1, 2);
    }

    /** Big game and predators of the wider continent. */
    public static void bigGame() {
        // Elk: mountain meadows, open woods and high grassland
        spawn(BiomeSelectors.includeByKey(Biomes.MEADOW, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_HILLS, Biomes.CHERRY_GROVE,
                Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, SHRUBLAND, Biomes.SAVANNA_PLATEAU), MobCategory.CREATURE, RyanEntities.ELK, 6, 3, 6);
        // Moose: cold wetlands and spruce forest, usually alone or a cow with her calf
        spawn(BiomeSelectors.includeByKey(Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.SWAMP, TUNDRA,
                Biomes.SNOWY_PLAINS), MobCategory.CREATURE, RyanEntities.MOOSE, 4, 1, 2);
        // Pronghorn: open grassland and shrubland
        spawn(BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, SHRUBLAND,
                Biomes.BADLANDS, Biomes.WOODED_BADLANDS), MobCategory.CREATURE, RyanEntities.PRONGHORN, 7, 3, 6);
        // Grizzlies: the high country, rarely
        spawn(BiomeSelectors.includeByKey(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_FOREST, Biomes.MEADOW, Biomes.GROVE, Biomes.TAIGA), MobCategory.CREATURE, RyanEntities.GRIZZLY_BEAR, 2, 1, 1);
        // Bobcats: almost anywhere with cover, but always scarce
        spawn(sel(new net.minecraft.resources.ResourceKey[]{Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.TAIGA,
                Biomes.SWAMP, Biomes.SAVANNA, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WINDSWEPT_FOREST, SEASONAL_FOREST, SHRUBLAND}),
                MobCategory.CREATURE, RyanEntities.BOBCAT, 2, 1, 1);
    }

    @SafeVarargs
    private static Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> sel(ResourceKey<Biome>[] base, ResourceKey<Biome>... more) {
        java.util.List<ResourceKey<Biome>> all = new java.util.ArrayList<>(java.util.List.of(base));
        all.addAll(java.util.List.of(more));
        return BiomeSelectors.includeByKey(all);
    }

    private static void spawn(Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> where,
                              MobCategory category, EntityType<?> type, int weight, int min, int max) {
        BiomeModifications.addSpawn(where, category, type, weight, min, max);
    }

    private RyanBiomes() {}
}

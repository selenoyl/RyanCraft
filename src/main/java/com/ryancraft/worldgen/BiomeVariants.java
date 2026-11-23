package com.ryancraft.worldgen;

import com.ryancraft.RyanCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Our own biome placement (in place of TerraBlender). Vanilla builds the overworld's biome map by handing each biome a
 * patch of climate; where it hands out one of the biomes below, {@code OverworldBiomeBuilderMixin} slices that patch
 * into a small grid and gives each cell to the next entry in its list. Vanilla biomes keep most cells, so they all still
 * generate; our variants (and the Beta biomes) take the rest, sitting where their parent would have been.
 */
public final class BiomeVariants {
    // the eastern forest variants
    public static final ResourceKey<Biome> OAK_HICKORY_WOODS = key("oak_hickory_woods");
    public static final ResourceKey<Biome> MAPLE_BEECH_FOREST = key("maple_beech_forest");
    public static final ResourceKey<Biome> TULIP_POPLAR_COVE = key("tulip_poplar_cove");
    public static final ResourceKey<Biome> SYCAMORE_BOTTOMS = key("sycamore_bottoms");
    public static final ResourceKey<Biome> CEDAR_GLADE = key("cedar_glade");
    public static final ResourceKey<Biome> REDBUD_DOGWOOD_GLADE = key("redbud_dogwood_glade");
    public static final ResourceKey<Biome> OLD_FIELD = key("old_field");
    public static final ResourceKey<Biome> BUCKEYE_HOLLOW = key("buckeye_hollow");

    /** Vanilla biome -> the cells its patch of climate is split into (repeat a vanilla biome to give it more of them). */
    public static final Map<ResourceKey<Biome>, List<ResourceKey<Biome>>> SLOTS = new LinkedHashMap<>();
    /** Each variant's vanilla parent: it shares the parent's mob spawns. */
    public static final Map<ResourceKey<Biome>, ResourceKey<Biome>> PARENT = new LinkedHashMap<>();

    static {
        slots(Biomes.FOREST, Biomes.FOREST, OAK_HICKORY_WOODS, Biomes.FOREST, MAPLE_BEECH_FOREST, RyanBiomes.SEASONAL_FOREST, TULIP_POPLAR_COVE, Biomes.FOREST);
        slots(Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST, MAPLE_BEECH_FOREST, Biomes.BIRCH_FOREST);
        slots(Biomes.FLOWER_FOREST, Biomes.FLOWER_FOREST, REDBUD_DOGWOOD_GLADE);
        slots(Biomes.DARK_FOREST, Biomes.DARK_FOREST, BUCKEYE_HOLLOW, Biomes.DARK_FOREST);
        slots(Biomes.PLAINS, Biomes.PLAINS, OLD_FIELD, Biomes.PLAINS, RyanBiomes.SHRUBLAND, Biomes.PLAINS, CEDAR_GLADE);
        slots(Biomes.SWAMP, Biomes.SWAMP, SYCAMORE_BOTTOMS);
        slots(Biomes.SAVANNA, Biomes.SAVANNA, RyanBiomes.SHRUBLAND, Biomes.SAVANNA);
        slots(Biomes.JUNGLE, Biomes.JUNGLE, RyanBiomes.RAINFOREST, Biomes.JUNGLE);
        slots(Biomes.SPARSE_JUNGLE, Biomes.SPARSE_JUNGLE, RyanBiomes.RAINFOREST);
        slots(Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, RyanBiomes.TUNDRA, Biomes.SNOWY_PLAINS);

        PARENT.put(OAK_HICKORY_WOODS, Biomes.FOREST); PARENT.put(MAPLE_BEECH_FOREST, Biomes.FOREST); PARENT.put(TULIP_POPLAR_COVE, Biomes.FOREST);
        PARENT.put(BUCKEYE_HOLLOW, Biomes.DARK_FOREST); PARENT.put(REDBUD_DOGWOOD_GLADE, Biomes.FLOWER_FOREST);
        PARENT.put(SYCAMORE_BOTTOMS, Biomes.SWAMP); PARENT.put(CEDAR_GLADE, Biomes.PLAINS); PARENT.put(OLD_FIELD, Biomes.PLAINS);
    }

    @SafeVarargs
    private static void slots(ResourceKey<Biome> vanilla, ResourceKey<Biome>... cells) { SLOTS.put(vanilla, List.of(cells)); }

    private static ResourceKey<Biome> key(String name) { return ResourceKey.create(Registries.BIOME, RyanCraft.id(name)); }

    private BiomeVariants() {}
}

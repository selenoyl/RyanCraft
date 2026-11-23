package com.ryancraft.metal;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The new metals: silver, lead, tin, zinc, aluminum and uranium. Each has stone and deepslate ore, raw metal and its
 * block, ingot, nugget and block. Ore veins: data/ryancraft/worldgen/placed_feature/ore_<metal>.json (lead also gets
 * extra veins in swamps).
 */
public final class Metals {
    public record Metal(String name, MapColor colour, int light, Block ore, Block deepOre, Block rawBlock, Block block, Item raw, Item ingot, Item nugget) {}
    public static final Map<String, Metal> ALL = new LinkedHashMap<>();
    private static final List<Item> BLOCK_ITEMS = new ArrayList<>(), ITEMS = new ArrayList<>();

    static {
        metal("silver", MapColor.METAL, 0); metal("lead", MapColor.COLOR_GRAY, 0); metal("tin", MapColor.METAL, 0);
        metal("zinc", MapColor.METAL, 0); metal("aluminum", MapColor.SNOW, 0); metal("uranium", MapColor.COLOR_LIGHT_GREEN, 3);
    }

    public static Metal get(String n) { return ALL.get(n); }

    private static void metal(String n, MapColor colour, int light) {
        Block ore = block(n + "_ore", p -> new DropExperienceBlock(UniformInt.of(0, 2), p), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops().strength(3.0f, 3.0f).lightLevel(s -> light));
        Block deep = block("deepslate_" + n + "_ore", p -> new DropExperienceBlock(UniformInt.of(0, 2), p), BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                .requiresCorrectToolForDrops().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE).lightLevel(s -> light));
        Block rawBlock = block("raw_" + n + "_block", Block::new, BlockBehaviour.Properties.of().mapColor(colour).requiresCorrectToolForDrops().strength(5.0f, 6.0f));
        BlockBehaviour.Properties bp = BlockBehaviour.Properties.of();
        if (n.equals("silver")) bp.randomTicks();
        Block block = block(n + "_block", Block::new, bp.mapColor(colour).requiresCorrectToolForDrops().strength(5.0f, 6.0f)
                .sound(SoundType.METAL).lightLevel(s -> light));     // silver's block tarnishes (Silver)
        ALL.put(n, new Metal(n, colour, light, ore, deep, rawBlock, block, item("raw_" + n), item(n + "_ingot"), item(n + "_nugget")));
    }

    private static Block block(String name, java.util.function.Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        BLOCK_ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    private static Item item(String name) {
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, ik, new Item(new Item.Properties().setId(ik)));
        ITEMS.add(i); return i;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> BLOCK_ITEMS.forEach(e::accept));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> ITEMS.forEach(e::accept));
        for (String n : ALL.keySet())
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
                    ResourceKey.create(Registries.PLACED_FEATURE, RyanCraft.id("ore_" + n)));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, com.ryancraft.worldgen.BiomeVariants.SYCAMORE_BOTTOMS),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, RyanCraft.id("ore_lead_swamp")));
    }

    private Metals() {}
}

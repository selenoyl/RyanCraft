package com.ryancraft.deco;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
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
import java.util.List;
import java.util.function.Function;

/**
 * Ruby and sapphire, the emerald's cousins: ore in stone and deepslate, the gem, and its block (beacon base and payment).
 * Rubies are found under deserts, badlands and savannas; sapphires under snowy and frozen lands (our tundra too). Assets: tools/gems.py.
 */
public final class Gems {
    private static final List<Item> BLOCKS = new ArrayList<>(), ITEMS = new ArrayList<>();
    public static final Item RUBY = item("ruby"), SAPPHIRE = item("sapphire");

    static {
        gem("ruby", MapColor.COLOR_RED);
        gem("sapphire", MapColor.COLOR_BLUE);
    }

    private static void gem(String g, MapColor c) {
        block(g + "_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops().strength(3.0f, 3.0f));
        block("deepslate_" + g + "_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p), BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                .requiresCorrectToolForDrops().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE));
        block(g + "_block", Block::new, BlockBehaviour.Properties.of().mapColor(c).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL));
    }

    private static void block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        BLOCKS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
    }

    private static Item item(String name) {
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, ik, new Item(new Item.Properties().setId(ik)));
        ITEMS.add(i); return i;
    }

    public static void init() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_BADLANDS).or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA))
                        .or(BiomeSelectors.includeByKey(Biomes.DESERT)),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, RyanCraft.id("ore_ruby")));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA, Biomes.SNOWY_SLOPES,
                        Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.GROVE, Biomes.FROZEN_RIVER, Biomes.SNOWY_BEACH,
                        ResourceKey.create(Registries.BIOME, RyanCraft.id("tundra"))),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, RyanCraft.id("ore_sapphire")));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> BLOCKS.forEach(e::accept));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> ITEMS.forEach(e::accept));
    }

    private Gems() {}
}

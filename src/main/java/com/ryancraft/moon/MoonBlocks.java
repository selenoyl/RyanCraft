package com.ryancraft.moon;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The moon's blocks: moon dust (a pale, fine sand that falls), moonstone (the moon's bedrock-grey stone, with a cobbled,
 * polished and brick family), and meteor iron (ore in the moon's caves, raw, ingot and blocks). The ore glows faintly.
 */
public final class MoonBlocks {
    public static final List<Item> BLOCK_ITEMS = new ArrayList<>(), ITEMS = new ArrayList<>();

    public static final Block MOON_DUST, MOONSTONE, COBBLED_MOONSTONE, COBBLED_MOONSTONE_STAIRS, COBBLED_MOONSTONE_SLAB, COBBLED_MOONSTONE_WALL,
            POLISHED_MOONSTONE, MOONSTONE_BRICKS, CRACKED_MOONSTONE_BRICKS, CHISELED_MOONSTONE_BRICKS,
            MOONSTONE_BRICK_STAIRS, MOONSTONE_BRICK_SLAB, MOONSTONE_BRICK_WALL,
            METEOR_IRON_ORE, RAW_METEOR_IRON_BLOCK, METEOR_IRON_BLOCK,
            DEEP_MOONSTONE, COBBLED_DEEP_MOONSTONE, POLISHED_DEEP_MOONSTONE, DEEP_MOONSTONE_BRICKS, DEEP_METEOR_IRON_ORE;
    public static final Item RAW_METEOR_IRON, METEOR_IRON_INGOT;

    static {
        MOON_DUST = block("moon_dust", p -> new ColoredFallingBlock(new ColorRGBA(0xFFB9B8B2), p),
                BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.SNARE).strength(0.5f).sound(SoundType.SAND));
        MOONSTONE = block("moonstone", Block::new, stone());
        COBBLED_MOONSTONE = block("cobbled_moonstone", Block::new, stone().strength(2.0f, 6.0f));
        COBBLED_MOONSTONE_STAIRS = block("cobbled_moonstone_stairs", p -> new StairBlock(COBBLED_MOONSTONE.defaultBlockState(), p), stone().strength(2.0f, 6.0f));
        COBBLED_MOONSTONE_SLAB = block("cobbled_moonstone_slab", SlabBlock::new, stone().strength(2.0f, 6.0f));
        COBBLED_MOONSTONE_WALL = block("cobbled_moonstone_wall", WallBlock::new, stone().strength(2.0f, 6.0f).forceSolidOn());
        POLISHED_MOONSTONE = block("polished_moonstone", Block::new, stone());
        MOONSTONE_BRICKS = block("moonstone_bricks", Block::new, stone());
        CRACKED_MOONSTONE_BRICKS = block("cracked_moonstone_bricks", Block::new, stone());
        CHISELED_MOONSTONE_BRICKS = block("chiseled_moonstone_bricks", Block::new, stone());
        MOONSTONE_BRICK_STAIRS = block("moonstone_brick_stairs", p -> new StairBlock(MOONSTONE_BRICKS.defaultBlockState(), p), stone());
        MOONSTONE_BRICK_SLAB = block("moonstone_brick_slab", SlabBlock::new, stone());
        MOONSTONE_BRICK_WALL = block("moonstone_brick_wall", WallBlock::new, stone().forceSolidOn());
        METEOR_IRON_ORE = block("meteor_iron_ore", p -> new DropExperienceBlock(UniformInt.of(1, 3), p),
                stone().strength(3.5f, 4.0f).lightLevel(s -> 4));
        // deep moonstone: the moon's deepslate, below about y=32
        DEEP_MOONSTONE = block("deep_moonstone", net.minecraft.world.level.block.RotatedPillarBlock::new, deep().strength(3.0f, 6.0f));
        COBBLED_DEEP_MOONSTONE = block("cobbled_deep_moonstone", Block::new, deep().strength(3.5f, 6.0f));
        POLISHED_DEEP_MOONSTONE = block("polished_deep_moonstone", Block::new, deep().strength(3.5f, 6.0f));
        DEEP_MOONSTONE_BRICKS = block("deep_moonstone_bricks", Block::new, deep().strength(3.5f, 6.0f));
        DEEP_METEOR_IRON_ORE = block("deep_meteor_iron_ore", p -> new DropExperienceBlock(UniformInt.of(1, 3), p), deep().strength(5.0f, 4.0f).lightLevel(s -> 4));
        RAW_METEOR_IRON_BLOCK = block("raw_meteor_iron_block", Block::new,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0f, 6.0f));
        METEOR_IRON_BLOCK = block("meteor_iron_block", Block::new,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(6.0f, 8.0f).sound(SoundType.METAL));
        RAW_METEOR_IRON = item("raw_meteor_iron");
        METEOR_IRON_INGOT = item("meteor_iron_ingot");
    }

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5f, 6.0f);
    }

    private static BlockBehaviour.Properties deep() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE);
    }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        BLOCK_ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    private static Item item(String name) {
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, ik, new Item(new Item.Properties().setId(ik)));
        ITEMS.add(i);
        return i;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(e -> BLOCK_ITEMS.forEach(e::accept));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> ITEMS.forEach(e::accept));
    }
}

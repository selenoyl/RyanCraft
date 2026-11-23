package com.ryancraft.deco;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Building blocks cut from gem blocks: jade and lapis bricks (with stairs, slabs and walls) and carved chiseled blocks. */
public final class GemBlocks {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final Block JADE_BRICKS, JADE_BRICK_STAIRS, JADE_BRICK_SLAB, JADE_BRICK_WALL, CHISELED_JADE,
            LAPIS_BRICKS, LAPIS_BRICK_STAIRS, LAPIS_BRICK_SLAB, LAPIS_BRICK_WALL, CHISELED_LAPIS;

    static {
        JADE_BRICKS = block("jade_bricks", Block::new, gem(MapColor.EMERALD));
        JADE_BRICK_STAIRS = block("jade_brick_stairs", p -> new StairBlock(JADE_BRICKS.defaultBlockState(), p), gem(MapColor.EMERALD));
        JADE_BRICK_SLAB = block("jade_brick_slab", SlabBlock::new, gem(MapColor.EMERALD));
        JADE_BRICK_WALL = block("jade_brick_wall", WallBlock::new, gem(MapColor.EMERALD).forceSolidOn());
        CHISELED_JADE = block("chiseled_jade", Block::new, gem(MapColor.EMERALD));
        LAPIS_BRICKS = block("lapis_bricks", Block::new, gem(MapColor.LAPIS));
        LAPIS_BRICK_STAIRS = block("lapis_brick_stairs", p -> new StairBlock(LAPIS_BRICKS.defaultBlockState(), p), gem(MapColor.LAPIS));
        LAPIS_BRICK_SLAB = block("lapis_brick_slab", SlabBlock::new, gem(MapColor.LAPIS));
        LAPIS_BRICK_WALL = block("lapis_brick_wall", WallBlock::new, gem(MapColor.LAPIS).forceSolidOn());
        CHISELED_LAPIS = block("chiseled_lapis", Block::new, gem(MapColor.LAPIS));
    }

    private static BlockBehaviour.Properties gem(MapColor c) {
        return BlockBehaviour.Properties.of().mapColor(c).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0f, 3.0f);
    }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(e -> ITEMS.forEach(e::accept));
    }

    private GemBlocks() {}
}

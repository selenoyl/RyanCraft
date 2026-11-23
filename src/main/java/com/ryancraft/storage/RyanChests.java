package com.ryancraft.storage;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Special chests. Obsidian: 27 slots, blast-proof and wither-proof. Diamond: 54 slots in one block, glass walls on a
 * diamond frame so you can see what's inside. Lead glass: 27 slots behind leaded windows, contents on show.
 * Assets: tools/chests.py; drawn by client/RyanChestRenderer.
 */
public final class RyanChests {
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final Block OBSIDIAN = chest(RyanChestBlock.Kind.OBSIDIAN, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
            .requiresCorrectToolForDrops().strength(22.5f, 1200f).sound(SoundType.STONE));
    public static final Block DIAMOND = chest(RyanChestBlock.Kind.DIAMOND, BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND)
            .requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.GLASS).noOcclusion());
    public static final Block LEAD_GLASS = chest(RyanChestBlock.Kind.LEAD_GLASS, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
            .requiresCorrectToolForDrops().strength(3.0f, 6.0f).sound(SoundType.GLASS).noOcclusion());
    public static final BlockEntityType<RyanChestBlockEntity> ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("special_chest"),
            FabricBlockEntityTypeBuilder.create(RyanChestBlockEntity::new, OBSIDIAN, DIAMOND, LEAD_GLASS).build());

    private static Block chest(RyanChestBlock.Kind kind, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(kind.id));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, new RyanChestBlock(kind, props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(kind.id));
        Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        BLOCKS.add(b);
        return b;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e -> BLOCKS.forEach(e::accept));
    }

    private RyanChests() {}
}

package com.ryancraft.flora;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

/**
 * The original 1.2.5 rose. Split a rose bush in the crafting grid for two roses; one rose makes one red dye
 * (as two roses from a bush match the bush's own two dye).
 */
public final class RyanFlora {
    public static final Block ROSE = block("rose", p -> new FlowerBlock(MobEffects.REGENERATION, 4.0f, p), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
    public static final Item ROSE_ITEM = item("rose", p -> new BlockItem(ROSE, p), new Item.Properties().useBlockDescriptionPrefix());
    public static final Block POTTED_ROSE = block("potted_rose", p -> new FlowerPotBlock(ROSE, p), Blocks.flowerPotProperties());

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, f.apply(props.setId(key)));
    }

    private static Item item(String name, Function<Item.Properties, Item> f, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(props.setId(key)));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> e.accept(ROSE_ITEM));
    }

    private RyanFlora() {}
}

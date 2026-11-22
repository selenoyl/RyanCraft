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

/** Wild flowers: the buttercup (grows on mooblooms and where they wander). */
public final class RyanFlora {
    /** Buttercups are mildly toxic: suspicious stew made with one gives a short dose of poison. */
    public static final Block BUTTERCUP = block("buttercup", p -> new FlowerBlock(MobEffects.POISON, 4.0f, p),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION));
    public static final Item BUTTERCUP_ITEM = item("buttercup", p -> new BlockItem(BUTTERCUP, p), new Item.Properties().useBlockDescriptionPrefix());
    public static final Block POTTED_BUTTERCUP = block("potted_buttercup", p -> new FlowerPotBlock(BUTTERCUP, p), Blocks.flowerPotProperties());

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, f.apply(props.setId(key)));
    }

    private static Item item(String name, Function<Item.Properties, Item> f, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(props.setId(key)));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> e.accept(BUTTERCUP_ITEM));
    }

    private RyanFlora() {}
}

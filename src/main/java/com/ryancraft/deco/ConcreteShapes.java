package com.ryancraft.deco;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Concrete stairs and slabs in all sixteen colours, cut from vanilla concrete (tools/concrete_shapes.py). */
public final class ConcreteShapes {
    public static final List<Item> ITEMS = new ArrayList<>();

    static {
        for (DyeColor c : DyeColor.values()) {
            Block base = BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(c.getName() + "_concrete"));
            block(c.getName() + "_concrete_stairs", p -> new StairBlock(base.defaultBlockState(), p), props(c));
            block(c.getName() + "_concrete_slab", SlabBlock::new, props(c));
        }
    }

    private static BlockBehaviour.Properties props(DyeColor c) {
        return BlockBehaviour.Properties.of().mapColor(c).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8f);
    }

    private static void block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COLORED_BLOCKS).register(e -> ITEMS.forEach(e::accept));
    }

    private ConcreteShapes() {}
}

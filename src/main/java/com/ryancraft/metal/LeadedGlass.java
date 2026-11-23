package com.ryancraft.metal;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Leaded glass: glass set in lead cames, clear or in any of the 16 dye colours, in two patterns (diamond quarrels and
 * squares; the stonecutter swaps them), as blocks and panes. Coloured leaded glass tints beacon beams like stained glass.
 * Plus a leaded glass door and trapdoor. Sturdier than glass: it drops itself.
 */
public final class LeadedGlass {
    public static final String[] PATTERNS = {"leaded_glass", "square_leaded_glass"};
    public static final List<Block> ALL = new ArrayList<>();
    private static final List<Item> ITEMS = new ArrayList<>();
    public static final Block DOOR, TRAPDOOR;

    static {
        for (String pattern : PATTERNS) {
            block(pattern, TransparentBlock::new, glass());
            block(pattern + "_pane", IronBarsBlock::new, glass());
            for (DyeColor c : DyeColor.values()) {
                block(c.getName() + "_" + pattern, p -> new StainedGlassBlock(c, p), glass().mapColor(c));
                block(c.getName() + "_" + pattern + "_pane", p -> new StainedGlassPaneBlock(c, p), glass().mapColor(c));
            }
        }
        DOOR = register("leaded_glass_door", p -> new DoorBlock(BlockSetType.COPPER, p), glass().strength(2.0f).pushReaction(PushReaction.DESTROY), true);
        TRAPDOOR = block("leaded_glass_trapdoor", p -> new TrapDoorBlock(BlockSetType.COPPER, p), glass().strength(2.0f));
    }

    private static BlockBehaviour.Properties glass() {
        return BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.6f).sound(SoundType.GLASS).noOcclusion()
                .isValidSpawn(Blocks::never).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false);
    }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        return register(name, f, props, false);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props, boolean tall) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item.Properties ip = new Item.Properties().setId(ik).useBlockDescriptionPrefix();
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, tall ? new DoubleHighBlockItem(b, ip) : new BlockItem(b, ip)));
        ALL.add(b);
        return b;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COLORED_BLOCKS).register(e -> ITEMS.forEach(e::accept));
    }

    private LeadedGlass() {}
}

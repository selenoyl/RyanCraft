package com.ryancraft.pipes;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Pipes, engines, the pump, tanks and the Ender Terminal. */
public final class RyanPipes {
    public static final Map<PipeType, Block> PIPES = new EnumMap<>(PipeType.class);
    private static final List<Item> TAB = new ArrayList<>();
    static {
        for (PipeType t : PipeType.values()) {
            SoundType sound = t == PipeType.WOODEN ? SoundType.WOOD : t == PipeType.GOLD || t == PipeType.IRON ? SoundType.METAL : SoundType.STONE;
            PIPES.put(t, block(t.id + "_pipe", p -> new PipeBlock(t, p), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(0.25f).sound(sound).noOcclusion().forceSolidOff()));
        }
    }
    public static final Block REDSTONE_ENGINE = block("redstone_engine", p -> new EngineBlock(EngineBlock.Kind.REDSTONE, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD).noOcclusion()
                    .isRedstoneConductor((s, l, p) -> false));     // power an engine and only that engine runs, not its neighbours
    public static final Block STIRLING_ENGINE = block("stirling_engine", p -> new EngineBlock(EngineBlock.Kind.STIRLING, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE).noOcclusion().isRedstoneConductor((s, l, p) -> false)
                    .lightLevel(s -> s.getValue(EngineBlock.RUNNING) ? 13 : 0));     // glows like a lit furnace
    public static final Block PUMP = block("pump", PumpBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5f).sound(SoundType.METAL).noOcclusion());   // an open frame: don't hide the neighbours' faces
    public static final Block TANK = block("tank", TankBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.8f).sound(SoundType.GLASS).noOcclusion());
    public static final Block VAT = block("vat", VatBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD).noOcclusion());
    public static final Block HAND_PUMP = block("hand_pump", HandPumpBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.METAL).noOcclusion());
    public static final Block ENDER_TERMINAL = block("ender_terminal", EnderTerminalBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0f, 600f).sound(SoundType.STONE).lightLevel(s -> 7));
    public static final Item WRENCH = item("wrench", WrenchItem::new, new Item.Properties().stacksTo(1));

    public static final BlockEntityType<PipeBlockEntity> PIPE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("pipe"),
            FabricBlockEntityTypeBuilder.create(PipeBlockEntity::new, PIPES.values().toArray(Block[]::new)).build());
    public static final BlockEntityType<EngineBlockEntity> ENGINE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("engine"),
            FabricBlockEntityTypeBuilder.create(EngineBlockEntity::new, REDSTONE_ENGINE, STIRLING_ENGINE).build());
    public static final BlockEntityType<PumpBlockEntity> PUMP_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("pump"),
            FabricBlockEntityTypeBuilder.create(PumpBlockEntity::new, PUMP).build());
    public static final BlockEntityType<TankBlockEntity> TANK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("tank"),
            FabricBlockEntityTypeBuilder.create(TankBlockEntity::new, TANK).build());
    public static final BlockEntityType<VatBlockEntity> VAT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("vat"),
            FabricBlockEntityTypeBuilder.create(VatBlockEntity::new, VAT).build());
    public static final BlockEntityType<HandPumpBlockEntity> HAND_PUMP_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("hand_pump"),
            FabricBlockEntityTypeBuilder.create(HandPumpBlockEntity::new, HAND_PUMP).build());
    public static final BlockEntityType<EnderTerminalBlockEntity> TERMINAL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("ender_terminal"),
            FabricBlockEntityTypeBuilder.create(EnderTerminalBlockEntity::new, ENDER_TERMINAL).build());

    public static final MenuType<DiamondPipeMenu> DIAMOND_MENU = Registry.register(BuiltInRegistries.MENU, RyanCraft.id("diamond_pipe"),
            new MenuType<>(DiamondPipeMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuType<EnderTerminalMenu> TERMINAL_MENU = Registry.register(BuiltInRegistries.MENU, RyanCraft.id("ender_terminal"),
            new MenuType<>(EnderTerminalMenu::new, FeatureFlags.VANILLA_SET));

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, key, f.apply(props.setId(key)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        TAB.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    private static Item item(String name, Function<Item.Properties, Item> f, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, key, f.apply(props.setId(key)));
        TAB.add(i);
        return i;
    }

    public static void init() {
        FluidStorage.SIDED.registerForBlockEntity((be, dir) -> be.tank, TANK_ENTITY);
        FluidStorage.SIDED.registerForBlockEntity((be, dir) -> be.tank, PUMP_ENTITY);
        FluidStorage.SIDED.registerForBlockEntity((be, dir) -> be.tank, VAT_ENTITY);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, RyanCraft.id("pipes"), FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.ryancraft.pipes")).icon(() -> new ItemStack(PIPES.get(PipeType.GOLD)))
                .displayItems((params, out) -> TAB.forEach(out::accept)).build());
        TerminalPayloads.init();
    }

    private RyanPipes() {}
}

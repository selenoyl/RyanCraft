package com.ryancraft.fish;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** The fishing loop's things: the trophy plaque and the Angler's Logbook. */
public final class RyanFishing {
    public static final Block PLAQUE;
    public static final BlockEntityType<FishPlaqueBlockEntity> PLAQUE_ENTITY;
    public static final Item LOGBOOK;

    static {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id("fish_plaque"));
        PLAQUE = Registry.register(BuiltInRegistries.BLOCK, bk, new FishPlaqueBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .strength(1.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava().setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id("fish_plaque"));
        Item plaqueItem = Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(PLAQUE, new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        PLAQUE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("fish_plaque"),
                FabricBlockEntityTypeBuilder.create(FishPlaqueBlockEntity::new, PLAQUE).build());
        ResourceKey<Item> lk = ResourceKey.create(Registries.ITEM, RyanCraft.id("anglers_logbook"));
        LOGBOOK = Registry.register(BuiltInRegistries.ITEM, lk, new Item(new Item.Properties().stacksTo(1).setId(lk)) {
            @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide()) FishingLog.show(player);
                return InteractionResult.SUCCESS;
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> { e.accept(LOGBOOK); e.accept(plaqueItem); });
    }

    public static void init() { FishingLog.BESTS.getClass(); }

    private RyanFishing() {}
}

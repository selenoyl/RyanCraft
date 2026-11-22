package com.ryancraft.storage;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

/** Clay pots for food, and bowls and buckets you can set down. */
public final class RyanStorage {
    public static final Block CLAY_POT = block("clay_pot", ClayPotBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE).strength(0.6f).sound(SoundType.DECORATED_POT).noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final Item CLAY_POT_ITEM = item("clay_pot", p -> new BlockItem(CLAY_POT, p), new Item.Properties().stacksTo(16).useBlockDescriptionPrefix());
    public static final Item UNFIRED_CLAY_POT = item("unfired_clay_pot", Item::new, new Item.Properties().stacksTo(16));
    public static final BlockEntityType<ClayPotBlockEntity> CLAY_POT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            RyanCraft.id("clay_pot"), FabricBlockEntityTypeBuilder.create(ClayPotBlockEntity::new, CLAY_POT).build());
    public static final MenuType<PotMenu> POT_MENU = Registry.register(BuiltInRegistries.MENU, RyanCraft.id("clay_pot"),
            new MenuType<>(PotMenu::new, FeatureFlags.VANILLA_SET));

    public static final Block PLACED_BOWL = block("placed_bowl", PlacedBlocks.PlacedBowl::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).instabreak().sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final Block PLACED_BUCKET = block("placed_bucket", PlacedBlocks.PlacedBucket::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).strength(0.3f).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.DESTROY)
            .lightLevel(s -> s.getValue(PlacedBlocks.PlacedBucket.CONTENTS) == PlacedBlocks.BucketContents.LAVA ? 13 : 0));

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, f.apply(props.setId(key)));
    }

    private static Item item(String name, Function<Item.Properties, Item> f, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(props.setId(key)));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e -> { e.accept(CLAY_POT_ITEM); });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> e.accept(UNFIRED_CLAY_POT));

        // Sneak-right-click the top of a block with a bowl or bucket to set it down
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!player.isSecondaryUseActive() || hit.getDirection() != Direction.UP || player.isSpectator()) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            BlockState state;
            var bowl = PlacedBlocks.BowlContents.of(stack);
            var bucket = PlacedBlocks.BucketContents.of(stack);
            if (bowl != null) state = PLACED_BOWL.defaultBlockState().setValue(PlacedBlocks.PlacedBowl.CONTENTS, bowl);
            else if (bucket != null) state = PLACED_BUCKET.defaultBlockState().setValue(PlacedBlocks.PlacedBucket.CONTENTS, bucket);
            else return InteractionResult.PASS;
            BlockPos at = hit.getBlockPos().above();
            if (!level.getBlockState(at).canBeReplaced() || !state.canSurvive(level, at)) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                level.setBlock(at, state, 3);
                level.playSound(null, at, bucket != null ? SoundEvents.LANTERN_PLACE : SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        });
    }

    private RyanStorage() {}
}

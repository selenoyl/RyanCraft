package com.ryancraft.sky;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The Skylands: floating islands above the clouds (our own take on the old Aether, named for Beta's sky dimension).
 * Reached through the classic portal: a frame of glowstone, lit by pouring water into it.
 */
public final class Skylands {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, RyanCraft.id("skylands"));
    public static final List<Item> ITEMS = new ArrayList<>();

    public static final Block SKYSTONE, CLOUD, BLUE_CLOUD, STORM_CLOUD, SKY_PORTAL, ZEPHYRITE_ORE, ZEPHYRITE_BLOCK;
    public static final Item ZEPHYRITE;

    static {
        SKYSTONE = block("skystone", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.ICE).requiresCorrectToolForDrops().strength(1.5f, 6.0f), true);
        CLOUD = block("cloud", CloudBlock::new, cloud(MapColor.SNOW), true);
        BLUE_CLOUD = block("blue_cloud", CloudBlock.Bouncy::new, cloud(MapColor.COLOR_LIGHT_BLUE), true);
        STORM_CLOUD = block("storm_cloud", CloudBlock.Storm::new, cloud(MapColor.COLOR_LIGHT_GRAY).noCollision(), true);
        // zephyrite: a light, wind-touched crystal in the skystone, for gear suited to falling and gliding
        ZEPHYRITE_ORE = block("zephyrite_ore", p -> new net.minecraft.world.level.block.DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(2, 5), p),
                BlockBehaviour.Properties.of().mapColor(MapColor.ICE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).lightLevel(s -> 3), true);
        ZEPHYRITE_BLOCK = block("zephyrite_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).requiresCorrectToolForDrops()
                .strength(3.0f, 4.0f).sound(SoundType.AMETHYST), true);
        ZEPHYRITE = item("zephyrite", new Item.Properties());
        SKY_PORTAL = block("sky_portal", SkyPortalBlock::new, BlockBehaviour.Properties.of().noCollision().randomTicks().strength(-1.0f)
                .sound(SoundType.GLASS).lightLevel(s -> 11).pushReaction(PushReaction.BLOCK), false);
    }

    private static BlockBehaviour.Properties cloud(MapColor c) {
        return BlockBehaviour.Properties.of().mapColor(c).strength(0.2f).sound(SoundType.WOOL).noOcclusion()
                .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).isValidSpawn((s, l, p, t) -> false);
    }

    private static Item item(String name, Item.Properties props) {
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, ik, new Item(props.setId(ik)));
        ITEMS.add(i); return i;
    }


    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props, boolean item) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        if (item) {
            ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
            ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        }
        return b;
    }

    public static final SkyCloudFeature SKY_CLOUD = Registry.register(BuiltInRegistries.FEATURE, RyanCraft.id("sky_cloud"),
            new SkyCloudFeature(net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration.CODEC));

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> ITEMS.forEach(e::accept));
        // Pour a water bucket into a glowstone frame to light the portal (instead of placing the water)
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.WATER_BUCKET) || player.isSpectator()) return InteractionResult.PASS;
            BlockPos at = hit.getBlockPos().relative(hit.getDirection());
            if (!level.getBlockState(hit.getBlockPos()).is(net.minecraft.world.level.block.Blocks.GLOWSTONE) && !SkyPortalShape.nearGlowstone(level, at))
                return InteractionResult.PASS;
            for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
                SkyPortalShape shape = SkyPortalShape.find(level, at, axis);
                if (shape == null) continue;
                if (!level.isClientSide()) {
                    shape.fill(level);
                    level.playSound(null, at, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.6f, 1.6f);
                    level.playSound(null, at, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    private Skylands() {}
}

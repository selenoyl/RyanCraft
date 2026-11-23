package com.ryancraft.metal;

import com.mojang.serialization.MapCodec;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Silver tarnishes like copper weathers: silver, dulled, tarnished, blackened. Each stage has a block, cut silver with
 * stairs and slab, chiseled silver, a grate, a door, a trapdoor and a bulb, and a waxed twin of each. Honeycomb waxes,
 * an axe scrapes one stage back (or the wax off). Tarnishing runs on random ticks (mixin/SilverTarnishMixin). Silverfish
 * drop silver nuggets now and then.
 */
public final class Silver {
    public static final String[] STAGES = {"", "dulled_", "tarnished_", "blackened_"};
    public static final String[] SHAPES = {"silver_block", "cut_silver", "cut_silver_stairs", "cut_silver_slab", "chiseled_silver",
            "silver_grate", "silver_door", "silver_trapdoor", "silver_bulb"};
    static final float CHANCE = 0.05688889f;                     // copper's weathering chance per random tick
    private static final MapColor[] COLOURS = {MapColor.METAL, MapColor.COLOR_LIGHT_GRAY, MapColor.TERRACOTTA_BROWN, MapColor.COLOR_BLACK};
    private static final int[] BULB_LIGHT = {15, 12, 8, 4};

    public static final Map<Block, Block> NEXT = new HashMap<>(), PREV = new HashMap<>(), WAX = new HashMap<>(), UNWAX = new HashMap<>();
    private static final List<Item> ITEMS = new ArrayList<>();

    static {
        Block[][] plain = new Block[STAGES.length][], waxed = new Block[STAGES.length][];
        for (int s = 0; s < STAGES.length; s++) { plain[s] = stage(s, false); waxed[s] = stage(s, true); }
        for (int s = 0; s < STAGES.length; s++)
            for (int k = 0; k < SHAPES.length; k++) {
                WAX.put(plain[s][k], waxed[s][k]); UNWAX.put(waxed[s][k], plain[s][k]);
                if (s + 1 < STAGES.length) { NEXT.put(plain[s][k], plain[s + 1][k]); PREV.put(plain[s + 1][k], plain[s][k]); }
            }
    }

    public static String name(int stage, boolean waxed, String shape) { return (waxed ? "waxed_" : "") + STAGES[stage] + shape; }

    private static Block[] stage(int s, boolean waxed) {
        Block[] out = new Block[SHAPES.length];
        out[0] = s == 0 && !waxed ? Metals.get("silver").block() : block(name(s, waxed, "silver_block"), Block::new, metal(s, waxed));
        Block cut = out[1] = block(name(s, waxed, "cut_silver"), Block::new, metal(s, waxed));
        out[2] = block(name(s, waxed, "cut_silver_stairs"), p -> new StairBlock(cut.defaultBlockState(), p), metal(s, waxed));
        out[3] = block(name(s, waxed, "cut_silver_slab"), SlabBlock::new, metal(s, waxed));
        out[4] = block(name(s, waxed, "chiseled_silver"), Block::new, metal(s, waxed));
        out[5] = block(name(s, waxed, "silver_grate"), Grate::new, metal(s, waxed).sound(SoundType.COPPER_GRATE).noOcclusion()
                .isValidSpawn(Blocks::never).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false));
        out[6] = door(name(s, waxed, "silver_door"), metal(s, waxed).strength(3.0f).noOcclusion().pushReaction(PushReaction.DESTROY));
        out[7] = block(name(s, waxed, "silver_trapdoor"), p -> new TrapDoorBlock(BlockSetType.COPPER, p), metal(s, waxed).strength(3.0f).noOcclusion()
                .isValidSpawn(Blocks::never));
        int light = BULB_LIGHT[s];
        out[8] = block(name(s, waxed, "silver_bulb"), CopperBulbBlock::new, metal(s, waxed).strength(3.0f).sound(SoundType.COPPER_BULB)
                .isRedstoneConductor((a, b, c) -> false).lightLevel(st -> st.getValue(BlockStateProperties.LIT) ? light : 0));
        return out;
    }

    private static BlockBehaviour.Properties metal(int s, boolean waxed) {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(COLOURS[s]).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL);
        return waxed || s == STAGES.length - 1 ? p : p.randomTicks();
    }

    private static Block block(String name, java.util.function.Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    private static Block door(String name, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, new DoorBlock(BlockSetType.COPPER, props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new DoubleHighBlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    /** Called from the random-tick mixin for every block with a next stage. */
    public static void tarnish(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Block next = NEXT.get(state.getBlock());
        if (next == null || random.nextFloat() >= CHANCE) return;
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) return;
        level.setBlockAndUpdate(pos, next.withPropertiesOf(state));   // a door's upper half follows its lower half
    }

    private static InteractionResult swap(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, Block to, SoundEvent sound, int event) {
        if (!level.isClientSide()) {
            level.setBlock(pos, to.withPropertiesOf(state), Block.UPDATE_ALL_IMMEDIATE);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(null, event, pos, 0);
        }
        return InteractionResult.SUCCESS;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(e -> ITEMS.forEach(e::accept));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            BlockPos pos = hit.getBlockPos(); BlockState state = level.getBlockState(pos); Block b = state.getBlock();
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(Items.HONEYCOMB) && WAX.containsKey(b)) {
                stack.consume(1, player);
                return swap(level, pos, state, WAX.get(b), SoundEvents.HONEYCOMB_WAX_ON, 3003);
            }
            if (stack.getItem() instanceof AxeItem) {
                Block to = UNWAX.containsKey(b) ? UNWAX.get(b) : PREV.get(b);
                if (to == null) return InteractionResult.PASS;
                stack.hurtAndBreak(1, player, hand);
                return UNWAX.containsKey(b) ? swap(level, pos, state, to, SoundEvents.AXE_WAX_OFF, 3004) : swap(level, pos, state, to, SoundEvents.AXE_SCRAPE, 3005);
            }
            return InteractionResult.PASS;
        });
        Item nugget = Metals.get("silver").nugget();
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (source.isBuiltin() && EntityType.SILVERFISH.getDefaultLootTable().filter(key::equals).isPresent())
                builder.withPool(LootPool.lootPool().add(LootItem.lootTableItem(nugget)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0f, 2.0f)))));
        });
    }

    /** The silver grate: see-through and waterloggable, like the copper grate. */
    public static final class Grate extends WaterloggedTransparentBlock {
        public static final MapCodec<Grate> CODEC = simpleCodec(Grate::new);
        public Grate(BlockBehaviour.Properties p) { super(p); }
        @Override protected MapCodec<? extends WaterloggedTransparentBlock> codec() { return CODEC; }
    }

    private Silver() {}
}

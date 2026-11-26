package com.ryancraft.deep;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The Abyss: a cave layer below the deepslate, from y=-64 down to bedrock at y=-128, in abyssal stone. It's split into
 * five kinds of cavern, chosen by the world's own climate noise (see mixin/DeepBiomeMixin):
 * <ul><li>Abyssal Caverns: huge plain chambers, dripstone and glow lichen</li>
 * <li>Toxic Caves (dry regions): floors of poisonous green sludge under glowing toxic moss</li>
 * <li>Frozen Caverns (cold regions): packed and blue ice, snow and powder snow</li>
 * <li>Sunken Wilds (wet regions): an underground overworld of moss, azaleas and grass under a bright lumen-moss ceiling</li>
 * <li>Hollow Dark: sculk-choked caverns, like the deep dark</li></ul>
 * The deepening itself (the overworld's height, its noise and the ore heights it shifts) is the built-in data pack
 * resourcepacks/deep_caves, off by default for now (being reworked) and switchable on in a new world's Data Packs screen. Assets: tools/deep_caves.py.
 */
public final class DeepCaves {
    public static final int TOP = -64, FLOOR = -128, LAVA_BELOW = -118;
    public static final List<Item> ITEMS = new ArrayList<>();

    public static final ResourceKey<Biome> ABYSSAL_CAVERNS = biome("abyssal_caverns"), TOXIC_CAVES = biome("toxic_caves"),
            FROZEN_CAVERNS = biome("frozen_caverns"), SUNKEN_WILDS = biome("sunken_wilds"), HOLLOW_DARK = biome("hollow_dark");
    public static final List<ResourceKey<Biome>> BIOMES = List.of(ABYSSAL_CAVERNS, TOXIC_CAVES, FROZEN_CAVERNS, SUNKEN_WILDS, HOLLOW_DARK);

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(4.5f, 6.0f).sound(SoundType.DEEPSLATE);
    }
    public static final Block ABYSSAL_STONE = block("abyssal_stone", Block::new, stone());
    public static final Block COBBLED_ABYSSAL_STONE = block("cobbled_abyssal_stone", Block::new, stone());
    public static final Block POLISHED_ABYSSAL_STONE = block("polished_abyssal_stone", Block::new, stone().sound(SoundType.POLISHED_DEEPSLATE));
    public static final Block ABYSSAL_BRICKS = block("abyssal_bricks", Block::new, stone().sound(SoundType.DEEPSLATE_BRICKS));
    /** Toxic sludge: a thick green muck that slows you and poisons anything standing in it. */
    public static final Block TOXIC_SLUDGE = block("toxic_sludge", ToxicSludgeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN)
            .strength(0.6f).sound(SoundType.MUD).speedFactor(0.5f).jumpFactor(0.6f).lightLevel(s -> 4).emissiveRendering((s, l, p) -> true));
    public static final Block TOXIC_MOSS = block("toxic_moss", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN)
            .strength(0.1f).sound(SoundType.MOSS).lightLevel(s -> 7));
    /** Lumen moss: the Sunken Wilds' ceiling, bright enough to grow things by. */
    public static final Block LUMEN_MOSS = block("lumen_moss", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
            .strength(0.1f).sound(SoundType.MOSS).lightLevel(s -> 14));

    public static class ToxicSludgeBlock extends Block {
        public ToxicSludgeBlock(Properties p) { super(p); }
        @Override
        public void stepOn(Level level, BlockPos pos, BlockState state, Entity e) {
            if (level instanceof ServerLevel && e instanceof LivingEntity le && !le.isSteppingCarefully() && level.getGameTime() % 20 == 0)
                le.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            super.stepOn(level, pos, state, e);
        }
    }

    private static ResourceKey<Biome> biome(String n) { return ResourceKey.create(Registries.BIOME, RyanCraft.id(n)); }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        return b;
    }

    public static void init() {
        Registry.register(BuiltInRegistries.FEATURE, RyanCraft.id("ice_stalagmite"), new IceSpireFeature(false));
        Registry.register(BuiltInRegistries.FEATURE, RyanCraft.id("ice_stalactite"), new IceSpireFeature(true));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> ITEMS.forEach(e::accept));
        FabricLoader.getInstance().getModContainer(RyanCraft.MOD_ID).ifPresent(mod ->
                ResourceLoader.registerBuiltinPack(RyanCraft.id("deep_caves"), mod, Component.literal("RyanCraft: the Abyss (deeper world)"), PackActivationType.NORMAL));
    }

    private DeepCaves() {}
}

package com.ryancraft.trees;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ryancraft.worldgen.RyanBiomes.*;

public final class RyanWood {
    public static final SpeciesTreeFeature SPECIES_TREE = Registry.register(BuiltInRegistries.FEATURE, RyanCraft.id("species_tree"), new SpeciesTreeFeature());
    private static final Map<TreeSpecies, WoodSet> SETS = new EnumMap<>(TreeSpecies.class);
    /** White dogwood petals (they drift like cherry petals). */
    public static final net.minecraft.core.particles.SimpleParticleType DOGWOOD_PETALS = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
            RyanCraft.id("dogwood_petals"), net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple());

    /** Bark (log side) and wood (planks, stripped) map colours, and the colour of falling leaf particles. */
    private record Look(MapColor bark, MapColor wood, int leaf) {}
    private static final Map<TreeSpecies, Look> LOOKS = Map.ofEntries(
            Map.entry(TreeSpecies.WHITE_OAK, new Look(MapColor.STONE, MapColor.WOOD, 0x5f8f3e)),
            Map.entry(TreeSpecies.RED_OAK, new Look(MapColor.COLOR_GRAY, MapColor.TERRACOTTA_ORANGE, 0x3f7a2e)),
            Map.entry(TreeSpecies.SUGAR_MAPLE, new Look(MapColor.COLOR_GRAY, MapColor.SAND, 0x3e7a34)),
            Map.entry(TreeSpecies.RED_MAPLE, new Look(MapColor.STONE, MapColor.SAND, 0x4a8436)),
            Map.entry(TreeSpecies.TULIP_POPLAR, new Look(MapColor.COLOR_GRAY, MapColor.SAND, 0x6aa642)),
            Map.entry(TreeSpecies.SHAGBARK_HICKORY, new Look(MapColor.STONE, MapColor.WOOD, 0x557f34)),
            Map.entry(TreeSpecies.BLACK_WALNUT, new Look(MapColor.COLOR_BLACK, MapColor.COLOR_BROWN, 0x86a846)),
            Map.entry(TreeSpecies.AMERICAN_BEECH, new Look(MapColor.COLOR_LIGHT_GRAY, MapColor.SAND, 0x5c8c3a)),
            Map.entry(TreeSpecies.SYCAMORE, new Look(MapColor.QUARTZ, MapColor.SAND, 0x6a9a44)),
            Map.entry(TreeSpecies.EASTERN_REDBUD, new Look(MapColor.COLOR_BROWN, MapColor.WOOD, 0xd0508e)),
            Map.entry(TreeSpecies.FLOWERING_DOGWOOD, new Look(MapColor.COLOR_BROWN, MapColor.SAND, 0xf2eee4)),
            Map.entry(TreeSpecies.EASTERN_RED_CEDAR, new Look(MapColor.TERRACOTTA_BROWN, MapColor.TERRACOTTA_RED, 0x2e5a40)),
            Map.entry(TreeSpecies.BLACK_CHERRY, new Look(MapColor.COLOR_BLACK, MapColor.TERRACOTTA_ORANGE, 0x3c6e2c)),
            Map.entry(TreeSpecies.SASSAFRAS, new Look(MapColor.COLOR_BROWN, MapColor.WOOD, 0x6aa040)),
            Map.entry(TreeSpecies.OHIO_BUCKEYE, new Look(MapColor.STONE, MapColor.QUARTZ, 0x4c7e30)));

    public static WoodSet get(TreeSpecies s) { return SETS.get(s); }
    public static Iterable<WoodSet> sets() { return SETS.values(); }

    /** Autumn colours: the species whose leaves turn, and the colour of their falling leaves. */
    public static final Map<TreeSpecies, Integer> AUTUMN_COLOURS = Map.ofEntries(
            Map.entry(TreeSpecies.SUGAR_MAPLE, 0xe07a1c), Map.entry(TreeSpecies.RED_MAPLE, 0xc8281e), Map.entry(TreeSpecies.TULIP_POPLAR, 0xe0b830),
            Map.entry(TreeSpecies.AMERICAN_BEECH, 0xb8862e), Map.entry(TreeSpecies.WHITE_OAK, 0x9a4a2a), Map.entry(TreeSpecies.RED_OAK, 0x8e2a1e),
            Map.entry(TreeSpecies.SASSAFRAS, 0xd8582a), Map.entry(TreeSpecies.FLOWERING_DOGWOOD, 0xa82a3a),
            Map.entry(TreeSpecies.BLACK_WALNUT, 0xd8c040), Map.entry(TreeSpecies.SHAGBARK_HICKORY, 0xc89a28), Map.entry(TreeSpecies.OHIO_BUCKEYE, 0xc06a28),
            Map.entry(TreeSpecies.SYCAMORE, 0xa8803a), Map.entry(TreeSpecies.BLACK_CHERRY, 0xc84a28), Map.entry(TreeSpecies.EASTERN_REDBUD, 0xd8b030));
    private static final Map<TreeSpecies, Block> AUTUMN_LEAVES = new EnumMap<>(TreeSpecies.class);
    /** The species' autumn leaves, or its ordinary leaves if it doesn't turn. */
    public static Block autumnLeaves(TreeSpecies s) { return AUTUMN_LEAVES.getOrDefault(s, get(s).leaves()); }

    public static void init() {
        for (TreeSpecies s : TreeSpecies.values()) SETS.put(s, make(s));
        for (var e : AUTUMN_COLOURS.entrySet()) {             // autumn leaves drift down more often
            int c = e.getValue();
            Block b = block(e.getKey().id + "_autumn_leaves", p -> new SeasonalLeavesBlock(true, 0.04f,
                    ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0xFF000000 | c), p),
                    Blocks.leavesProperties(SoundType.GRASS).mapColor(MapColor.COLOR_ORANGE));
            AUTUMN_LEAVES.put(e.getKey(), b);
            Block green = get(e.getKey()).leaves();             // the pair swap back and forth with the seasons
            ((SeasonalLeavesBlock) b).pair(() -> green);
            ((SeasonalLeavesBlock) green).pair(() -> b);
            FlammableBlockRegistry.getDefaultInstance().add(b, 30, 60);
            CompostingChanceRegistry.INSTANCE.add(b, 0.3f);
        }

        for (WoodSet w : SETS.values()) {
            StrippableBlockRegistry.register(w.log(), w.strippedLog());
            StrippableBlockRegistry.register(w.wood(), w.strippedWood());
            FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
            for (Block b : List.of(w.log(), w.wood(), w.strippedLog(), w.strippedWood())) fire.add(b, 5, 5);
            for (Block b : List.of(w.planks(), w.slab(), w.stairs(), w.fence(), w.fenceGate())) fire.add(b, 5, 20);
            fire.add(w.leaves(), 30, 60);
            CompostingChanceRegistry.INSTANCE.add(w.leaves(), 0.3f);
            CompostingChanceRegistry.INSTANCE.add(w.sapling(), 0.3f);
        }
        for (var fruit : List.of(com.ryancraft.RyanItems.CHERRIES, com.ryancraft.RyanItems.BLACK_WALNUT, com.ryancraft.RyanItems.SHELLED_WALNUT)) {
            CompostingChanceRegistry.INSTANCE.add(fruit, 0.65f);
        }

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(e -> {
            for (WoodSet w : SETS.values()) for (Block b : w.all()) if (b != w.leaves() && b != w.sapling()) e.accept(b);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> {
            for (WoodSet w : SETS.values()) { e.accept(w.log()); e.accept(w.leaves()); e.accept(w.sapling()); }
            for (Block b : AUTUMN_LEAVES.values()) e.accept(b);
        });

        addTrees();
    }

    // ---- worldgen: which trees grow where. Placed features (with rarity) live in data/ryancraft/worldgen/placed_feature.
    private static void addTrees() {
        Predicate<BiomeSelectionContext> forests = BiomeSelectors.includeByKey(Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, SEASONAL_FOREST, RAINFOREST);
        Predicate<BiomeSelectionContext> flowery = BiomeSelectors.includeByKey(Biomes.FLOWER_FOREST, Biomes.FOREST, SEASONAL_FOREST, Biomes.MEADOW);
        Predicate<BiomeSelectionContext> wet = BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
        Predicate<BiomeSelectionContext> open = BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW,
                Biomes.SAVANNA, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, SHRUBLAND);

        for (String sp : List.of("white_oak", "red_oak", "sugar_maple", "red_maple", "tulip_poplar", "shagbark_hickory",
                "black_walnut", "american_beech", "black_cherry", "sassafras", "ohio_buckeye")) add(forests, sp + "_forest");
        for (String sp : List.of("eastern_redbud", "flowering_dogwood")) add(flowery, sp + "_understory");
        add(wet, "sycamore_riverbank");
        add(BiomeSelectors.includeByKey(Biomes.FOREST, SEASONAL_FOREST), "sycamore_forest");
        add(open, "eastern_red_cedar_open");
        add(BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW), "white_oak_lone");
    }

    private static void add(Predicate<BiomeSelectionContext> where, String placed) {
        BiomeModifications.addFeature(where, GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, RyanCraft.id(placed)));
    }

    // ---- blocks
    private static WoodSet make(TreeSpecies s) {
        String n = s.id;
        Look look = LOOKS.get(s);
        Block log = block(n + "_log", RotatedPillarBlock::new, Blocks.logProperties(look.wood(), look.bark(), SoundType.WOOD));
        Block wood = block(n + "_wood", RotatedPillarBlock::new, Blocks.logProperties(look.bark(), look.bark(), SoundType.WOOD));
        Block sLog = block("stripped_" + n + "_log", RotatedPillarBlock::new, Blocks.logProperties(look.wood(), look.wood(), SoundType.WOOD));
        Block sWood = block("stripped_" + n + "_wood", RotatedPillarBlock::new, Blocks.logProperties(look.wood(), look.wood(), SoundType.WOOD));
        Block planks = block(n + "_planks", Block::new, wooden(look.wood()).strength(2.0f, 3.0f));
        ParticleOptions particle = s == TreeSpecies.FLOWERING_DOGWOOD ? DOGWOOD_PETALS : s.blossoms() ? ParticleTypes.CHERRY_LEAVES : ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0xFF000000 | look.leaf());
        Block leaves = block(n + "_leaves", p -> new SeasonalLeavesBlock(false, s.blossoms() ? 0.1f : 0.01f, particle, p),
                Blocks.leavesProperties(SoundType.GRASS).mapColor(s == TreeSpecies.FLOWERING_DOGWOOD ? MapColor.SNOW : s.blossoms() ? MapColor.COLOR_PINK : MapColor.PLANT));
        ResourceKey<ConfiguredFeature<?, ?>> tree = ResourceKey.create(Registries.CONFIGURED_FEATURE, RyanCraft.id(n));
        TreeGrower grower = new TreeGrower(RyanCraft.MOD_ID + ":" + n, Optional.empty(), Optional.of(tree), Optional.empty());
        Block sapling = block(n + "_sapling", p -> new SaplingBlock(grower, p), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                .noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));
        Block potted = register("potted_" + n + "_sapling", p -> new FlowerPotBlock(sapling, p), Blocks.flowerPotProperties());
        Block slab = block(n + "_slab", SlabBlock::new, wooden(look.wood()).strength(2.0f, 3.0f));
        Block stairs = block(n + "_stairs", p -> new StairBlock(planks.defaultBlockState(), p), wooden(look.wood()).strength(2.0f, 3.0f));
        Block fence = block(n + "_fence", FenceBlock::new, wooden(look.wood()).strength(2.0f, 3.0f));
        Block gate = block(n + "_fence_gate", p -> new FenceGateBlock(WoodType.OAK, p), wooden(look.wood()).forceSolidOn().strength(2.0f, 3.0f));
        Block door = block(n + "_door", p -> new DoorBlock(BlockSetType.OAK, p), wooden(look.wood()).strength(3.0f).noOcclusion().pushReaction(PushReaction.DESTROY));
        Block trapdoor = block(n + "_trapdoor", p -> new TrapDoorBlock(BlockSetType.OAK, p),
                wooden(look.wood()).strength(3.0f).noOcclusion().isValidSpawn(Blocks::never));
        Block button = block(n + "_button", p -> new ButtonBlock(BlockSetType.OAK, 30, p), Blocks.buttonProperties());
        Block plate = block(n + "_pressure_plate", p -> new PressurePlateBlock(BlockSetType.OAK, p),
                wooden(look.wood()).forceSolidOn().noCollision().strength(0.5f).pushReaction(PushReaction.DESTROY));
        return new WoodSet(s, log, wood, sLog, sWood, planks, leaves, sapling, potted, slab, stairs, fence, gate, door, trapdoor, button, plate);
    }

    private static BlockBehaviour.Properties wooden(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).ignitedByLava();
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
    }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Block block = register(name, factory, props);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item.Properties ip = new Item.Properties().setId(itemKey).useBlockDescriptionPrefix();
        Registry.register(BuiltInRegistries.ITEM, itemKey, block instanceof DoorBlock ? new DoubleHighBlockItem(block, ip) : new BlockItem(block, ip));
        return block;
    }
}

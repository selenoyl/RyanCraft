package com.ryancraft.flora;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Shelf mushrooms: the banded brown turkey tail and the orange sulphur shelf, bracket fungi that grow on tree trunks in
 * forests, taigas and swamps. Two make a bowl of mushroom stew. Assets: tools/shelf_mushrooms.py.
 */
public final class ShelfMushrooms {
    public static final Block TURKEY_TAIL = block("turkey_tail", MapColor.COLOR_BROWN);
    public static final Block SULPHUR_SHELF = block("sulphur_shelf", MapColor.COLOR_ORANGE);

    /** Scans the chunk for exposed trunk bark a little above the ground and puts a few shelves on it. */
    public static final Feature<NoneFeatureConfiguration> FEATURE = Registry.register(BuiltInRegistries.FEATURE, RyanCraft.id("shelf_mushrooms"),
            new Feature<>(NoneFeatureConfiguration.CODEC) {
                @Override
                public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
                    WorldGenLevel level = ctx.level(); RandomSource r = ctx.random(); BlockPos o = ctx.origin();
                    // every trunk in the chunk (a log column at least 3 tall), found by checking all 256 columns
                    java.util.List<BlockPos> trunks = new java.util.ArrayList<>();
                    for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
                        int x = o.getX() + dx, z = o.getZ() + dz;
                        // while a chunk generates, the worldgen heightmap may or may not include the trees just placed, so
                        // look for bark just above the ground, then drop through the canopy if we started above it
                        int y0 = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, y0, z);
                        if (!level.getBlockState(p).is(BlockTags.LOGS)) {
                            p.setY(y0 - 1);
                            for (int k = 0; k < 24; k++) {
                                BlockState st = level.getBlockState(p);
                                if (st.is(BlockTags.LEAVES) || st.isAir() || st.is(net.minecraft.world.level.block.Blocks.VINE)) p.move(0, -1, 0); else break;
                            }
                        }
                        if (!level.getBlockState(p).is(BlockTags.LOGS)) continue;
                        while (level.getBlockState(p.below()).is(BlockTags.LOGS)) p.move(0, -1, 0);           // down to its foot
                        int n = 0;
                        while (n < 24 && level.getBlockState(p.above(n + 1)).is(BlockTags.LOGS)) n++;          // and its height
                        if (n >= 3) trunks.add(p.above(1 + r.nextInt(Math.min(4, n - 1))).immutable());
                    }
                    int placed = 0, want = 2 + r.nextInt(4);
                    java.util.Collections.shuffle(trunks, new java.util.Random(r.nextLong()));
                    for (BlockPos p : trunks) {
                        if (placed >= want) break;
                        Block kind = r.nextInt(4) == 0 ? SULPHUR_SHELF : TURKEY_TAIL;
                        // a little cluster up one side of the trunk
                        Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(r);
                        for (int k = 0, c = 1 + r.nextInt(3); k < c; k++) {
                            BlockPos at = p.above(k).relative(d);
                            if (!level.getBlockState(at).isAir() || !level.getBlockState(p.above(k)).is(BlockTags.LOGS)) break;
                            level.setBlock(at, kind.defaultBlockState().setValue(ShelfMushroomBlock.FACING, d).setValue(ShelfMushroomBlock.SHELVES, 1 + r.nextInt(3)), 2);
                        }
                        placed++;
                    }
                    return placed > 0;
                }
            });

    private static Block block(String name, MapColor colour) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, new ShelfMushroomBlock(BlockBehaviour.Properties.of().mapColor(colour).noCollision()
                .noOcclusion().strength(0.3f).sound(SoundType.FUNGUS).randomTicks().pushReaction(PushReaction.DESTROY).setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        return b;
    }

    public static void init() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_FOREST).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)).or(BiomeSelectors.tag(BiomeTags.IS_JUNGLE))
                        .or(BiomeSelectors.includeByKey(ResourceKey.create(Registries.BIOME, RyanCraft.id("sycamore_bottoms")), ResourceKey.create(Registries.BIOME, RyanCraft.id("cedar_glade")), ResourceKey.create(Registries.BIOME, RyanCraft.id("rainforest"))))
                        .or(BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP)),
                GenerationStep.Decoration.VEGETAL_DECORATION, ResourceKey.create(Registries.PLACED_FEATURE, RyanCraft.id("shelf_mushrooms")));
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(TURKEY_TAIL, 0.65f);   // as vanilla's mushrooms
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(SULPHUR_SHELF, 0.65f);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> { e.accept(TURKEY_TAIL); e.accept(SULPHUR_SHELF); });
    }

    private ShelfMushrooms() {}
}

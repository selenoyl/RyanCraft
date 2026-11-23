package com.ryancraft.entity;

import com.ryancraft.worldgen.BiomeVariants;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Which mudling lives where, and what its camp is built of. */
public final class MudlingKinds {
    public static Mudling.Kind forBiome(Holder<Biome> b) {
        if (b.is(BiomeTags.IS_NETHER)) return Mudling.Kind.CINDER;
        if (b.unwrapKey().map(k -> k.identifier().equals(com.ryancraft.RyanCraft.id("sky_meadow"))).orElse(false)) return Mudling.Kind.KITE;
        if (b.is(BiomeTags.IS_BADLANDS)) return Mudling.Kind.TERRACOTTA;
        if (b.is(Biomes.DESERT)) return Mudling.Kind.SANDSTONE;
        if (b.is(BiomeTags.IS_JUNGLE)) return Mudling.Kind.VINE;
        if (b.is(Biomes.SNOWY_PLAINS) || b.is(Biomes.ICE_SPIKES) || b.is(Biomes.SNOWY_SLOPES) || b.is(Biomes.SNOWY_TAIGA) || b.is(Biomes.FROZEN_PEAKS)
                || b.is(Biomes.SNOWY_BEACH) || b.is(Biomes.GROVE)) return Mudling.Kind.SNOWPACK;
        if (b.is(BiomeTags.IS_TAIGA)) return Mudling.Kind.BARK;
        if (b.is(BiomeTags.IS_BEACH)) return Mudling.Kind.SHELL;
        if (b.is(Biomes.SWAMP) || b.is(Biomes.MANGROVE_SWAMP) || b.is(BiomeVariants.SYCAMORE_BOTTOMS)) return Mudling.Kind.MUD;
        if (b.is(Biomes.STONY_SHORE) || b.is(Biomes.DRIPSTONE_CAVES) || b.is(Biomes.LUSH_CAVES) || b.is(Biomes.WINDSWEPT_GRAVELLY_HILLS)
                || b.is(Biomes.STONY_PEAKS)) return Mudling.Kind.PEBBLE;
        if (b.is(BiomeTags.IS_MOUNTAIN) || b.is(Biomes.WINDSWEPT_HILLS)) return Mudling.Kind.GRAVEL;
        if (b.is(BiomeTags.IS_FOREST) || b.is(Biomes.DARK_FOREST)) return Mudling.Kind.TWIG;
        return Mudling.Kind.SOD;
    }

    /** The block a camp's little ring is laid with. */
    public static Block campBlock(Mudling.Kind k) {
        return switch (k) {
            case SOD -> Blocks.COARSE_DIRT; case TWIG -> Blocks.OAK_PLANKS; case BARK -> Blocks.SPRUCE_LOG; case MUD -> Blocks.MUD;
            case SANDSTONE -> Blocks.CUT_SANDSTONE; case TERRACOTTA -> Blocks.TERRACOTTA; case SNOWPACK -> Blocks.PACKED_ICE;
            case VINE -> Blocks.MOSSY_COBBLESTONE; case GRAVEL -> Blocks.GRAVEL; case SHELL -> Blocks.SMOOTH_SANDSTONE;
            case PEBBLE -> Blocks.COBBLESTONE; case CINDER -> Blocks.BLACKSTONE; case KITE -> com.ryancraft.sky.Skylands.SKYSTONE;
        };
    }

    private MudlingKinds() {}
}

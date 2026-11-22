package com.ryancraft.worldgen;

import com.mojang.serialization.MapCodec;
import com.ryancraft.RyanCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Our eastern trees only grow where the country around is temperate too: no sycamores on the grassy strip of a river
 * running through a desert, no dogwoods on the edge of a jungle. Looks a little way out in eight directions and refuses
 * if any of them is in {@code #ryancraft:not_temperate} (deserts, badlands, savannas, jungles).
 */
public final class TemperateSurroundings extends PlacementFilter {
    public static final TemperateSurroundings INSTANCE = new TemperateSurroundings();
    public static final MapCodec<TemperateSurroundings> CODEC = MapCodec.unit(INSTANCE);
    public static final TagKey<Biome> NOT_TEMPERATE = TagKey.create(Registries.BIOME, RyanCraft.id("not_temperate"));
    public static PlacementModifierType<TemperateSurroundings> TYPE;
    private static final int R = 32;

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        // Ask the biome source, not the level: during generation only nearby chunks exist, and reading a biome
        // from a chunk that isn't there yet crashes world generation.
        var source = ctx.generator().getBiomeSource();
        var sampler = ctx.getLevel().getLevel().getChunkSource().randomState().sampler();
        int qy = net.minecraft.core.QuartPos.fromBlock(pos.getY());
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            int qx = net.minecraft.core.QuartPos.fromBlock(pos.getX() + dx * R), qz = net.minecraft.core.QuartPos.fromBlock(pos.getZ() + dz * R);
            if (source.getNoiseBiome(qx, qy, qz, sampler).is(NOT_TEMPERATE)) return false;
        }
        return true;
    }

    @Override public PlacementModifierType<?> type() { return TYPE; }

    public static void init() {
        TYPE = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, RyanCraft.id("temperate_surroundings"), () -> CODEC);
    }
}

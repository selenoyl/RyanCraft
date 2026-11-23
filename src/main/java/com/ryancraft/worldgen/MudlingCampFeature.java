package com.ryancraft.worldgen;

import com.mojang.serialization.Codec;
import com.ryancraft.entity.Mudling;
import com.ryancraft.entity.MudlingKinds;
import com.ryancraft.entity.RyanEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A mudling camp: a little ring of the biome's own blocks with a flag post in the middle, and 3-6 mudlings of the
 * biome's kind. Camps are rare (placed_feature mudling_camp) and never refill.
 */
public class MudlingCampFeature extends Feature<NoneFeatureConfiguration> {
    public static MudlingCampFeature FEATURE;

    public MudlingCampFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    public static void init() {
        FEATURE = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE, com.ryancraft.RyanCraft.id("mudling_camp"),
                new MudlingCampFeature(NoneFeatureConfiguration.CODEC));
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.SURFACE_STRUCTURES,
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE, com.ryancraft.RyanCraft.id("mudling_camp")));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level(); RandomSource r = ctx.random();
        BlockPos origin = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockState ground = level.getBlockState(origin.below());
        if (!ground.isSolidRender() || !level.getFluidState(origin).isEmpty()) return false;
        Mudling.Kind kind = MudlingKinds.forBiome(level.getBiome(origin));
        BlockState ring = MudlingKinds.campBlock(kind).defaultBlockState();
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d < 2.5 || d > 3.4 || r.nextInt(6) == 0) continue;              // a broken ring, like a pebble fort
            BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin.offset(dx, 0, dz));
            if (level.getBlockState(top.below()).isSolidRender()) level.setBlock(top.below(), ring, 2);
        }
        level.setBlock(origin, Blocks.OAK_FENCE.defaultBlockState(), 2);                  // the flag post
        level.setBlock(origin.above(), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(origin.above(2), Blocks.WHITE_WOOL.defaultBlockState(), 2);
        int n = 3 + r.nextInt(4);
        for (int i = 0; i < n; i++) {
            BlockPos at = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin.offset(r.nextInt(5) - 2, 0, r.nextInt(5) - 2));
            if (at.equals(origin)) continue;
            Mudling m = RyanEntities.MUDLING.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (m == null) continue;
            m.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, r.nextFloat() * 360f, 0f);
            m.setKind(kind);
            m.setVariant(r.nextInt(20) == 0 ? 1 + r.nextInt(3) : 0);
            m.setPersistenceRequired();
            level.addFreshEntityWithPassengers(m);
        }
        return true;
    }
}

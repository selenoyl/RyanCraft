package com.ryancraft.worldgen;

import com.ryancraft.RyanCraft;
import com.ryancraft.sky.RocEggBlock;
import com.ryancraft.sky.Skylands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A roc nest on a high island (y 120 and up): a bed of hay sunk into the grass, ringed with sky willow logs, holding
 * one to three roc eggs.
 */
public class RocNestFeature extends Feature<NoneFeatureConfiguration> {
    public static final int MIN_Y = 120;
    public RocNestFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level(); BlockPos o = ctx.origin(); RandomSource r = ctx.random();
        if (o.getY() < MIN_Y) return false;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)                       // a level, solid patch of island to sit on
                if (!level.getBlockState(o.offset(dx, -1, dz)).is(Blocks.GRASS_BLOCK)) return false;
        Block log = BuiltInRegistries.BLOCK.getValue(RyanCraft.id("sky_willow_log"));
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                int d = dx * dx + dz * dz;
                if (d > 5) continue;                                // round off the corners
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) level.setBlock(o.offset(dx, -1, dz), Blocks.HAY_BLOCK.defaultBlockState(), 2);
                else {
                    BlockState s = log.defaultBlockState();
                    if (s.hasProperty(RotatedPillarBlock.AXIS)) s = s.setValue(RotatedPillarBlock.AXIS, Math.abs(dx) == 2 ? Direction.Axis.Z : Direction.Axis.X);
                    level.setBlock(o.offset(dx, 0, dz), s, 2);
                }
            }
        int eggs = 1 + r.nextInt(3);
        int[][] spots = {{0, 0}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for (int i = 0; i < eggs; i++)
            level.setBlock(o.offset(spots[i][0], 0, spots[i][1]), Skylands.ROC_EGG.defaultBlockState().setValue(RocEggBlock.HATCH, 0), 2);
        return true;
    }
}

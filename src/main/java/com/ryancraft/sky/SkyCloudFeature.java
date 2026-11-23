package com.ryancraft.sky;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/** A drifting cloud: a flat, lumpy blob of cloud blocks in the open sky, only ever filling air. */
public class SkyCloudFeature extends Feature<BlockStateConfiguration> {
    public SkyCloudFeature(Codec<BlockStateConfiguration> c) { super(c); }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> ctx) {
        WorldGenLevel level = ctx.level(); RandomSource r = ctx.random(); BlockPos o = ctx.origin();
        if (!level.isEmptyBlock(o)) return false;
        int puffs = 3 + r.nextInt(4); boolean any = false;
        for (int i = 0; i < puffs; i++) {
            BlockPos c = o.offset(r.nextInt(9) - 4, r.nextInt(2), r.nextInt(9) - 4);
            float rx = 2 + r.nextFloat() * 2.5f, ry = 1 + r.nextFloat(), rz = 2 + r.nextFloat() * 2.5f;
            for (int dx = (int) -rx; dx <= rx; dx++) for (int dy = (int) -ry; dy <= ry; dy++) for (int dz = (int) -rz; dz <= rz; dz++) {
                if ((dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz) > 1) continue;
                BlockPos p = c.offset(dx, dy, dz);
                if (level.isEmptyBlock(p)) { level.setBlock(p, ctx.config().state, 2); any = true; }
            }
        }
        return any;
    }
}

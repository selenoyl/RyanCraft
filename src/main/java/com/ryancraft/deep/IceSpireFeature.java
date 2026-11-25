package com.ryancraft.deep;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * The Frozen Caverns' ice spires, like the ice spikes biome turned into a cave: great tapering columns of packed ice
 * with a blue-ice heart, rising from the floor (stalagmites) or hanging from the ceiling (stalactites). Most are a few
 * blocks across and up to a dozen tall; now and then a giant runs nearly floor to ceiling.
 */
public class IceSpireFeature extends Feature<NoneFeatureConfiguration> {
    private final boolean hanging;
    public IceSpireFeature(boolean hanging) { super(NoneFeatureConfiguration.CODEC); this.hanging = hanging; }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level(); RandomSource r = ctx.random(); BlockPos o = ctx.origin();
        int dir = hanging ? -1 : 1;                                             // which way it grows
        if (!level.getBlockState(o).isAir() || level.getBlockState(o.below(dir)).isAir()) return false;   // must start on rock
        boolean giant = r.nextInt(6) == 0;
        int height = giant ? 14 + r.nextInt(12) : 5 + r.nextInt(8);
        float base = giant ? 3.2f + r.nextFloat() * 1.3f : 1.4f + r.nextFloat() * 1.2f;
        BlockState ice = Blocks.PACKED_ICE.defaultBlockState(), core = Blocks.BLUE_ICE.defaultBlockState();
        double lean = (r.nextDouble() - 0.5) * 0.25, leanZ = (r.nextDouble() - 0.5) * 0.25;
        for (int i = -1; i < height; i++) {                                     // (one layer sunk into the rock, so it's rooted)
            float rad = base * (1f - Math.max(0, i) / (float) height) + 0.35f;
            int ri = (int) Math.ceil(rad), cx = (int) Math.round(lean * i), cz = (int) Math.round(leanZ * i);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > rad) continue;
                    BlockPos p = o.offset(cx + dx, i * dir, cz + dz);
                    BlockState here = level.getBlockState(p);
                    if (i >= 0 && !here.isAir() && !here.is(Blocks.SNOW) && !here.is(Blocks.WATER)) continue;
                    if (i < 0 && here.isAir()) continue;
                    level.setBlock(p, d < rad * 0.4 && rad > 1.2f ? core : ice, 2);
                }
        }
        return true;
    }
}

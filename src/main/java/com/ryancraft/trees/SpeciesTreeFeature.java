package com.ryancraft.trees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/** Grows one tree of a species, with its own random variation, used by worldgen and by saplings. */
public class SpeciesTreeFeature extends Feature<SpeciesTreeConfig> {
    public SpeciesTreeFeature() { super(SpeciesTreeConfig.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<SpeciesTreeConfig> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        if (!level.getBlockState(origin.below()).is(BlockTags.DIRT)) return false;
        TreeSpecies species = TreeSpecies.byId(ctx.config().species());
        WoodSet wood = RyanWood.get(species);
        TreeShape shape = new TreeShape(ctx.random());
        species.build(shape);

        // Room for the trunk?
        for (var e : shape.cells.entrySet()) {
            BlockPos p = e.getKey();
            if (e.getValue().part() != TreeShape.Part.LEAF && Math.abs(p.getX()) <= 1 && Math.abs(p.getZ()) <= 1 && p.getY() < 6
                    && !TreeFeature.validTreePos(level, origin.offset(p))) return false;
        }

        // Wood first
        for (var e : shape.cells.entrySet()) {
            TreeShape.Cell cell = e.getValue();
            if (cell.part() == TreeShape.Part.LEAF) continue;
            BlockPos pos = origin.offset(e.getKey());
            if (!TreeFeature.validTreePos(level, pos)) continue;
            BlockState log = (cell.part() == TreeShape.Part.LOG2 ? wood.strippedLog() : wood.log()).defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, cell.axis());
            this.setBlock(level, pos, log);
        }

        // Leaves, each with its true distance to wood; anything farther than 6 is trimmed so nothing decays
        Map<BlockPos, Integer> dist = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (var e : shape.cells.entrySet()) {
            if (e.getValue().part() != TreeShape.Part.LEAF) { dist.put(e.getKey(), 0); queue.add(e.getKey()); }
        }
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            int d = dist.get(p);
            if (d >= 6) continue;
            for (Direction dir : Direction.values()) {
                BlockPos n = p.relative(dir);
                TreeShape.Cell c = shape.cells.get(n);
                if (c != null && c.part() == TreeShape.Part.LEAF && !dist.containsKey(n)) { dist.put(n, d + 1); queue.add(n); }
            }
        }
        for (var e : shape.cells.entrySet()) {
            if (e.getValue().part() != TreeShape.Part.LEAF) continue;
            Integer d = dist.get(e.getKey());
            if (d == null || d < 1 || d > 6) continue;
            BlockPos pos = origin.offset(e.getKey());
            if (!TreeFeature.validTreePos(level, pos)) continue;
            this.setBlock(level, pos, wood.leaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, d).setValue(LeavesBlock.PERSISTENT, false));
        }

        if (level.getBlockState(origin.below()).is(Blocks.GRASS_BLOCK)) this.setBlock(level, origin.below(), Blocks.DIRT.defaultBlockState());
        return true;
    }
}

package com.ryancraft.pipes;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Shared by both pumps: drawing a bucket out of the pool below, and pushing fluid into whatever is next door. */
public final class FluidWell {
    /** What a draw found: the fluid, and how far down the pool's surface is (for drawing the intake tube). */
    public record Draw(FluidVariant fluid, int depth) {}

    /**
     * Find the pool below (down through up to {@code maxDepth} blocks of air; a hand pump's pipe goes through the ground) and take one bucket from its farthest
     * source, BuildCraft-style: every bucket really comes out of the pool, from its edges inward.
     */
    public static @Nullable Draw draw(Level level, BlockPos pump, int maxDepth, boolean waterOnly) {
        BlockPos p = pump.below(); int depth = 1;
        for (; depth < maxDepth && level.getFluidState(p).isEmpty() && (waterOnly || level.getBlockState(p).isAir()); depth++) p = p.below();
        FluidState fs = level.getFluidState(p);
        if (fs.isEmpty()) return null;
        boolean water = fs.is(FluidTags.WATER);
        if (waterOnly && !water) return null;
        // BuildCraft-style: take the farthest source in the pool, preferring true edges (fewest source neighbours), so a
        // pool, a lake or a patch of ocean is drained from the outside in, slowly. Flowing water alone gives nothing.
        Set<BlockPos> seen = new HashSet<>(); ArrayDeque<BlockPos> q = new ArrayDeque<>(); java.util.Map<BlockPos, Integer> dist = new java.util.HashMap<>();
        BlockPos far = null; int farDist = -1, farNeighbours = 99;
        q.add(p); seen.add(p); dist.put(p, 0);
        while (!q.isEmpty() && seen.size() < 1024) {
            BlockPos c = q.poll();
            FluidState f = level.getFluidState(c);
            if (f.isEmpty() || f.is(FluidTags.WATER) != water) continue;
            int dc = dist.get(c);
            if (f.isSource()) {
                int nb = 0;
                for (Direction h : Direction.Plane.HORIZONTAL) if (level.getFluidState(c.relative(h)).isSource()) nb++;
                if (dc > farDist || (dc == farDist && nb < farNeighbours)) { far = c; farDist = dc; farNeighbours = nb; }
            }
            for (Direction d : Direction.values()) {
                if (d == Direction.UP && c.getY() >= p.getY()) continue;
                BlockPos n = c.relative(d);
                if (n.distManhattan(p) <= 32 && seen.add(n)) { q.add(n); dist.put(n, dc + 1); }
            }
        }
        if (far == null) return null;
        level.setBlock(far, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return new Draw(FluidVariant.of(water ? Fluids.WATER : Fluids.LAVA), depth);
    }

    /** Offer fluid to the block on one side (a fluid pipe, a tank, a vat, a cauldron...). Returns how much it took. */
    public static long push(Level level, BlockPos from, Direction d, FluidVariant v, long amount) {
        BlockPos n = from.relative(d);
        if (level.getBlockEntity(n) instanceof PipeBlockEntity pipe)
            return pipe.leadsSomewhere(level, v, d.getOpposite()) ? pipe.offerFluid(v, amount, d.getOpposite()) : 0;   // no dead ends
        Storage<FluidVariant> s = FluidStorage.SIDED.find(level, n, d.getOpposite());
        if (s == null) return 0;
        try (Transaction tx = Transaction.openOuter()) { long took = s.insert(v, amount, tx); tx.commit(); return took; }
    }

    private FluidWell() {}
}

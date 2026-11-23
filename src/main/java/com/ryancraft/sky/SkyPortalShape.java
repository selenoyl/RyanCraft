package com.ryancraft.sky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A glowstone frame (like a nether portal's, 2x3 to 21x21 inside) and its inside, for lighting the Skylands portal. */
public record SkyPortalShape(BlockPos bottomLeft, Direction.Axis axis, int width, int height) {
    private static boolean frame(BlockState s) { return s.is(Blocks.GLOWSTONE); }
    private static boolean open(BlockState s) { return s.isAir() || s.is(Blocks.WATER) || s.is(Skylands.SKY_PORTAL); }

    static boolean nearGlowstone(Level level, BlockPos p) {
        for (Direction d : Direction.values()) if (frame(level.getBlockState(p.relative(d)))) return true;
        return false;
    }

    public static SkyPortalShape find(Level level, BlockPos inside, Direction.Axis axis) {
        if (!open(level.getBlockState(inside))) return null;
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH, left = right.getOpposite();
        BlockPos p = inside;
        int n = 0;
        while (open(level.getBlockState(p.below())) && n++ < 21) p = p.below();               // down to the frame's bottom
        if (!frame(level.getBlockState(p.below()))) return null;
        n = 0;
        while (open(level.getBlockState(p.relative(left))) && n++ < 21) p = p.relative(left);   // left to the frame's side
        if (!frame(level.getBlockState(p.relative(left)))) return null;
        int w = 0;
        while (w < 22 && open(level.getBlockState(p.relative(right, w)))) w++;
        if (w < 2 || w > 21 || !frame(level.getBlockState(p.relative(right, w)))) return null;
        int h = 0;
        while (h < 22 && open(level.getBlockState(p.above(h)))) h++;
        if (h < 3 || h > 21) return null;
        for (int x = 0; x < w; x++) {                                                            // check the whole frame and inside
            if (!frame(level.getBlockState(p.relative(right, x).below())) || !frame(level.getBlockState(p.relative(right, x).above(h)))) return null;
            for (int y = 0; y < h; y++) if (!open(level.getBlockState(p.relative(right, x).above(y)))) return null;
        }
        for (int y = 0; y < h; y++)
            if (!frame(level.getBlockState(p.relative(left).above(y))) || !frame(level.getBlockState(p.relative(right, w).above(y)))) return null;
        return new SkyPortalShape(p, axis, w, h);
    }

    public void fill(Level level) {
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState portal = Skylands.SKY_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++)
            level.setBlock(bottomLeft.relative(right, x).above(y), portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }
}

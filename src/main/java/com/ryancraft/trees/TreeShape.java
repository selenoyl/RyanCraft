package com.ryancraft.trees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

/** A tree being built in local coordinates (trunk base at 0,0,0), cell by cell. Ported from tools/trees.py. */
public final class TreeShape {
    public enum Part { LOG, LOG2, LEAF }
    public record Cell(Part part, Direction.Axis axis) {}

    final Map<BlockPos, Cell> cells = new HashMap<>();
    final RandomSource r;

    public TreeShape(RandomSource r) { this.r = r; }

    /** x varied by up to +-amount (fraction). */
    double v(double x, double amount) { return x * (1 + (r.nextDouble() * 2 - 1) * amount); }
    /** A random offset in [-amount, amount]. */
    double j(double amount) { return (r.nextDouble() * 2 - 1) * amount; }
    int vi(int base, int spread) { return base + r.nextInt(spread + 1); }

    void put(double x, double y, double z, Part part, Direction.Axis axis, boolean force) {
        BlockPos k = new BlockPos((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
        Cell old = cells.get(k);
        if (force || old == null || (old.part() == Part.LEAF && part != Part.LEAF)) cells.put(k, new Cell(part, axis));
    }

    void trunk(int h, int w, int y0) {
        for (int y = y0; y < y0 + h; y++)
            for (int dx = 0; dx < w; dx++)
                for (int dz = 0; dz < w; dz++) put(dx - (w - 1) / 2.0 - 0.01, y, dz - (w - 1) / 2.0 - 0.01, Part.LOG, Direction.Axis.Y, true);
    }

    /** A branch from a to b, connected face to face so limbs never look broken. */
    void line(double[] a, double[] b, Part part) {
        double dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
        Direction.Axis axis = Math.abs(dy) >= Math.max(Math.abs(dx), Math.abs(dz)) ? Direction.Axis.Y
                : Math.abs(dx) >= Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
        int n = (int) Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) * 2 + 1;
        int[] prev = null;
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            int[] cur = {(int) Math.round(a[0] + dx * t), (int) Math.round(a[1] + dy * t), (int) Math.round(a[2] + dz * t)};
            if (prev != null) {
                int[] p = prev.clone();
                for (int ax : new int[]{1, 0, 2}) {
                    if (p[ax] != cur[ax]) { p[ax] = cur[ax]; put(p[0], p[1], p[2], part, axis, false); }
                }
            }
            put(cur[0], cur[1], cur[2], part, axis, false);
            prev = cur;
        }
    }

    double[] limb(double[] base, double angle, double rise, double length, Part part) {
        double[] end = {base[0] + Math.cos(angle) * length, base[1] + rise, base[2] + Math.sin(angle) * length};
        line(base, end, part);
        return end;
    }

    /** An ellipsoid of leaves, ragged at the edges. Big blobs get hidden logs inside so the leaves never decay. */
    void blob(double cx, double cy, double cz, double rx, double ry, double rz, double density) {
        for (int x = (int) Math.floor(cx - rx) - 1; x <= (int) Math.ceil(cx + rx) + 1; x++)
            for (int y = (int) Math.floor(cy - ry) - 1; y <= (int) Math.ceil(cy + ry) + 1; y++)
                for (int z = (int) Math.floor(cz - rz) - 1; z <= (int) Math.ceil(cz + rz) + 1; z++) {
                    double d = sq((x - cx) / rx) + sq((y - cy) / ry) + sq((z - cz) / rz);
                    if (d <= 1.0 && (d <= 0.7 || r.nextDouble() < density)) put(x, y, z, Part.LEAF, Direction.Axis.Y, false);
                }
        put(cx, cy, cz, Part.LOG, Direction.Axis.Y, false);
        if (Math.max(rx, rz) > 3.2) {
            double s = Math.max(rx, rz) * 0.5;
            line(new double[]{cx - s, cy, cz}, new double[]{cx + s, cy, cz}, Part.LOG);
            line(new double[]{cx, cy, cz - s}, new double[]{cx, cy, cz + s}, Part.LOG);
        }
        if (ry > 3.2) line(new double[]{cx, cy - ry * 0.5, cz}, new double[]{cx, cy + ry * 0.5, cz}, Part.LOG);
    }


    // ---- crowns built like vanilla's fancy oak: limbs reaching out from the trunk, each ending in a flat-bottomed cluster

    /** A leaf cluster: stacked discs (radii 2,3,3,2 scaled), the limb ending in the second disc, with a ragged rim. */
    void cluster(double cx, double cy, double cz, double size, int[] layers, double density) {
        int bx = (int) Math.round(cx), by = (int) Math.round(cy), bz = (int) Math.round(cz);
        for (int i = 0; i < layers.length; i++) {
            double rr = layers[i] * size;
            for (int dx = -4; dx <= 4; dx++)
                for (int dz = -4; dz <= 4; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > rr + 0.4 || (d > rr - 0.6 && r.nextDouble() > density)) continue;
                    put(bx + dx, by + i - 1, bz + dz, Part.LEAF, Direction.Axis.Y, false);
                }
        }
        put(bx, by, bz, Part.LOG, Direction.Axis.Y, false);
    }

    static final int[] CLUSTER = {2, 3, 3, 2}, SMALL_CLUSTER = {2, 3, 2};

    interface Envelope { double at(double f); }
    static final Envelope ROUND = f -> Math.sqrt(Math.max(0, 1 - sq(2 * f - 1)));
    static Envelope egg(double p) {
        return f -> f < p ? Math.sqrt(Math.max(0, 1 - sq((p - f) / p))) : Math.sqrt(Math.max(0, 1 - sq((f - p) / (1 - p))));
    }
    static final Envelope VASE = f -> f < 0.8 ? Math.min(1.0, 0.35 + 0.9 * f) : Math.max(0.3, 1 - (f - 0.8) * 3.5);

    /**
     * A whole crown: trunk of trunkH (w wide), then {@code tips} limbs ending in clusters, spread up the crown from
     * h*baseF to h-2 and out to envelope(f)*width. Limbs rise {@code slope} blocks per block of reach.
     */
    void crown(int h, int trunkH, double baseF, Envelope env, double width, int tips, double slope, double size, int w,
               Part limb, double density, int[] layers, boolean top) {
        trunk(trunkH, w, 0);
        double y0 = h * baseF, y1 = h - 2, ga = r.nextDouble() * Math.PI * 2;
        for (int k = 0; k < tips; k++) {
            double f = (k + r.nextDouble()) / tips;
            double y = y0 + f * (y1 - y0);
            double dist = env.at(f) * width * (0.5 + 0.5 * r.nextDouble());
            ga += 2.39996 + j(0.3);                                  // golden angle, so limbs don't line up
            double[] tip = {Math.cos(ga) * dist, y, Math.sin(ga) * dist};
            double start = Math.min(trunkH - 1, Math.max(2, y - dist * slope));
            line(new double[]{0, start, 0}, tip, start > trunkH * 0.6 ? limb : Part.LOG);
            cluster(tip[0], tip[1], tip[2], size * (0.85 + 0.3 * r.nextDouble()), layers, density);
        }
        if (top) cluster(0, trunkH, 0, size, layers, density);
    }

    private static double sq(double v) { return v * v; }
}

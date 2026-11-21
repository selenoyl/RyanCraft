package com.ryancraft.trees;

import java.util.function.Consumer;

/**
 * The fifteen eastern US trees. Each build rolls its own variation (height, limbs, crown size, lean,
 * occasional forks) so no two trees of a species are the same, but each still reads as its species.
 */
public enum TreeSpecies {
    WHITE_OAK("white_oak", t -> {                 // massive, wide-spreading, low horizontal limbs
        int h = t.vi(5, 2); t.trunk(h, 2, 0);
        int n = t.vi(5, 2);
        for (int k = 0; k < n; k++) {
            double a = k * Math.PI * 2 / n + t.j(0.35);
            double[] e = t.limb(new double[]{0, h - 1 + t.r.nextDouble() * 1.5, 0}, a, t.v(3, .35), t.v(7, .15), TreeShape.Part.LOG);
            double rr = t.v(3.6, .15); t.blob(e[0], e[1], e[2], rr, t.v(2.6, .15), rr, .92);
        }
        double cr = t.v(5, .12); t.blob(t.j(1.5), h + 5 + t.j(1), t.j(1.5), cr, t.v(3.2, .15), cr, .92);
    }),
    RED_OAK("red_oak", t -> {                     // straighter trunk, limbs angled up, rounded crown
        int h = t.vi(8, 2); t.trunk(h, 1, 0);
        int n = t.vi(4, 2);
        for (int k = 0; k < n; k++) {
            double a = k * Math.PI * 2 / n + t.j(0.35);
            double[] e = t.limb(new double[]{0, h - 3 + (k % 2), 0}, a, t.v(5, .2), t.v(4.2, .2), TreeShape.Part.LOG);
            t.blob(e[0], e[1], e[2], t.v(3, .15), t.v(2.6, .15), t.v(3, .15), .92);
        }
        double cr = t.v(5.2, .12); t.blob(t.j(1.2), h + 4, t.j(1.2), cr, t.v(4.2, .12), cr, .92);
    }),
    SUGAR_MAPLE("sugar_maple", t -> {             // big, dense, egg-shaped crown that comes down low
        int h = t.vi(6, 2); t.trunk(h, 1, 0);
        int n = t.vi(4, 2);
        for (int k = 0; k < n; k++) {
            double a = k * Math.PI * 2 / n + t.j(0.4);
            double[] e = t.limb(new double[]{0, h - 3, 0}, a, 3, t.v(4, .2), TreeShape.Part.LOG);
            t.blob(e[0], e[1], e[2], 3, 2.6, 3, .97);
        }
        double cr = t.v(6, .12); t.blob(t.j(.8), h + 4, t.j(.8), cr, t.v(6.5, .12), cr, .98);
    }),
    RED_MAPLE("red_maple", t -> {                 // slim upright oval; sometimes forks into twin trunks
        if (t.r.nextFloat() < 0.3) {
            t.trunk(3, 1, 0);
            for (int s = -1; s <= 1; s += 2) {
                double a = t.r.nextDouble() * Math.PI * 2;
                double[] top = {s * 1.6 * Math.cos(a), t.vi(8, 2), s * 1.6 * Math.sin(a)};
                t.line(new double[]{0, 2, 0}, top, TreeShape.Part.LOG);
                t.blob(top[0], top[1] + 1, top[2], t.v(2.8, .15), t.v(4.5, .12), t.v(2.8, .15), .93);
            }
        } else {
            int h = t.vi(8, 2); t.trunk(h, 1, 0);
            for (int k = 0; k < 3; k++) t.limb(new double[]{0, h - 4, 0}, k * Math.PI * 2 / 3 + t.j(.4), 4, 2, TreeShape.Part.LOG);
            t.blob(t.j(.6), h + 2, t.j(.6), t.v(3.4, .15), t.v(5.5, .12), t.v(3.4, .15), .93);
        }
    }),
    TULIP_POPLAR("tulip_poplar", t -> {           // very tall, straight, narrow crown high up
        int h = t.vi(19, 6); t.trunk(h, 1, 0);
        for (int y = h - 9; y < h - 1; y += 2) t.limb(new double[]{0, y, 0}, t.r.nextDouble() * Math.PI * 2, 1.5, 2.5, TreeShape.Part.LOG);
        t.blob(t.j(.8), h - 2, t.j(.8), t.v(4, .15), t.v(7, .12), t.v(4, .15), .94);
    }),
    SHAGBARK_HICKORY("shagbark_hickory", t -> {   // tall, narrow, irregular clumps
        int h = t.vi(13, 4); t.trunk(h, 1, 0);
        for (int y = h - 6; y < h + 2; y += 2) {
            double[] e = t.limb(new double[]{0, y, 0}, t.r.nextDouble() * Math.PI * 2, 2, t.v(2.8, .25), TreeShape.Part.LOG);
            t.blob(e[0], e[1], e[2], t.v(2.6, .15), 2.2, t.v(2.6, .15), .85);
        }
        t.blob(0, h + 2, 0, 3, 3, 3, .92);
    }),
    BLACK_WALNUT("black_walnut", t -> {           // open, airy crown on long arching limbs
        int h = t.vi(8, 2); t.trunk(h, 1, 0);
        int n = t.vi(4, 2);
        for (int k = 0; k < n; k++) {
            double[] e = t.limb(new double[]{0, h - 2, 0}, k * Math.PI * 2 / n + t.j(.35), t.v(4, .25), t.v(5.8, .15), TreeShape.Part.LOG);
            t.blob(e[0], e[1], e[2], 3, 2.2, 3, .6);
        }
        t.blob(0, h + 4, 0, 4, 3, 4, .6);
    }),
    AMERICAN_BEECH("american_beech", t -> {       // short trunk, low, wide, dense dome
        int h = t.vi(4, 1); t.trunk(h, 1, 0);
        for (int k = 0; k < 6; k++) t.limb(new double[]{0, 3, 0}, k * Math.PI / 3 + t.j(.3), 2, 4, TreeShape.Part.LOG);
        double cr = t.v(6.2, .1); t.blob(t.j(.8), h + 3, t.j(.8), cr, t.v(4.8, .1), cr, .97);
    }),
    SYCAMORE("sycamore", t -> {                   // huge, open, white upper limbs over mottled bark
        int h = t.vi(6, 2); t.trunk(h, 2, 0);
        int n = t.vi(4, 2);
        for (int k = 0; k < n; k++) {
            double a = k * Math.PI * 2 / n + t.j(.4);
            double[] mid = t.limb(new double[]{0, h - 1, 0}, a, 4, 4, TreeShape.Part.LOG);
            double[] e = t.limb(mid, a + t.j(.5), t.v(4, .25), t.v(5, .2), TreeShape.Part.LOG2);
            t.blob(e[0], e[1], e[2], 3.2, 2.6, 3.2, .7);
        }
        t.blob(0, h + 10, 0, 4, 3, 4, .7);
    }),
    EASTERN_REDBUD("eastern_redbud", t -> {       // small vase, forks low into leaning stems, flat crown of blossom
        int tb = t.vi(2, 1); t.trunk(tb, 1, 0);
        int n = t.vi(3, 2);
        for (int k = 0; k < n; k++) {
            double[] e = t.limb(new double[]{0, tb, 0}, k * Math.PI * 2 / n + t.j(.4), t.v(3, .2), t.v(3, .2), TreeShape.Part.LOG);
            t.blob(e[0], e[1] + 1, e[2], t.v(2.6, .15), 1.4, t.v(2.6, .15), .8);
        }
        t.blob(0, tb + 4.5, 0, 3, 1.4, 3, .8);
    }),
    FLOWERING_DOGWOOD("flowering_dogwood", t -> { // small, flat tiers of white blossom with air between
        int tiers = t.r.nextFloat() < 0.25 ? 2 : 3;
        int top = 3 + 4 * (tiers - 1);
        t.trunk(top, 1, 0);
        for (int i = 0; i < tiers; i++) {
            double y = 3 + 4 * i + (i > 0 ? t.j(.6) : 0);
            double rr = t.v(4.8 - 1.1 * i, .12);
            int limbs = t.vi(4, 2);
            for (int k = 0; k < limbs; k++) t.limb(new double[]{0, y, 0}, k * Math.PI * 2 / limbs + t.j(.4), 0, rr - 1.4, TreeShape.Part.LOG);
            t.blob(0, y + 1, 0, rr, 0.55, rr, .9);
        }
    }),
    EASTERN_RED_CEDAR("eastern_red_cedar", t -> { // dense dark column, a little irregular
        int h = t.vi(12, 5); t.trunk(h - 2, 1, 0);
        double base = t.v(2.4, .15);
        for (int y = 2; y < h; y++) {
            double rr = Math.max(.8, base - (y - 2) * 0.16 * 12.0 / h);
            t.blob(t.j(.35), y, t.j(.35), rr, .7, rr, .97);
        }
        t.put(0, h, 0, TreeShape.Part.LEAF, net.minecraft.core.Direction.Axis.Y, false);
        t.put(0, h + 1, 0, TreeShape.Part.LEAF, net.minecraft.core.Direction.Axis.Y, false);
    }),
    BLACK_CHERRY("black_cherry", t -> {           // tall dark trunk, small irregular crown at the top
        int h = t.vi(11, 4); t.trunk(h, 1, 0);
        for (int k = 0; k < 3; k++) {
            double[] e = t.limb(new double[]{0, h - 3, 0}, k * Math.PI * 2 / 3 + t.j(.5), 2.5, t.v(3.5, .2), TreeShape.Part.LOG);
            t.blob(e[0], e[1], e[2], 2.4, 2, 2.4, .8);
        }
        t.blob(t.j(.8), h + 2, t.j(.8), 3, 2.6, 3, .85);
    }),
    SASSAFRAS("sassafras", t -> {                 // small-medium, layered irregular clumps; sometimes twin-stemmed
        int h = t.vi(8, 2); t.trunk(h, 1, 0);
        for (int y = 4; y <= h - 1; y += 2) {
            for (int k = 0; k < 2; k++) {
                double[] e = t.limb(new double[]{0, y, 0}, t.r.nextDouble() * Math.PI * 2, 1, t.v(2.8, .25), TreeShape.Part.LOG);
                t.blob(e[0], e[1], e[2], 2.2, 1.2, 2.2, .85);
            }
        }
        t.blob(0, h + 1, 0, 2.6, 2, 2.6, .92);
        if (t.r.nextFloat() < 0.25) {            // a second, shorter stem
            double a = t.r.nextDouble() * Math.PI * 2;
            double[] top = t.limb(new double[]{0, 1, 0}, a, h - 3, 2, TreeShape.Part.LOG);
            t.blob(top[0], top[1] + 1, top[2], 2.2, 1.6, 2.2, .85);
        }
    }),
    OHIO_BUCKEYE("ohio_buckeye", t -> {           // short and stout: low, wide, rounded crown
        int h = t.vi(4, 1); t.trunk(h, 1, 0);
        for (int k = 0; k < 4; k++) t.limb(new double[]{0, 3, 0}, k * Math.PI / 2 + .8 + t.j(.3), 2, 3, TreeShape.Part.LOG);
        double cr = t.v(5.5, .12); t.blob(t.j(.6), h + 3.5, t.j(.6), cr, t.v(3.6, .12), cr, .96);
    });

    public final String id;
    private final Consumer<TreeShape> builder;

    TreeSpecies(String id, Consumer<TreeShape> builder) { this.id = id; this.builder = builder; }

    public void build(TreeShape shape) { builder.accept(shape); }

    public boolean blossoms() { return this == EASTERN_REDBUD || this == FLOWERING_DOGWOOD; }

    public static TreeSpecies byId(String id) {
        for (TreeSpecies s : values()) if (s.id.equals(id)) return s;
        return WHITE_OAK;
    }
}

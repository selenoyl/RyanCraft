package com.ryancraft.trees;

import java.util.function.Consumer;

/**
 * The fifteen eastern US trees. Each build rolls its own variation (height, limbs, crown size, lean,
 * occasional forks) so no two trees of a species are the same, but each still reads as its species.
 */
public enum TreeSpecies {
    WHITE_OAK("white_oak", t -> { // massive, wide-spreading, low near-horizontal limbs on a thick trunk
        int h = t.vi(12, 3); t.crown(h, (int) (h * .55), 0.45, TreeShape.egg(.4), 7.5, 11, 0.35, 1.0, 2, TreeShape.Part.LOG, 0.75, TreeShape.CLUSTER, true);
    }),
    RED_OAK("red_oak", t -> { // straight trunk, rounded crown
        int h = t.vi(14, 3); t.crown(h, (int) (h * .7), 0.45, TreeShape.ROUND, 5, 10, 0.7, 1.0, 1, TreeShape.Part.LOG, 0.8, TreeShape.CLUSTER, true);
    }),
    SUGAR_MAPLE("sugar_maple", t -> { // big, dense, egg-shaped crown
        int h = t.vi(15, 3); t.crown(h, (int) (h * .7), 0.35, TreeShape.egg(.38), 5.5, 14, 0.8, 1.0, 1, TreeShape.Part.LOG, 0.9, TreeShape.CLUSTER, true);
    }),
    RED_MAPLE("red_maple", t -> { // slimmer upright oval
        int h = t.vi(12, 3); t.crown(h, (int) (h * .75), 0.4, TreeShape.ROUND, 3.6, 9, 1.0, 1.0, 1, TreeShape.Part.LOG, 0.8, TreeShape.CLUSTER, true);
    }),
    TULIP_POPLAR("tulip_poplar", t -> { // very tall and straight; oval crown over the upper half
        int h = t.vi(22, 5); t.crown(h, h - 3, 0.5, TreeShape.egg(.45), 4.2, 15, 1.0, 0.9, 1, TreeShape.Part.LOG, 0.85, TreeShape.CLUSTER, true);
    }),
    SHAGBARK_HICKORY("shagbark_hickory", t -> { // tall, narrow, irregular
        int h = t.vi(16, 4); t.crown(h, h - 3, 0.45, TreeShape.ROUND, 3.8, 10, 0.8, 0.85, 1, TreeShape.Part.LOG, 0.75, TreeShape.CLUSTER, true);
    }),
    BLACK_WALNUT("black_walnut", t -> { // open, airy crown on long arching limbs
        int h = t.vi(13, 3); t.crown(h, (int) (h * .6), 0.4, TreeShape.egg(.55), 6.5, 9, 0.5, 0.85, 1, TreeShape.Part.LOG, 0.65, TreeShape.CLUSTER, true);
    }),
    AMERICAN_BEECH("american_beech", t -> { // short trunk, low wide dense dome
        int h = t.vi(10, 3); t.crown(h, (int) (h * .6), 0.38, TreeShape.egg(.3), 6.5, 12, 0.45, 1.0, 1, TreeShape.Part.LOG, 0.85, TreeShape.CLUSTER, true);
    }),
    SYCAMORE("sycamore", t -> { // huge and open, white upper limbs
        int h = t.vi(17, 4); t.crown(h, (int) (h * .55), 0.45, TreeShape.ROUND, 7, 9, 0.7, 1.0, 2, TreeShape.Part.LOG2, 0.65, TreeShape.CLUSTER, true);
    }),
    EASTERN_REDBUD("eastern_redbud", t -> { // small vase of leaning stems under blossom
        int h = t.vi(8, 1); t.crown(h, 3, 0.55, TreeShape.VASE, 4, 6, 1.1, 0.75, 1, TreeShape.Part.LOG, 0.85, TreeShape.SMALL_CLUSTER, false);
    }),
    FLOWERING_DOGWOOD("flowering_dogwood", t -> { // small and spreading, layered pads of blossom
        int h = t.vi(8, 1); t.crown(h, 4, 0.55, TreeShape.egg(.45), 4.5, 6, 0.55, 0.8, 1, TreeShape.Part.LOG, 0.8, TreeShape.SMALL_CLUSTER, true);
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
    BLACK_CHERRY("black_cherry", t -> { // tall dark trunk, small irregular crown at the top
        int h = t.vi(14, 4); t.crown(h, h - 3, 0.6, TreeShape.ROUND, 3.6, 8, 0.8, 0.9, 1, TreeShape.Part.LOG, 0.8, TreeShape.CLUSTER, true);
    }),
    SASSAFRAS("sassafras", t -> { // small-medium, layered clumps
        int h = t.vi(9, 3); t.crown(h, h - 2, 0.45, TreeShape.egg(.4), 3.6, 8, 0.4, 0.8, 1, TreeShape.Part.LOG, 0.8, TreeShape.SMALL_CLUSTER, true);
    }),
    OHIO_BUCKEYE("ohio_buckeye", t -> { // short and stout, rounded crown
        int h = t.vi(9, 2); t.crown(h, (int) (h * .6), 0.42, TreeShape.ROUND, 4.5, 9, 0.6, 1.0, 1, TreeShape.Part.LOG, 0.9, TreeShape.CLUSTER, true);
    }),
    SKY_WILLOW("sky_willow", t -> { // the Skylands' own: tall and slender, a narrow crown of drooping silvery leaves
        int h = t.vi(13, 3); t.crown(h, (int) (h * .7), 0.35, TreeShape.egg(.4), 3.2, 8, 0.9, 1.1, 1, TreeShape.Part.LOG, 0.75, TreeShape.CLUSTER, true);
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

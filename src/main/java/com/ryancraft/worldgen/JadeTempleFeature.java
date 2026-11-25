package com.ryancraft.worldgen;

import com.ryancraft.RyanCraft;
import com.ryancraft.entity.GreatMoonRabbit;
import com.ryancraft.entity.RyanEntities;
import com.ryancraft.moon.MoonBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The jade temple of the old moon people, the Great Moon Rabbit's arena. It stands on two stepped terraces of moonstone
 * bricks, each banded in jade, with jade lantern posts at the corners. A grand stair climbs the south face, toward the
 * landing site, through a great circular jade moon gate; on top is the open arena, ringed in chiseled moonstone and a
 * low parapet, with the rabbit waiting at its heart. At the back stands a pagoda hall with tiered, upturned jade roofs
 * and the temple's chests; when the rabbit falls, the portal home opens in the hall's doorway (GreatMoonRabbit#die,
 * eight blocks north of the arena's heart).
 * <p>There's only one, a short walk north of the moon's landing site (MoonPortalBlock), where every jade portal leads.
 */
public class JadeTempleFeature extends Feature<NoneFeatureConfiguration> {
    /** Where the one temple stands (its terraces' centre): north of the landing site at the moon's 0, 0. Kept in the
     *  middle of its chunk, so the whole temple fits in the area a feature may build in. */
    public static final int TEMPLE_X = 8, TEMPLE_Z = -40;
    /** Terraces: the lower one 31 across and the upper 25, each 3 high; the arena floor is 6 up. */
    public static final int LOW = 15, HIGH = 12, TIER = 3, TOP = 6;
    /** The arena's heart, relative to the terraces' centre (a little south of it, to leave room for the hall). */
    public static final int ARENA_Z = 2;
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, RyanCraft.id("chests/jade_temple"));
    public JadeTempleFeature() { super(NoneFeatureConfiguration.CODEC); }

    private WorldGenLevel level; private BlockPos o; private RandomSource r;
    private void set(int x, int y, int z, BlockState st) { level.setBlock(o.offset(x, y, z), st, 2); }
    private void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState st) {
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) set(x, y, z, st);
    }
    private BlockState worn(BlockState bricks, BlockState cracked) { return r.nextInt(7) == 0 ? cracked : bricks; }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        level = ctx.level(); r = ctx.random();
        BlockPos c0 = ctx.origin();
        if (c0.getX() >> 4 != TEMPLE_X >> 4 || c0.getZ() >> 4 != TEMPLE_Z >> 4) return false;      // only the one temple
        o = new BlockPos(TEMPLE_X, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, TEMPLE_X, TEMPLE_Z), TEMPLE_Z);
        if (o.getY() < level.getMinY() + 12) return false;
        BlockState bricks = MoonBlocks.MOONSTONE_BRICKS.defaultBlockState(), cracked = MoonBlocks.CRACKED_MOONSTONE_BRICKS.defaultBlockState(),
                deep = MoonBlocks.DEEP_MOONSTONE_BRICKS.defaultBlockState(), floor = MoonBlocks.POLISHED_MOONSTONE.defaultBlockState(),
                ring = MoonBlocks.CHISELED_MOONSTONE_BRICKS.defaultBlockState(), jade = MoonBlocks.JADE_BLOCK.defaultBlockState(),
                jadeBricks = com.ryancraft.deco.GemBlocks.JADE_BRICKS.defaultBlockState(), glow = Blocks.SEA_LANTERN.defaultBlockState(), air = Blocks.AIR.defaultBlockState();

        // the terraces, on a foundation down to solid ground, with the sky above them cleared
        for (int x = -LOW; x <= LOW; x++)
            for (int z = -LOW; z <= LOW + 7; z++) {
                boolean stair = z > LOW;
                if (stair && Math.abs(x) > 2) continue;
                for (int y = -1; y >= -12; y--) {
                    BlockPos p = o.offset(x, y, z);
                    if (y < -1 && !level.getBlockState(p).isAir() && !level.getBlockState(p).canBeReplaced()) break;
                    level.setBlock(p, deep, 2);
                }
                for (int y = 0; y <= 18; y++) set(x, y, z, air);
                int ad = Math.max(Math.abs(x), Math.abs(z));
                if (!stair) {
                    for (int y = 0; y < TIER; y++) set(x, y, z, ad == LOW && y == TIER - 1 ? jadeBricks : deep);
                    if (ad <= HIGH) for (int y = TIER; y < TOP; y++) set(x, y, z, ad == HIGH && y == TOP - 1 ? jadeBricks : worn(bricks, cracked));
                }
            }
        // the arena floor, its chiseled ring and a low parapet (open at the gate)
        for (int x = -HIGH + 1; x <= HIGH - 1; x++)
            for (int z = -HIGH + 1; z <= HIGH - 1; z++) {
                double d = Math.sqrt(x * x + (z - ARENA_Z) * (z - ARENA_Z));
                set(x, TOP - 1, z, d >= 6.5 && d < 7.5 ? ring : floor);
            }
        for (int i = -HIGH; i <= HIGH; i++) {
            for (int[] c : new int[][]{{i, -HIGH}, {-HIGH, i}, {HIGH, i}, {i, HIGH}}) {
                if (c[1] == HIGH && Math.abs(c[0]) <= 3) continue;                    // the gateway
                if (r.nextInt(9) == 0) continue;                                       // ruined, here and there
                set(c[0], TOP, c[1], (i & 1) == 0 ? jadeBricks : worn(bricks, cracked));
            }
        }
        // the grand stair up the south face, five wide
        for (int k = 1; k <= TOP; k++) {
            int z = HIGH + (TOP + 1 - k);
            for (int x = -2; x <= 2; x++) for (int y = 0; y < k; y++) set(x, y, z, y == k - 1 ? floor : deep);
            set(-3, k - 1, z, jadeBricks); set(3, k - 1, z, jadeBricks);                 // its jade balustrades
        }
        // the moon gate: a great ring of jade standing at the top of the stair
        double gr = 4.6; int gy = TOP + 4;
        for (int x = -6; x <= 6; x++)
            for (int y = TOP; y <= gy + 6; y++) {
                double d = Math.sqrt(x * x + (y - gy) * (y - gy));
                if (d < gr - 0.8 || d > gr + 0.8) continue;
                if (y == TOP && Math.abs(x) <= 2) continue;                              // walk through it
                set(x, y, HIGH, (x + y) % 3 == 0 ? jadeBricks : jade);
            }
        set(0, gy + (int) Math.round(gr) + 1, HIGH, glow);
        // the pagoda hall at the back: walls, jade columns, a doorway where the portal home will open
        int hz0 = ARENA_Z - 8, hz1 = -HIGH + 1, hx = 6;                                 // front wall at the portal's place
        fill(-hx, TOP, hz1, hx, TOP, hz0, floor);
        for (int x = -hx; x <= hx; x++)
            for (int z = hz1; z <= hz0; z++)
                for (int y = TOP + 1; y <= TOP + 5; y++) {
                    boolean wall = Math.abs(x) == hx || z == hz1 || z == hz0;
                    boolean door = z == hz0 && x >= -1 && x <= 2 && y <= TOP + 4;
                    set(x, y, z, wall && !door ? worn(bricks, cracked) : air);
                }
        for (int x : new int[]{-hx, -3, 4, hx}) for (int y = TOP + 1; y <= TOP + 5; y++) set(x, y, hz0 + 1, jade);   // the columns
        // its roofs: two tiers of jade, the lower overhanging, with the corners turned up and a lantern on top
        int ry = TOP + 6;
        fill(-hx - 2, ry, hz1 - 1, hx + 2, ry, hz0 + 2, jadeBricks);
        for (int[] c : new int[][]{{-hx - 2, hz1 - 1}, {hx + 2, hz1 - 1}, {-hx - 2, hz0 + 2}, {hx + 2, hz0 + 2}}) set(c[0], ry + 1, c[1], jade);
        fill(-hx + 2, ry + 1, hz1 + 1, hx - 2, ry + 2, hz0 - 1, bricks);
        fill(-hx, ry + 3, hz1, hx, ry + 3, hz0, jadeBricks);
        for (int[] c : new int[][]{{-hx, hz1}, {hx, hz1}, {-hx, hz0}, {hx, hz0}}) set(c[0], ry + 4, c[1], jade);
        fill(-2, ry + 4, hz1 + 2, 2, ry + 4, hz0 - 2, jadeBricks);
        set(0, ry + 5, (hz0 + hz1) / 2, jade); set(0, ry + 6, (hz0 + hz1) / 2, glow);
        // lantern posts at the terraces' corners and beside the gate
        for (int[] c : new int[][]{{-LOW + 1, -LOW + 1}, {LOW - 1, -LOW + 1}, {-LOW + 1, LOW - 1}, {LOW - 1, LOW - 1},
                                   {-HIGH + 1, -HIGH + 1}, {HIGH - 1, -HIGH + 1}, {-5, HIGH - 1}, {5, HIGH - 1}}) {
            int base = Math.max(Math.abs(c[0]), Math.abs(c[1])) > HIGH - 1 ? TIER : TOP;
            for (int y = base; y < base + 3; y++) set(c[0], y, c[1], jadeBricks);
            set(c[0], base + 3, c[1], glow);
        }
        // the temple's chests, in the hall
        for (int x : new int[]{-hx + 2, hx - 2}) {
            BlockPos cp = o.offset(x, TOP + 1, hz1 + 1);
            level.setBlock(cp, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(cp) instanceof ChestBlockEntity chest) chest.setLootTable(LOOT, r.nextLong());
        }
        // and the rabbit, at the arena's heart
        BlockPos heart = o.offset(0, TOP, ARENA_Z);
        GreatMoonRabbit boss = RyanEntities.GREAT_MOON_RABBIT.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (boss != null) {
            boss.snapTo(heart.getX() + 0.5, heart.getY(), heart.getZ() + 0.5, 180f, 0f);
            boss.setHome(heart);
            level.addFreshEntity(boss);
        }
        return true;
    }
}

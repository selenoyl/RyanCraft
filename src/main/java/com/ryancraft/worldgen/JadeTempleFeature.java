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
 * A ruined jade temple of the old moon people: an open-roofed arena of moonstone bricks, 23 across, on a solid
 * foundation, with tall jade pillars at its corners (glowing at their tops, so a temple can be spotted across the dark
 * craters), a doorway in each wall, a chiseled ring in the floor and two chests. The Great Moon Rabbit waits at its
 * heart; when it falls, the portal home opens where it stood (GreatMoonRabbit#die).
 * <p>There's only one, a short walk from the moon's landing site (MoonPortalBlock), where every jade portal leads.
 */
public class JadeTempleFeature extends Feature<NoneFeatureConfiguration> {
    public static final int R = 11, WALL = 6, PILLAR = 11;
    /** Where the one temple stands: a short walk north of the landing site at the moon's 0, 0. */
    public static final int TEMPLE_X = 0, TEMPLE_Z = -48;
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, RyanCraft.id("chests/jade_temple"));
    public JadeTempleFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level(); RandomSource r = ctx.random();
        BlockPos c0 = ctx.origin();
        if (c0.getX() >> 4 != TEMPLE_X >> 4 || c0.getZ() >> 4 != TEMPLE_Z >> 4) return false;      // only the one temple
        BlockPos o = new BlockPos(TEMPLE_X, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, TEMPLE_X, TEMPLE_Z), TEMPLE_Z);
        if (o.getY() < level.getMinY() + 12) return false;
        BlockState bricks = MoonBlocks.MOONSTONE_BRICKS.defaultBlockState(), cracked = MoonBlocks.CRACKED_MOONSTONE_BRICKS.defaultBlockState(),
                floor = MoonBlocks.POLISHED_MOONSTONE.defaultBlockState(), ring = MoonBlocks.CHISELED_MOONSTONE_BRICKS.defaultBlockState(),
                jade = MoonBlocks.JADE_BLOCK.defaultBlockState(), glow = Blocks.SEA_LANTERN.defaultBlockState(), air = Blocks.AIR.defaultBlockState();
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++) {
                for (int y = -1; y >= -10; y--) {                                   // a foundation down to solid ground
                    BlockPos p = o.offset(dx, y, dz);
                    if (y < -1 && !level.getBlockState(p).isAir() && !level.getBlockState(p).canBeReplaced()) break;
                    level.setBlock(p, y == -1 ? floor : bricks, 2);
                }
                int ad = Math.max(Math.abs(dx), Math.abs(dz)); double rd = Math.sqrt(dx * dx + dz * dz);
                if (rd >= 5.5 && rd < 6.5) level.setBlock(o.offset(dx, -1, dz), ring, 2);
                for (int y = 0; y <= PILLAR + 1; y++) level.setBlock(o.offset(dx, y, dz), air, 2);
                if (ad == R) {                                                      // the walls, with doorways
                    boolean door = Math.min(Math.abs(dx), Math.abs(dz)) <= 1;
                    boolean corner = Math.abs(dx) == R && Math.abs(dz) == R;
                    int h = corner ? PILLAR : door ? 0 : WALL - (r.nextInt(5) == 0 ? r.nextInt(3) : 0);   // ruined, here and there
                    for (int y = 0; y < h; y++) level.setBlock(o.offset(dx, y, dz), corner ? jade : r.nextInt(6) == 0 ? cracked : bricks, 2);
                    if (corner) level.setBlock(o.offset(dx, PILLAR, dz), glow, 2);
                    if (door) for (int y = 3; y < WALL; y++) level.setBlock(o.offset(dx, y, dz), bricks, 2);   // lintel
                }
            }
        for (int[] c : new int[][]{{-R + 2, -R + 2}, {R - 2, R - 2}}) {             // two chests in the corners
            BlockPos cp = o.offset(c[0], 0, c[1]);
            level.setBlock(cp, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(cp) instanceof ChestBlockEntity chest) chest.setLootTable(LOOT, r.nextLong());
        }
        GreatMoonRabbit boss = RyanEntities.GREAT_MOON_RABBIT.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (boss != null) {
            boss.snapTo(o.getX() + 0.5, o.getY(), o.getZ() + 0.5, r.nextFloat() * 360f, 0f);
            boss.setHome(o);
            level.addFreshEntity(boss);
        }
        return true;
    }
}

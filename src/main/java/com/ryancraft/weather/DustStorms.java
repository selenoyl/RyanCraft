package com.ryancraft.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Dust storms: when a thunderstorm rolls in, deserts and badlands don't get rain, they get a dust storm. Rare, like a
 * thunderstorm. Thick sandy fog (rusty over the badlands), blowing dust and howling wind on the client; thirst drains
 * faster for anyone caught out in one.
 */
public final class DustStorms {
    public static boolean dusty(Holder<Biome> b) { return b.is(Biomes.DESERT) || b.is(BiomeTags.IS_BADLANDS); }
    public static boolean red(Holder<Biome> b) { return b.is(BiomeTags.IS_BADLANDS); }

    /** Server: is this spot out in a dust storm right now? */
    public static boolean caughtIn(Level level, BlockPos pos) {
        return level.isThundering() && level.canSeeSky(pos) && dusty(level.getBiome(pos));
    }

    private DustStorms() {}
}

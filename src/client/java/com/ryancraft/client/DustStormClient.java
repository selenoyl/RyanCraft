package com.ryancraft.client;

import com.ryancraft.weather.DustStorms;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

/** The look of a dust storm: how thick it is around the camera, its colour, and the dust blowing past. */
public final class DustStormClient {
    /** 0 (clear) .. 1 (full storm), eased so it rolls in and out. */
    public static float intensity, redness;
    private static final int SAND = 0xD8BC84, RUST = 0xB86A40;

    public static int fogColour(int base) {
        if (intensity <= 0.001f) return base;
        int c = lerp(SAND, RUST, redness);
        return lerp(base & 0xFFFFFF, c, Math.min(1f, intensity * 1.1f)) | (base & 0xFF000000);
    }

    private static int lerp(int a, int b, float k) {
        int r = (int) (((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k), g = (int) (((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k),
            bl = (int) ((a & 255) * (1 - k) + (b & 255) * k);
        return (r << 16) | (g << 8) | bl;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ClientLevel level = mc.level; Player p = mc.player;
            if (level == null || p == null) { intensity = 0; return; }
            // how much of the land around is desert or badlands (so the storm thins out at the edges)
            int dusty = 0, red = 0, n = 0;
            BlockPos at = p.blockPosition();
            for (int dx = -24; dx <= 24; dx += 12)
                for (int dz = -24; dz <= 24; dz += 12) {
                    Holder<Biome> b = level.getBiome(at.offset(dx, 0, dz)); n++;
                    if (DustStorms.dusty(b)) { dusty++; if (DustStorms.red(b)) red++; }
                }
            float sheltered = level.canSeeSky(at) || level.canSeeSky(at.above()) ? 1f : 0.35f;      // indoors it's calmer
            float target = level.getThunderLevel(1f) * dusty / (float) n * sheltered;
            intensity += (target - intensity) * 0.02f;
            if (dusty > 0) redness += ((float) red / dusty - redness) * 0.05f;
            if (intensity > 0.05f && !mc.isPaused()) blow(level, p, level.getRandom());
        });
    }

    /** Dust streaming past on the wind. */
    private static void blow(ClientLevel level, Player p, RandomSource r) {
        int c = lerp(lerp(SAND, RUST, redness), 0xFFF0D8, 0.25f);           // a little paler than the fog, so specks read as dust
        DustParticleOptions dust = new DustParticleOptions(c, 0.7f + r.nextFloat() * 0.5f);
        int count = (int) (intensity * 22);
        for (int i = 0; i < count; i++) {
            double x = p.getX() - 14 + r.nextDouble() * 20, z = p.getZ() - 12 + r.nextDouble() * 24, y = p.getY() - 1 + r.nextDouble() * 6;
            level.addParticle(dust, x, y, z, 7 + r.nextDouble() * 4, (r.nextDouble() - 0.5) * 0.6, 1.5 + r.nextDouble());
        }
    }

    private DustStormClient() {}
}

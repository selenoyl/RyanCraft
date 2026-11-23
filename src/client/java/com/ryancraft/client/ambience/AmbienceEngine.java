package com.ryancraft.client.ambience;

import com.ryancraft.RyanConfig;
import com.ryancraft.RyanSounds;
import com.ryancraft.entity.Turkey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * MAtmos-style atmosphere: reads the player's surroundings once a second and blends
 * looping beds (wind, forest, crickets, frogs, streams, surf, rain on leaves, cave air)
 * with randomly timed calls placed around you in 3D (birds, owls, woodpeckers, wolves, drips...).
 * Everything plays on the Ambient/Environment volume slider.
 */
public final class AmbienceEngine {
    private final RandomSource random = RandomSource.create();
    private final Map<SoundEvent, AmbientLoop> loops = new HashMap<>();
    private final Map<SoundEvent, Integer> cooldowns = new HashMap<>();
    private Surroundings env;
    private int ticks;

    public void tick(Minecraft mc) {
        RyanConfig cfg = RyanConfig.get();
        if (!cfg.ambience || mc.level == null || mc.player == null) {
            stopAll(mc);
            return;
        }
        if (mc.isPaused()) return;
        if (env == null || ++ticks % 20 == 0) {
            env = Surroundings.sample(mc);
            updateLoops(mc, cfg.ambienceVolume);
        }
        oneShots(mc, cfg.ambienceVolume);
        splashes(mc, cfg.ambienceVolume);
        if (cfg.fireflies) fireflies(mc, cfg.fireflyAmount);
    }

    /** Fireflies blinking over fields and through the woods on warm, dry nights, only once it's fully dark (config fireflies, fireflyAmount). */
    private void fireflies(Minecraft mc, float amount) {
        Surroundings e = env;
        if (e == null || !e.surface || !e.night || e.rainHere || e.snowy || e.desert || e.temperature < 0.5f) return;
        long t = Math.floorMod(mc.level.getDayTime(), 24000L);
        if (t < 13500 || t > 22500) return;                              // only once it's properly dark, not at dusk or dawn
        if (e.grass < 6 && e.leaves < 5) return;
        if (random.nextFloat() > 0.25f * Math.max(0f, Math.min(1f, amount))) return;      // a thin scatter by default
        var player = mc.player;
        double x = player.getX() + (random.nextDouble() - 0.5) * 28, z = player.getZ() + (random.nextDouble() - 0.5) * 28;
        int ground = mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
        if (Math.abs(ground - player.getY()) > 12) return;
        double y = ground + 0.4 + random.nextDouble() * 2.6;
        mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.FIREFLY, x, y, z, 0, 0, 0);
    }

    private double wadeDistance;

    /** Sloshing splashes as you wade or swim through water, paced by how fast you move. */
    private void splashes(Minecraft mc, float master) {
        var player = mc.player;
        if (!player.isInWater() || player.isSpectator() || player.isPassenger()) { wadeDistance = 0; return; }
        Vec3 v = player.getDeltaMovement();
        double speed = Math.sqrt(v.x * v.x + v.z * v.z);
        if (speed < 0.02) return;
        boolean submerged = player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER);
        wadeDistance += speed;
        double stride = submerged ? 1.6 : (player.onGround() ? 0.9 : 1.2);
        if (wadeDistance >= stride) {
            wadeDistance = 0;
            float vol = (submerged ? 0.15f : 0.35f + (float) Math.min(speed, 0.3) * 1.2f) * master;
            mc.getSoundManager().play(new SimpleSoundInstance(RyanSounds.SPLASH, SoundSource.PLAYERS, vol,
                    (submerged ? 0.7f : 0.9f) + random.nextFloat() * 0.2f, random, player.getX(), player.getY() + 0.2, player.getZ()));
        }
    }

    // ---------------- loops ----------------

    private void updateLoops(Minecraft mc, float master) {
        Surroundings e = env;
        float muffle = e.submerged ? 0.15f : 1.0f;
        float m = master * muffle;
        boolean calm = !e.rainHere;

        // Wind: rises with altitude, mountains, deserts and storms; trees break it up
        float wind = 0;
        if (e.surface) {
            // A light breeze anywhere under open sky, building with altitude
            wind = 0.12f * e.skyLight + Mth.clamp((e.y - (e.seaLevel + 15)) / 80.0f, 0, 0.6f);
            if (e.night) wind += 0.05f;
            if (e.mountain) wind += 0.25f;
            if (e.desert || e.beach || e.ocean) wind += 0.2f;
            if (e.thundering) wind += 0.3f;
            else if (e.raining) wind += 0.1f;
            if (e.leaves > 30) wind *= 0.5f;
            wind = Math.min(wind, 0.85f);
        }
        if (e.skylands) wind = e.leaves > 30 ? 0.35f : 0.55f + 0.15f * e.skyLight;   // the high wind never quite drops up here
        wind = Math.max(wind, com.ryancraft.client.DustStormClient.intensity);     // a dust storm howls
        loop(mc, e.snowy ? RyanSounds.WIND_HOWL : RyanSounds.WIND, wind * m, null);
        loop(mc, e.snowy ? RyanSounds.WIND : RyanSounds.WIND_HOWL, 0, null);

        // Forest bed: leaves rustling and distant twitter by day
        float forest = e.surface && e.day && calm ? Mth.clamp(e.leaves / 45.0f, 0, 1) * 0.55f : 0;
        loop(mc, RyanSounds.FOREST, forest * m, null);

        // Crickets on warm, dry nights wherever there's grass or trees
        boolean lively = e.leaves > 3 || e.grass > 6 || e.forest || e.open || e.jungle || e.savanna;
        float crickets = e.surface && (e.night || e.dusk) && calm && !e.snowy && !e.desert && lively ? 0.5f : 0;
        loop(mc, RyanSounds.CRICKETS, crickets * m, null);

        // Cicadas: the drone of a hot summer day in the trees
        long t = mc.level.getDayTime() % 24000L;
        float midday = Mth.clamp(1.0f - Math.abs(t - 6500L) / 5000.0f, 0, 1);
        boolean warm = e.temperature >= 0.6f && !e.snowy;
        float cicadas = e.surface && e.day && calm && warm && (e.leaves > 8 || e.jungle || e.savanna) ? 0.25f + 0.35f * midday : 0;
        loop(mc, RyanSounds.CICADAS, cicadas * m, null);

        // Waterfalls: roaring where water falls, placed at the falling water
        float falls = e.fallingWater >= 2 ? Mth.clamp(e.fallingWater / 10.0f, 0.25f, 1.0f) * 0.9f : 0;
        loop(mc, RyanSounds.WATERFALL, falls * master, e.fallingCentre);

        // Frogs: swamps at night, or ponds on warm nights
        float frogs = 0;
        if (e.surface && (e.night || e.dusk) && !e.snowy) {
            if (e.swamp) frogs = 0.6f;
            else if (e.stillWater > 10 && e.temperature > 0.5f) frogs = 0.3f;
        }
        loop(mc, RyanSounds.FROGS, frogs * m, null);

        // Running water, placed where the water actually is (works in caves too)
        float stream = e.flowingWater >= 3 ? Mth.clamp(e.flowingWater / 14.0f, 0.2f, 1.0f) * 0.8f : 0;
        loop(mc, RyanSounds.STREAM, stream * master, e.flowingCentre);

        // Surf at the coast
        float surf = e.surface && (e.ocean || e.beach) && e.stillWater > 40 && Math.abs(e.y - e.seaLevel) < 14 ? 0.65f : 0;
        loop(mc, RyanSounds.OCEAN, surf * m, null);

        // Rain pattering on leaves
        float rain = e.rainHere && e.surface && e.leaves > 10 ? Mth.clamp(e.leaves / 50.0f, 0, 1) * 0.6f : 0;
        loop(mc, RyanSounds.RAIN_LEAVES, rain * m, null);

        // Cave air: deeper is darker
        float cave = e.underground ? 0.35f + 0.4f * Mth.clamp((e.seaLevel - e.y) / 70.0f, 0, 1) : 0;
        loop(mc, RyanSounds.CAVE, cave * m, null);
    }

    private void loop(Minecraft mc, SoundEvent event, float target, Vec3 anchor) {
        AmbientLoop current = loops.get(event);
        if (current != null && (current.isStopped() || !mc.getSoundManager().isActive(current)) && current.target() <= 0) {
            loops.remove(event);
            current = null;
        }
        if (target <= 0.001f) {
            if (current != null) current.setTarget(0, anchor);
            return;
        }
        if (current == null || current.isStopped()) {
            current = new AmbientLoop(event, anchor);
            loops.put(event, current);
            mc.getSoundManager().play(current);
        }
        current.setTarget(target, anchor);
    }

    private void stopAll(Minecraft mc) {
        if (loops.isEmpty()) return;
        for (AmbientLoop l : loops.values()) mc.getSoundManager().stop(l);
        loops.clear();
        env = null;
    }

    // ---------------- one-shots ----------------

    private void oneShots(Minecraft mc, float master) {
        Surroundings e = env;
        if (e == null || e.submerged) return;
        boolean calm = !e.rainHere;
        boolean treed = e.leaves > 5;

        if (e.surface && calm && (e.day || e.dawn) && !e.desert && (treed || e.open || e.grass > 10)) {
            // Birds sing hardest at dawn
            maybe(mc, RyanSounds.BIRD, e.dawn ? 30 : 70, e.dawn ? 120 : 260, 8, 24, 3, 9, 0.35f, 0.75f, master);
        }
        if (e.surface && calm && e.day && e.leaves > 25) {
            maybe(mc, RyanSounds.WOODPECKER, 600, 1800, 12, 30, 2, 8, 0.4f, 0.7f, master);
        }
        if (e.surface && calm && (e.day || e.dawn) && e.leaves > 15) {
            maybe(mc, RyanSounds.CUCKOO, 900, 2600, 20, 40, 4, 10, 0.35f, 0.6f, master);
        }
        if (e.surface && e.day && (e.open || e.forest || e.taiga)) {
            if (maybe(mc, RyanSounds.CROW, 900, 2200, 16, 36, 4, 12, 0.35f, 0.6f, master)) shockGobble(mc);
        }
        if (e.surface && calm && e.day && !e.skylands && (e.mountain || e.desert || e.savanna || e.y > e.seaLevel + 45)) {
            if (maybe(mc, RyanSounds.HAWK, 1200, 3000, 10, 30, 20, 35, 0.35f, 0.6f, master)) shockGobble(mc);
        }
        if (e.surface && e.day && (e.ocean || e.beach)) {
            maybe(mc, RyanSounds.SEAGULL, 300, 900, 10, 30, 6, 16, 0.4f, 0.7f, master);
        }
        if (e.surface && calm && e.night && e.leaves > 10) {
            if (maybe(mc, RyanSounds.OWL, 300, 900, 10, 26, 4, 10, 0.4f, 0.7f, master)) shockGobble(mc);
        }
        if (e.surface && calm && e.night && (e.taiga || e.snowy || e.mountain || e.forest)) {
            maybe(mc, RyanSounds.WOLF_HOWL, 1200, 3600, 32, 48, 0, 10, 0.3f, 0.5f, master);
        }
        if (e.surface && (e.night || e.dusk) && (e.swamp || (e.stillWater > 10 && e.temperature > 0.5f))) {
            maybe(mc, RyanSounds.BULLFROG, 200, 600, 6, 18, -2, 1, 0.4f, 0.7f, master);
        }
        if (e.surface && e.leaves > 20) {
            maybe(mc, RyanSounds.LEAVES, e.thundering ? 120 : 400, e.thundering ? 400 : 1200, 4, 12, 3, 8, 0.25f, 0.5f, master);
        }
        if (e.underground) {
            maybe(mc, RyanSounds.DRIP, 40, 220, 2, 8, 1, 5, 0.25f, 0.6f, master);
            if (e.y < e.seaLevel - 20) maybe(mc, RyanSounds.RUMBLE, 1200, 3600, 10, 30, -8, 4, 0.3f, 0.5f, master);
            // The old-shaft stone slide: somewhere off in the dark, deeper down
            if (e.y < e.seaLevel - 10) maybe(mc, RyanSounds.STONE_SLIDE, 1800, 4800, 14, 36, -12, 6, 0.3f, 0.55f, master);
        }
        // The Skylands: gusts, the creak and groan of floating rock below, distant roc cries from far above
        if (e.skylands) {
            maybePlayer(mc, RyanSounds.GUST, 140, 420, 0.4f, 0.7f, master);
            maybePitched(mc, RyanSounds.STONE_SLIDE, 1400, 3600, 12, 30, -24, -6, 0.2f, 0.4f, master, 0.45f, 0.6f);
            maybePitched(mc, RyanSounds.RUMBLE, 2400, 6000, 20, 40, -30, -10, 0.2f, 0.35f, master, 0.5f, 0.7f);
            if (e.day) maybePitched(mc, RyanSounds.HAWK, 900, 2400, 30, 60, 12, 30, 0.3f, 0.5f, master, 0.5f, 0.62f);
            return;
        }
        // Gusts rolling over summits and high ground
        if (e.surface && (e.mountain || e.y > e.seaLevel + 35 || (e.thundering && e.skyLight > 0.8f))) {
            float g = e.thundering ? 0.8f : 0.55f;
            maybePlayer(mc, RyanSounds.GUST, e.thundering ? 160 : 300, e.thundering ? 500 : 900, g * 0.7f, g, master);
        }
    }

    /** Plays a sound at a random spot around the player when its cooldown runs out. */
    private boolean maybe(Minecraft mc, SoundEvent event, int minGap, int maxGap, double minDist, double maxDist,
                          double minUp, double maxUp, float minVol, float maxVol, float master) {
        int cd = cooldowns.getOrDefault(event, minGap / 2 + random.nextInt(Math.max(1, minGap)));
        if (--cd > 0) { cooldowns.put(event, cd); return false; }
        cooldowns.put(event, minGap + random.nextInt(Math.max(1, maxGap - minGap)));
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = minDist + random.nextDouble() * (maxDist - minDist);
        Vec3 p = mc.player.position();
        double x = p.x + Math.cos(angle) * dist, z = p.z + Math.sin(angle) * dist;
        double y = mc.player.getEyeY() + minUp + random.nextDouble() * (maxUp - minUp);
        float vol = (minVol + random.nextFloat() * (maxVol - minVol)) * master;
        float pitch = 0.9f + random.nextFloat() * 0.2f;
        mc.getSoundManager().play(new SimpleSoundInstance(event, SoundSource.AMBIENT, vol, pitch, random, x, y, z));
        return true;
    }

    /** Like {@link #maybe} with its own pitch range (deep roc cries, slow creaks of floating rock). */
    private void maybePitched(Minecraft mc, SoundEvent event, int minGap, int maxGap, double minDist, double maxDist,
                              double minUp, double maxUp, float minVol, float maxVol, float master, float minPitch, float maxPitch) {
        int cd = cooldowns.getOrDefault(event, minGap / 2 + random.nextInt(Math.max(1, minGap)));
        if (--cd > 0) { cooldowns.put(event, cd); return; }
        cooldowns.put(event, minGap + random.nextInt(Math.max(1, maxGap - minGap)));
        double angle = random.nextDouble() * Math.PI * 2, dist = minDist + random.nextDouble() * (maxDist - minDist);
        Vec3 p = mc.player.position();
        double y = mc.player.getEyeY() + minUp + random.nextDouble() * (maxUp - minUp);
        mc.getSoundManager().play(new SimpleSoundInstance(event, SoundSource.AMBIENT, (minVol + random.nextFloat() * (maxVol - minVol)) * master,
                minPitch + random.nextFloat() * (maxPitch - minPitch), random, p.x + Math.cos(angle) * dist, y, p.z + Math.sin(angle) * dist));
    }

    /** A sound that belongs to the player (a gust around you): not placed in the world. */
    private void maybePlayer(Minecraft mc, SoundEvent event, int minGap, int maxGap, float minVol, float maxVol, float master) {
        int cd = cooldowns.getOrDefault(event, minGap / 2 + random.nextInt(Math.max(1, minGap)));
        if (--cd > 0) { cooldowns.put(event, cd); return; }
        cooldowns.put(event, minGap + random.nextInt(Math.max(1, maxGap - minGap)));
        float vol = (minVol + random.nextFloat() * (maxVol - minVol)) * master;
        mc.getSoundManager().play(new SimpleSoundInstance(event.location(), SoundSource.AMBIENT, vol, 0.95f + random.nextFloat() * 0.1f,
                random, false, 0, net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE, 0, 0, 0, true));
    }

    /** Real gobblers "shock gobble" at owls, crows and hawks. Nearby toms answer, a beat later. */
    private void shockGobble(Minecraft mc) {
        AABB area = mc.player.getBoundingBox().inflate(40);
        for (Turkey turkey : mc.level.getEntitiesOfClass(Turkey.class, area, t -> !t.isBaby())) {
            if (random.nextInt(3) == 0) continue;
            mc.getSoundManager().playDelayed(new SimpleSoundInstance(RyanSounds.TURKEY_GOBBLE, SoundSource.NEUTRAL,
                    1.2f, 0.9f + random.nextFloat() * 0.2f, random, turkey.getX(), turkey.getY(), turkey.getZ()),
                    15 + random.nextInt(25));
        }
    }
}

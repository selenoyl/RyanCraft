package com.ryancraft.entity;

import net.minecraft.world.entity.Entity;

/** Daytime birds go quiet once it's properly dark, and stay quiet until the dawn chorus starts. */
final class DayBirds {
    private DayBirds() {}

    static boolean quiet(Entity bird) {
        long t = Math.floorMod(bird.level().getDayTime(), 24000L);
        return t >= 13000L && t < 23000L;
    }
}

package com.ryancraft.moon;

import com.ryancraft.entity.BossMeteor;
import com.ryancraft.entity.RyanEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Meteors fall all the time on the moon (never on Earth): glowing rocks streak down near anyone out under the black
 * sky, more often at night, and now and then a shower brings them thick and fast. They never break blocks, but a near
 * miss bursts like a small creeper and a direct hit blows up right on you; a roof or a cave keeps you safe.
 */
public final class MoonMeteors {
    private static int showerTicks;

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (!level.dimension().equals(MoonPortalBlock.MOON)) return;
            RandomSource r = level.getRandom();
            if (showerTicks > 0) showerTicks--;
            else if (r.nextInt(12000) == 0) showerTicks = 600 + r.nextInt(600);        // a shower: half a minute to a minute
            boolean night = level.isDarkOutside();
            int chance = showerTicks > 0 ? 40 : night ? 400 : 900;                       // per player, per tick
            for (ServerPlayer p : level.players()) {
                if (p.isSpectator() || r.nextInt(chance) != 0 || !level.canSeeSky(p.blockPosition())) continue;
                fall(level, p.position().add(r.nextGaussian() * 10, 0, r.nextGaussian() * 10), r);
            }
        });
    }

    /** A meteor streaking down at a slant to land around the given spot. */
    public static void fall(ServerLevel level, Vec3 target, RandomSource r) {
        Vec3 from = target.add(r.nextGaussian() * 12, 40 + r.nextInt(20), r.nextGaussian() * 12);
        level.addFreshEntity(new BossMeteor(level, from, target.subtract(from).normalize()).gentle());
    }

    private MoonMeteors() {}
}

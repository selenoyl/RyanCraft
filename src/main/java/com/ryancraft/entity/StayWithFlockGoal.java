package com.ryancraft.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Drifts back toward the middle of its flock when it wanders too far from the others. */
public class StayWithFlockGoal<T extends PathfinderMob> extends Goal {
    private final T mob;
    private final Class<T> type;
    private final double speed, radius, maxSpread;
    private Vec3 centre;
    private int cooldown;

    public StayWithFlockGoal(T mob, Class<T> type, double speed, double radius, double maxSpread) {
        this.mob = mob; this.type = type; this.speed = speed; this.radius = radius; this.maxSpread = maxSpread;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (--cooldown > 0 || mob.isBaby()) return false;
        cooldown = 40 + mob.getRandom().nextInt(40);
        List<T> flock = mob.level().getEntitiesOfClass(type, mob.getBoundingBox().inflate(radius), o -> o != mob && o.isAlive());
        if (flock.isEmpty()) return false;
        double x = 0, y = 0, z = 0;
        for (T o : flock) { x += o.getX(); y += o.getY(); z += o.getZ(); }
        centre = new Vec3(x / flock.size(), y / flock.size(), z / flock.size());
        return mob.position().distanceTo(centre) > maxSpread;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone() && mob.position().distanceTo(centre) > maxSpread * 0.5;
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(centre.x, centre.y, centre.z, speed);
    }
}

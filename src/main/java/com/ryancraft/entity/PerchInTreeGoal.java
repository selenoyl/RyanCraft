package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Birds like trees: every so often a bird picks the nearest open spot on top of the leaves (a leaf block with room to
 * stand above it) and flies there, settles onto it and sits a good while, before flying off again. Being spooked or
 * hunting (higher-priority goals) still comes first.
 */
public class PerchInTreeGoal extends Goal {
    private final Mob bird;
    private final double speed;
    private final int chance, range;
    private BlockPos perch;
    private int ticks, sat, sitFor;

    /** @param chance one in this many ticks it thinks about perching (lower = keener on trees) */
    public PerchInTreeGoal(Mob bird, double speed, int chance, int range) {
        this.bird = bird; this.speed = speed; this.chance = chance; this.range = range;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override public boolean canUse() {
        if (bird.isPassenger() || bird.isLeashed() || bird.getRandom().nextInt(chance) != 0) return false;
        perch = findPerch();
        return perch != null;
    }

    private BlockPos findPerch() {
        BlockPos me = bird.blockPosition(), best = null; double bestD = Double.MAX_VALUE;
        var lv = bird.level();
        for (int i = 0; i < 80; i++) {
            BlockPos p = me.offset(bird.getRandom().nextInt(range * 2 + 1) - range, bird.getRandom().nextInt(19) - 6, bird.getRandom().nextInt(range * 2 + 1) - range);
            if (!lv.isLoaded(p) || !lv.getBlockState(p).is(BlockTags.LEAVES)) continue;
            BlockPos top = p;                                              // climb to the top of that leaf column
            for (int k = 0; k < 8 && lv.getBlockState(top.above()).is(BlockTags.LEAVES); k++) top = top.above();
            if (!lv.getBlockState(top.above()).isAir() || !lv.getBlockState(top.above(2)).isAir()) continue;
            double d = top.distSqr(me);
            if (d < bestD) { bestD = d; best = top.immutable(); }
        }
        return best;
    }

    @Override public void start() {
        ticks = 0; sat = 0; sitFor = 300 + bird.getRandom().nextInt(600);
        bird.getNavigation().moveTo(perch.getX() + 0.5, perch.getY() + 1.0, perch.getZ() + 0.5, speed);
    }

    @Override public boolean canContinueToUse() {
        return perch != null && (sat > 0 ? sat < sitFor : ticks < 240) && bird.level().getBlockState(perch).is(BlockTags.LEAVES);
    }

    @Override public void stop() { perch = null; bird.getNavigation().stop(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }

    @Override public void tick() {
        ticks++;
        Vec3 spot = new Vec3(perch.getX() + 0.5, perch.getY() + 1.0, perch.getZ() + 0.5);
        double d = bird.position().distanceToSqr(spot);
        if (sat > 0 || d < 1.0) {                                      // there: settle on the leaves and stay put
            sat++;
            bird.getNavigation().stop();
            bird.setNoGravity(false);
            Vec3 m = bird.getDeltaMovement();
            bird.setDeltaMovement(0, Math.min(m.y, 0), 0);
            bird.setZza(0); bird.setXxa(0); bird.setYya(0);
        } else if (bird.getNavigation().isDone() && ticks % 20 == 0) {
            bird.getNavigation().moveTo(spot.x, spot.y, spot.z, speed);
        }
    }
}

package com.ryancraft.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/** Wanders over to food lying on the ground and eats it. Raccoons, opossums and bears can't resist. */
public class EatDroppedFoodGoal extends Goal {
    private final PathfinderMob mob;
    private final Predicate<ItemStack> food;
    private final double speed;
    private ItemEntity target;
    private int cooldown;

    public EatDroppedFoodGoal(PathfinderMob mob, double speed, Predicate<ItemStack> food) {
        this.mob = mob;
        this.speed = speed;
        this.food = food;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) { cooldown--; return false; }
        cooldown = 20;
        List<ItemEntity> items = mob.level().getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(10, 4, 10),
                e -> e.isAlive() && food.test(e.getItem()));
        if (items.isEmpty()) return false;
        target = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(target, speed);
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;
        mob.getLookControl().setLookAt(target);
        if (mob.distanceToSqr(target) < 2.0 && mob.level() instanceof ServerLevel level) {
            ItemStack stack = target.getItem();
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), target.getX(), target.getY() + 0.2, target.getZ(), 6, 0.1, 0.1, 0.1, 0.05);
            mob.playSound(SoundEvents.GENERIC_EAT.value(), 0.6f, 1.2f);
            stack.shrink(1);
            if (stack.isEmpty()) target.discard();
            mob.heal(2.0f);
            target = null;
        }
    }

    @Override
    public void stop() {
        target = null;
    }
}

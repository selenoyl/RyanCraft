package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** Bullfrog: a big pond frog that hops along the bank, plops into the water when startled, and calls 'jug-o-rum' at night. */
public class Bullfrog extends CritterAnimal {
    public Bullfrog(EntityType<? extends Bullfrog> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 5.0f, 1.2, 1.5, e -> !e.isCrouching()));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0, 80) {
            @Override protected Vec3 getPosition() {
                if (Bullfrog.this.random.nextFloat() < 0.6f) {
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                    for (int i = 0; i < 16; i++) {
                        p.set(getBlockX() + random.nextInt(13) - 6, getBlockY() + random.nextInt(5) - 2, getBlockZ() + random.nextInt(13) - 6);
                        if (level().getFluidState(p).is(FluidTags.WATER)) return Vec3.atBottomCenterOf(p);
                    }
                }
                return super.getPosition();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Hop instead of walking
        if (this.onGround() && !this.isInWater() && this.getNavigation().isInProgress() && this.random.nextInt(8) == 0) this.jumpFromGround();
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.SPIDER_EYE); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.BULLFROG, l, p); }
    @Override protected SoundEvent getAmbientSound() {
        long t = this.level().getDayTime() % 24000L;
        return t > 12000L && t < 23500L ? RyanSounds.BULLFROG : null;
    }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.FROG_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.FROG_DEATH; }
    @Override public int getAmbientSoundInterval() { return 300; }
}

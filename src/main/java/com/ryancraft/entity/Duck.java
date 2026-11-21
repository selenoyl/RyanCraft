package com.ryancraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Paddles around rivers and ponds, flutters down instead of falling, and drops a feather now and then.
 * Mallards would rather be on the water: they head for nearby streams and lakes and mostly paddle about there.
 * Tempted and bred with seeds, like chickens.
 */
public class Duck extends RyanAnimal {
    private int featherTime;

    public Duck(EntityType<? extends Duck> type, Level level) {
        super(type, level);
        this.featherTime = this.random.nextInt(6000) + 6000;
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        // Wander, but mostly toward (and around on) open water
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0, 60) {
            @Override protected Vec3 getPosition() {
                boolean wet = Duck.this.isInWater();
                if (Duck.this.random.nextFloat() < (wet ? 0.9f : 0.65f)) {
                    Vec3 water = findWater(wet ? 8 : 16);
                    if (water != null) return water;
                }
                return wet ? null : super.getPosition();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** A spot on the water surface within range (open sky above), or null. */
    private Vec3 findWater(int range) {
        net.minecraft.core.BlockPos.MutableBlockPos p = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int i = 0; i < 24; i++) {
            p.set(this.getBlockX() + this.random.nextInt(range * 2 + 1) - range, this.getBlockY() + this.random.nextInt(7) - 4,
                    this.getBlockZ() + this.random.nextInt(range * 2 + 1) - range);
            if (this.level().getFluidState(p).is(net.minecraft.tags.FluidTags.WATER) && this.level().getBlockState(p.above()).isAir()) {
                return Vec3.atBottomCenterOf(p);
            }
        }
        return null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Flutter down like a chicken
        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && !this.isInWater() && motion.y < 0.0) {
            this.setDeltaMovement(motion.multiply(1.0, 0.6, 1.0));
        }
        this.fallDistance = 0;

        if (this.level() instanceof ServerLevel serverLevel && this.isAlive() && !this.isBaby() && --this.featherTime <= 0) {
            this.spawnAtLocation(serverLevel, Items.FEATHER);
            this.featherTime = this.random.nextInt(6000) + 6000;
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.CHICKEN_FOOD);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return RyanEntities.DUCK.create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.75f;
    }
}

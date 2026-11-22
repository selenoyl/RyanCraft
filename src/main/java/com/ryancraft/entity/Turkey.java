package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Eastern wild turkey. Struts around with its tail fanned, gobbles at dawn and when thunder rolls,
 * bolts when spooked and glides down from heights. Drops turkey and feathers.
 */
public class Turkey extends RyanAnimal {
    public Turkey(EntityType<? extends Turkey> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Poults run; grown birds stand and fight, and the flock piles in
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6) {
            @Override public boolean canUse() { return Turkey.this.isBaby() && super.canUse(); }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new StayWithFlockGoal<>(this, Turkey.class, 1.0, 24, 6));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this) {
            @Override public boolean canUse() { return !Turkey.this.isBaby() && super.canUse(); }
        }.setAlertOthers(Turkey.class));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && !this.isInWater() && motion.y < 0.0) {
            this.setDeltaMovement(motion.multiply(1.0, 0.65, 1.0));   // glide down
        }
        this.fallDistance = 0;
        // Shock gobble: toms answer thunder
        if (this.level() instanceof ServerLevel && this.level().isThundering() && !this.isBaby()
                && this.random.nextInt(600) == 0) {
            this.playSound(RyanSounds.TURKEY_GOBBLE, 1.5f, this.getVoicePitch());
        }
    }

    /** Gobbles far more at dawn, like the real thing. */
    @Override
    public int getAmbientSoundInterval() {
        long time = this.level().getDayTime() % 24000L;
        boolean dawn = time > 22500L || time < 1500L;
        return dawn ? 120 : 400;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.CHICKEN_FOOD) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return RyanEntities.TURKEY.create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() || this.random.nextInt(3) == 0 ? RyanSounds.TURKEY_CLUCK : RyanSounds.TURKEY_GOBBLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return RyanSounds.TURKEY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return RyanSounds.TURKEY_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.3f;
    }
}

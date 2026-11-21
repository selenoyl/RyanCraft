package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * A smaller, wilder, untameable wolf. Coyotes run in loose packs, leave you alone unless you crowd them,
 * pile in together if one is hit, and break off and run once they're badly hurt. They hunt small game,
 * and yip and howl at night.
 */
public class Coyote extends CritterAnimal {
    private boolean fleeing;
    private int healTimer;

    public Coyote(EntityType<? extends Coyote> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 16.0f, 1.3, 1.6, e -> this.fleeing));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.35f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.35, true) {
            @Override public boolean canUse() { return !Coyote.this.fleeing && super.canUse(); }
            @Override public boolean canContinueToUse() { return !Coyote.this.fleeing && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new StayWithFlockGoal<>(this, Coyote.class, 1.1, 24, 7));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override public boolean canUse() { return !Coyote.this.fleeing && super.canUse(); }
        }.setAlertOthers(Coyote.class));
        // Crowd a coyote and it snaps
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.fleeing && !this.isBaby() && crowding(target)));
        // Small game, now and then
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Chicken.class, 200, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, 200, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Chipmunk.class, 240, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Squirrel.class, 300, true, false, (t, l) -> !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Duck.class, 300, true, false, (t, l) -> !this.isBaby()));
    }

    private boolean crowding(LivingEntity target) {
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        return this.distanceTo(target) < (target.isCrouching() ? 2.5 : 4.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!fleeing && !this.isBaby() && this.getHealth() <= this.getMaxHealth() * 0.35f) {
            fleeing = true;
            this.setTarget(null);
        } else if (fleeing && this.getHealth() >= this.getMaxHealth() * 0.7f) {
            fleeing = false;
        }
        if (fleeing && this.getTarget() != null) this.setTarget(null);
        if (this.getTarget() == null && this.getHealth() < this.getMaxHealth() && ++healTimer >= 100) {
            healTimer = 0;
            this.heal(1.0f);
        }
        // Howl at night; the pack answers
        long t = level.getDayTime() % 24000L;
        boolean night = t > 13000L && t < 23000L;
        if (night && !this.isBaby() && this.random.nextInt(2400) == 0) {
            this.playSound(RyanSounds.COYOTE_HOWL, 3.0f, 0.95f + this.random.nextFloat() * 0.1f);
            for (Coyote other : level.getEntitiesOfClass(Coyote.class, this.getBoundingBox().inflate(32), o -> o != this)) {
                if (this.random.nextBoolean()) other.playSound(RyanSounds.COYOTE_YIPS, 2.0f, 1.0f + this.random.nextFloat() * 0.15f);
            }
        }
    }

    @Override
    public boolean isFood(ItemStack s) { return s.is(Items.CHICKEN) || s.is(Items.RABBIT) || s.is(Items.MUTTON); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.COYOTE, l, p); }

    @Override
    protected SoundEvent getAmbientSound() {
        long t = this.level().getDayTime() % 24000L;
        return (t > 12500L && t < 23500L) || this.getTarget() != null ? RyanSounds.COYOTE_YIPS : null;
    }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return net.minecraft.sounds.SoundEvents.FOX_HURT; }
    @Override protected SoundEvent getDeathSound() { return net.minecraft.sounds.SoundEvents.FOX_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.85f; }
    @Override public int getAmbientSoundInterval() { return 400; }
}

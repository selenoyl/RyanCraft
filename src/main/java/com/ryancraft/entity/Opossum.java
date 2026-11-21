package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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

/** Hisses when you get close, and when it gets hurt it keels over and plays possum for a while. */
public class Opossum extends CritterAnimal {
    private static final EntityDataAccessor<Boolean> PLAYING_DEAD = SynchedEntityData.defineId(Opossum.class, EntityDataSerializers.BOOLEAN);
    private int deadTicks;

    public Opossum(EntityType<? extends Opossum> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override protected boolean climbs() { return true; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLAYING_DEAD, false);
    }

    @Override public boolean isPlayingDead() { return this.entityData.get(PLAYING_DEAD); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(3, new EatDroppedFoodGoal(this, 1.0, this::isFood));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive() && !isPlayingDead()) {
            this.entityData.set(PLAYING_DEAD, true);
            deadTicks = 100 + this.random.nextInt(100);
            this.getNavigation().stop();
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && isPlayingDead() && --deadTicks <= 0) {
            this.entityData.set(PLAYING_DEAD, false);
        }
    }

    @Override protected boolean isImmobile() { return super.isImmobile() || isPlayingDead(); }

    @Override
    public boolean isFood(ItemStack s) {
        return s.is(Items.SWEET_BERRIES) || s.is(Items.EGG) || s.is(Items.ROTTEN_FLESH) || s.is(Items.APPLE) || s.is(Items.MELON_SLICE);
    }

    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.OPOSSUM, l, p); }
    @Override protected SoundEvent getAmbientSound() { return isPlayingDead() ? null : RyanSounds.OPOSSUM_HISS; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.OPOSSUM_HISS; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.OPOSSUM_HISS; }
    @Override public int getAmbientSoundInterval() { return 300; }
}

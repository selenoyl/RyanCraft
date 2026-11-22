package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * American crow. Wary and clever: keeps its distance from strangers, but tame one with seeds the way you'd tame a
 * parrot and it's yours: it follows you, sits when told (right-click), rides on your shoulder (stand still near it
 * and it hops up, or sneak-right-click it), and lives happily in a bird cage.
 */
public class Crow extends ShoulderRidingEntity implements FlyingAnimal, CritterVisual {
    public Crow(EntityType<? extends Crow> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.FLYING_SPEED, 0.45).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.0, 1.3,
                e -> !this.isTame() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && !e.isCrouching()));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 5.0f, 1.0f));
        this.goalSelector.addGoal(4, new LandOnOwnersShoulderGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame() && stack.is(ItemTags.PARROT_FOOD)) {
            this.usePlayerItem(player, hand, stack);
            if (!this.isSilent()) this.playSound(RyanSounds.CROW_CAW, 1.0f, 1.2f);
            if (!this.level().isClientSide()) {
                if (this.random.nextInt(6) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }
        // Sneak-click your crow with an empty hand and it hops up onto your shoulder
        if (this.isTame() && this.isOwnedBy(player) && player.isSecondaryUseActive() && stack.isEmpty()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                this.setOrderedToSit(false);
                this.setEntityOnShoulder(sp);
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isTame() && this.isOwnedBy(player) && !this.isFlying()) {
            if (!this.level().isClientSide()) this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));
        this.fallDistance = 0;
        // Unlike a parrot, a crow comes to you: stand still nearby for a moment and it settles on your shoulder
        if (this.isTame() && !this.isOrderedToSit() && !this.isLeashed() && this.canSitOnShoulder()
                && this.getOwner() instanceof net.minecraft.server.level.ServerPlayer owner && owner.onGround() && !owner.isSpectator()
                && owner.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && this.distanceToSqr(owner) < 9.0) {
            if (++stillTicks > 60) { stillTicks = 0; this.setEntityOnShoulder(owner); }
        } else {
            stillTicks = 0;
        }
    }

    private int stillTicks;

    /** Spawns like the other critters: on grassy ground in daylight. */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return super.checkSpawnRules(level, reason);
        BlockPos below = this.blockPosition().below();
        return level.getBlockState(below).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(this.blockPosition(), 0) > 8;
    }

    @Override public boolean isFlying() { return !this.onGround(); }
    @Override public boolean isFlyingVisual() { return airborne(); }
    @Override public int getVariant() { return 0; }
    @Override public boolean isFood(ItemStack s) { return s.is(net.minecraft.world.item.Items.SWEET_BERRIES) || s.is(net.minecraft.world.item.Items.GLOW_BERRIES); }
    @Override public @Nullable AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override public boolean canMate(Animal other) { return false; }

    @Override protected SoundEvent getAmbientSound() { return RyanSounds.CROW_CAW; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public int getAmbientSoundInterval() { return 240; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.95f; }
    @Override public boolean isSittingVisual() { return this.isInSittingPose(); }
}

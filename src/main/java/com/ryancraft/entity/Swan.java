package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Swans glide about lakes and slow rivers in pairs, rarely leaving the water. Mute swans are white; black swans are a rare
 * sight. Both hiss and come at you if you crowd their cygnets. Feed them grain or seagrass.
 */
public class Swan extends CritterAnimal {
    public Swan(EntityType<? extends Swan> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override protected int variantCount() { return 2; }
    /** About one in fifteen is a black swan. */
    @Override protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) { return this.random.nextInt(15) == 0 ? 1 : 0; }
    @Override protected String variantName() { return getVariant() == 1 ? "black" : null; }

    private boolean guardingCygnets(LivingEntity target) {
        if (this.isBaby() || !(target instanceof Player p) || p.isCreative() || p.isSpectator()) return false;
        return this.distanceTo(target) < 5 && !this.level().getEntitiesOfClass(Swan.class, this.getBoundingBox().inflate(8), Swan::isBaby).isEmpty();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new StayWithFlockGoal<>(this, Swan.class, 1.0, 16, 2));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0, 60) {          // mostly out on open water
            @Override protected Vec3 getPosition() {
                boolean wet = Swan.this.isInWater();
                if (Swan.this.random.nextFloat() < (wet ? 0.95f : 0.8f)) {
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                    int range = wet ? 10 : 16;
                    for (int i = 0; i < 24; i++) {
                        p.set(getBlockX() + random.nextInt(range * 2 + 1) - range, getBlockY() + random.nextInt(7) - 4, getBlockZ() + random.nextInt(range * 2 + 1) - range);
                        if (level().getFluidState(p).is(FluidTags.WATER) && level().getBlockState(p.above()).isAir()) return Vec3.atBottomCenterOf(p);
                    }
                }
                return wet ? null : super.getPosition();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(Swan.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (t, l) -> guardingCygnets(t)));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && !this.isInWater() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));
        this.fallDistance = 0;
        LivingEntity t = this.getTarget();
        if (t != null && this.getLastHurtByMob() != t && !guardingCygnets(t) && this.distanceTo(t) > 8) this.setTarget(null);
    }

    /** Grain and water plants. */
    @Override public boolean isFood(ItemStack s) { return s.is(ItemTags.CHICKEN_FOOD) || s.is(Items.SEAGRASS) || s.is(Items.WHEAT); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.SWAN, l, p); }
    @Override protected SoundEvent getAmbientSound() { return this.getTarget() != null ? RyanSounds.GOOSE_HISS : null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.GOOSE_HISS; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.GOOSE_HISS; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.8f; }
}

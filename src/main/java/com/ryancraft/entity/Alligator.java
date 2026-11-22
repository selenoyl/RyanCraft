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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * American alligator: lurks low in swamp water with just its eyes showing and bellows in the evening. Slow on land, quick in
 * the water. Come within lunging range at the water's edge and it grabs you.
 */
public class Alligator extends CritterAnimal {
    public Alligator(EntityType<? extends Alligator> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.16)
                .add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6).add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0);
    }

    private boolean inRange(LivingEntity t) {
        if (t instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        return this.distanceTo(t) < (this.isInWater() ? 6 : 3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.8, true));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0, 100) {
            @Override protected Vec3 getPosition() {
                if (Alligator.this.random.nextFloat() < 0.8f) {
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                    for (int i = 0; i < 16; i++) {
                        p.set(getBlockX() + random.nextInt(17) - 8, getBlockY() + random.nextInt(5) - 2, getBlockZ() + random.nextInt(17) - 8);
                        if (level().getFluidState(p).is(FluidTags.WATER)) return Vec3.atBottomCenterOf(p);
                    }
                }
                return super.getPosition();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (t, l) -> inRange(t)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Chicken.class, 100, true, false, (t, l) -> inRange(t)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Duck.class, 100, true, false, (t, l) -> inRange(t)));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        LivingEntity t = this.getTarget();
        if (t != null && this.getLastHurtByMob() != t && this.distanceTo(t) > 10) this.setTarget(null);
    }

    @Override public boolean isFood(ItemStack s) { return s.is(net.minecraft.world.item.Items.CHICKEN) || s.is(com.ryancraft.RyanItems.FISH_FOOD); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.ALLIGATOR, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.ALLIGATOR_BELLOW; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.GOOSE_HISS; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.8f; }
    @Override public int getAmbientSoundInterval() { return 600; }
}

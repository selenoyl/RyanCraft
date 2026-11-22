package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** Great blue heron: stalks the shallows on stilt legs, stands stock still, and spears fish. Croaks and stalks off if you come close. */
public class GreatBlueHeron extends CritterAnimal {
    public GreatBlueHeron(EntityType<? extends GreatBlueHeron> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    /** Shallow water: water with solid ground right under it. */
    private Vec3 shallows() {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 20; i++) {
            p.set(getBlockX() + random.nextInt(21) - 10, getBlockY() + random.nextInt(5) - 2, getBlockZ() + random.nextInt(21) - 10);
            if (level().getFluidState(p).is(FluidTags.WATER) && !level().getFluidState(p.below()).is(FluidTags.WATER) && level().getBlockState(p.above()).isAir())
                return Vec3.atBottomCenterOf(p);
        }
        return null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 10.0f, 1.0, 1.4,
                e -> !e.isCrouching() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8, 120) {
            @Override protected Vec3 getPosition() {
                Vec3 v = GreatBlueHeron.this.random.nextFloat() < 0.8f ? shallows() : null;
                return v != null ? v : super.getPosition();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, AbstractFish.class, 80, true, false, null));
    }

    @Override public boolean isFood(ItemStack s) { return false; }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.HERON_CROAK; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.HERON_CROAK; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public int getAmbientSoundInterval() { return 800; }
}

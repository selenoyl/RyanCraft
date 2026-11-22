package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** River otter: a playful swimmer of creeks and rivers. Spends much of its time in the water, catching fish. */
public class RiverOtter extends CritterAnimal {
    public RiverOtter(EntityType<? extends RiverOtter> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.2, 1.5,
                e -> !e.isCrouching() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 50) {
            @Override protected Vec3 getPosition() {
                if (RiverOtter.this.random.nextFloat() < 0.7f) {
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                    for (int i = 0; i < 16; i++) {
                        p.set(RiverOtter.this.getBlockX() + RiverOtter.this.random.nextInt(17) - 8, RiverOtter.this.getBlockY() + RiverOtter.this.random.nextInt(5) - 3,
                                RiverOtter.this.getBlockZ() + RiverOtter.this.random.nextInt(17) - 8);
                        if (RiverOtter.this.level().getFluidState(p).is(FluidTags.WATER)) return Vec3.atBottomCenterOf(p);
                    }
                }
                return super.getPosition();
            }
        });
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, AbstractFish.class, 60, true, false, null));
    }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.COD) || s.is(Items.SALMON) || s.is(com.ryancraft.RyanItems.FISH_FOOD); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.RIVER_OTTER, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.MINK_SQUEAK; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.MINK_SQUEAK; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.MINK_SQUEAK; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.85f; }
}

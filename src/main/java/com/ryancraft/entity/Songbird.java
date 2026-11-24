package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Kentucky songbirds, each with its own song: Northern Cardinal (the state bird),
 * American Goldfinch, American Robin, Blue Jay, Eastern Bluebird and Mourning Dove.
 * Shy: they flit off when you come close, unless you win one over with seeds. Catch them with a bundle and
 * keep them in a bird cage.
 */
public class Songbird extends CritterAnimal implements FlyingAnimal, com.ryancraft.bag.Baggable {
    public static final int CARDINAL = 0, GOLDFINCH = 1, ROBIN = 2, BLUE_JAY = 3, BLUEBIRD = 4, MOURNING_DOVE = 5;
    private static final int[] WEIGHTS = {24, 14, 20, 14, 8, 20};   // sums to 100

    public Songbird(EntityType<? extends Songbird> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.FLYING_SPEED, 0.5).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override protected int variantCount() { return 6; }
    @Override protected boolean skittish() { return true; }
    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.BIRD; }

    @Override
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        int roll = this.random.nextInt(100);
        for (int i = 0; i < WEIGHTS.length; i++) { if ((roll -= WEIGHTS[i]) < 0) return i; }
        return CARDINAL;
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
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.8, this::isFood, true) {
            @Override public boolean canUse() { return !Songbird.this.isTrusting() && super.canUse(); }
        });
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 7.0f, 1.2, 1.5,
                e -> !this.isTrusting() && net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(4, new PerchInTreeGoal(this, 1.0, 60, 14));   // songbirds spend most of their time up in the trees
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));
        this.fallDistance = 0;
    }

    @Override public boolean isFlying() { return !this.onGround(); }
    @Override public boolean isFlyingVisual() { return airborne(); }

    @Override public boolean isFood(ItemStack s) { return s.is(ItemTags.CHICKEN_FOOD) || s.is(net.minecraft.world.item.Items.SWEET_BERRIES); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.SONGBIRD, l, p); }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (getVariant()) {
            case GOLDFINCH -> RyanSounds.SONG_GOLDFINCH;
            case ROBIN -> RyanSounds.SONG_ROBIN;
            case BLUE_JAY -> RyanSounds.SONG_BLUE_JAY;
            case BLUEBIRD -> RyanSounds.SONG_BLUEBIRD;
            case MOURNING_DOVE -> RyanSounds.SONG_DOVE;
            default -> RyanSounds.SONG_CARDINAL;
        };
    }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public int getAmbientSoundInterval() { return 160; }
    @Override protected float getSoundVolume() { return 0.8f; }
    @Override protected String variantName() { return switch (getVariant()) { case GOLDFINCH -> "goldfinch"; case ROBIN -> "robin"; case BLUE_JAY -> "blue_jay"; case BLUEBIRD -> "bluebird"; case MOURNING_DOVE -> "mourning_dove"; default -> null; }; }
}

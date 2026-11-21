package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;

/**
 * Eastern gray squirrel (deep woods) and fox squirrel (open woods and edges).
 * Quick, nervous, and straight up the nearest trunk when you come near.
 */
public class Squirrel extends CritterAnimal {
    public static final int GRAY = 0, FOX = 1;

    public Squirrel(EntityType<? extends Squirrel> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.32);
    }

    @Override protected int variantCount() { return 2; }

    /** Fox squirrels are the bigger of the two; eastern grays are noticeably smaller. */
    @Override
    protected void onVariantSet(int variant) {
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) scale.setBaseValue(variant == FOX ? 1.15 : 0.85);
    }
    @Override protected boolean climbs() { return true; }

    @Override
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        var biome = level.getBiome(pos);
        boolean openWoods = biome.is(Biomes.PLAINS) || biome.is(Biomes.MEADOW) || biome.is(BiomeTags.IS_SAVANNA)
                || biome.unwrapKey().map(k -> k.identifier().getPath().equals("shrubland")).orElse(false);
        return this.random.nextFloat() < (openWoods ? 0.75f : 0.25f) ? FOX : GRAY;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.5, 1.9,
                e -> !e.isCrouching() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, true));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack s) { return s.is(Items.SWEET_BERRIES) || s.is(Items.PUMPKIN_SEEDS) || s.is(Items.MELON_SEEDS) || s.is(Items.COCOA_BEANS); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.SQUIRREL, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.SQUIRREL_CHATTER; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.SQUIRREL_CHATTER; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.SQUIRREL_CHATTER; }
    @Override public int getAmbientSoundInterval() { return 240; }
    @Override protected String variantName() { return getVariant() == FOX ? "fox" : null; }
}

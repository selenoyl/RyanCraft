package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Pileated woodpecker: the big red-crested one. Flies from trunk to trunk and drums loud enough to hear across the woods. */
public class PileatedWoodpecker extends SmallFlyer {
    public PileatedWoodpecker(EntityType<? extends PileatedWoodpecker> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.FLYING_SPEED, 0.5).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override protected boolean climbs() { return true; }

    @Override
    public void aiStep() {
        super.aiStep();
        // Drum on a nearby trunk now and then
        if (this.level() instanceof ServerLevel && this.random.nextInt(300) == 0) {
            BlockPos p = this.blockPosition();
            for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, 0, -1), p.offset(1, 1, 1))) {
                if (this.level().getBlockState(q).is(BlockTags.LOGS)) { this.playSound(RyanSounds.WOODPECKER, 2.0f, 0.85f); break; }
            }
        }
    }

    @Override public boolean isFood(ItemStack s) { return s.is(net.minecraft.world.item.Items.SWEET_BERRIES) || s.is(net.minecraft.world.item.Items.GLOW_BERRIES); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
}

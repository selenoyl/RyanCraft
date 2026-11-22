package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/** Elk: big herds in mountain meadows and open woods. Bulls bugle at dawn and dusk and stand their ground; cows run. */
public class Elk extends BigGame {
    public Elk(EntityType<? extends Elk> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.4).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override protected boolean stagedAntlers() { return true; }
    @Override protected String maleName() { return "bull"; }
    @Override protected float maleChance() { return 0.3f; }
    @Override protected float spookDistance() { return 14f; }
    @Override protected boolean fightsBack() { return isMale() && !this.isBaby(); }
    @Override protected int herdSize() { return 8; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || !isMale() || this.isBaby()) return;
        long t = level.getDayTime() % 24000L;
        boolean twilight = t > 22500L || t < 1500L || (t > 11500L && t < 13500L);
        if (this.random.nextInt(twilight ? 600 : 6000) == 0) this.playSound(RyanSounds.ELK_BUGLE, 4.0f, 0.95f + this.random.nextFloat() * 0.1f);
    }

    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.ELK, l, p); }
    @Override protected SoundEvent getAmbientSound() { return isMale() ? null : RyanSounds.ELK_CHIRP; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.ELK_CHIRP; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.CAMEL_DEATH; }
    @Override public int getAmbientSoundInterval() { return 500; }
}

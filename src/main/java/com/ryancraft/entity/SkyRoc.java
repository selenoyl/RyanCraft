package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Sky roc: the Skylands' great soaring bird, pale as cloud with storm-grey flight feathers, circling wide and high over
 * the islands. It nests on the highest islands (worldgen/RocNestFeature) and its eggs hatch roc chicks. Leave it be
 * and it leaves you be; strike it and it strikes back hard. Feed a pair mutton to breed them.
 */
public class SkyRoc extends SoaringBird {
    public SkyRoc(EntityType<? extends SkyRoc> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override protected double soarHeight() { return 30; }
    @Override protected double soarRadius() { return 34; }
    @Override protected int soarTime() { return 1200 + this.random.nextInt(2400); }

    @Override
    protected void addPrey() { this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); }

    @Override public boolean isFood(ItemStack s) { return s.is(Items.MUTTON) || s.is(Items.COOKED_MUTTON); }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return RyanEntities.SKY_ROC.create(level, EntitySpawnReason.BREEDING);
    }

    @Override protected SoundEvent getAmbientSound() { return RyanSounds.HAWK; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.6f; }      // a deep, far-carrying cry
    @Override public int getAmbientSoundInterval() { return 900; }
}

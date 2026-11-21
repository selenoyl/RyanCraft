package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Slow, unbothered, and not to be crowded. Walk right up to one (or hit it) and it sprays:
 * a reeking cloud that leaves you sick and half-blind. Sneak past and it lets you be.
 */
public class Skunk extends CritterAnimal {
    private int sprayCooldown;

    public Skunk(EntityType<? extends Skunk> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (sprayCooldown > 0) { sprayCooldown--; return; }
        if (this.isBaby() || this.tickCount % 10 != 0) return;
        // Crowded by a non-sneaking player, a wolf or a fox?
        boolean crowded = !level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5), e ->
                (e instanceof Player p && !p.isCrouching() && !p.isCreative() && !p.isSpectator() && !this.isFood(p.getMainHandItem()))
                        || e instanceof Wolf || e instanceof Fox).isEmpty();
        if (crowded) spray(level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive() && sprayCooldown <= 0) spray(level);
        return hurt;
    }

    private void spray(ServerLevel level) {
        sprayCooldown = 1200;
        this.playSound(RyanSounds.SKUNK_SPRAY, 1.0f, 1.0f);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF9AAA3A),
                this.getX(), this.getY() + 0.4, this.getZ(), 60, 1.5, 0.6, 1.5, 0.02);
        level.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getY() + 0.3, this.getZ(), 30, 1.2, 0.4, 1.2, 0.02);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0), e -> !(e instanceof Skunk))) {
            e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 240), this);
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60), this);
            if (e instanceof Animal || e instanceof Wolf) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100), this);
            }
        }
    }

    @Override
    public boolean isFood(ItemStack s) { return s.is(Items.SWEET_BERRIES) || s.is(Items.EGG) || s.is(Items.GLOW_BERRIES); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.SKUNK, l, p); }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.OPOSSUM_HISS; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.OPOSSUM_HISS; }
}

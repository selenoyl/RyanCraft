package com.ryancraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * An angry moon rabbit, summoned by the Great Moon Rabbit: red-eyed and quick, about four hearts, nipping for a heart or
 * so. One in four is a brute: bigger, darker, eight hearts, and it hits as hard as an enderman.
 */
public class AngryMoonRabbit extends Rabbit {
    private static final EntityDataAccessor<Boolean> BRUTE = SynchedEntityData.defineId(AngryMoonRabbit.class, EntityDataSerializers.BOOLEAN);

    public AngryMoonRabbit(EntityType<? extends AngryMoonRabbit> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Rabbit.createAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.36).add(Attributes.ATTACK_DAMAGE, 2.5)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(BRUTE, false); }
    public boolean isBrute() { return entityData.get(BRUTE); }

    public void makeBrute() {
        entityData.set(BRUTE, true);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(16.0); setHealth(16f);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.5);
        getAttribute(Attributes.SCALE).setBaseValue(1.35);
        getAttribute(Attributes.ARMOR).setBaseValue(4.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10f));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public boolean isFood(ItemStack s) { return false; }
    @Override public boolean canMate(Animal o) { return false; }
    @Override public @Nullable Rabbit getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override public boolean checkSpawnRules(LevelAccessor l, EntitySpawnReason r) { return r != EntitySpawnReason.NATURAL; }
}

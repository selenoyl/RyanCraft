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
 * <p>Raise one from an egg: feed a chick mutton until it trusts you (it's then yours), saddle it once grown, and ride.
 * Ridden, it flies where you look while you hold forward, and hovers, sinking slowly, when you let go.
 */
public class SkyRoc extends SoaringBird {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> TAME =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SkyRoc.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> SADDLED =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SkyRoc.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final double FLY_SPEED = 0.85;
    private java.util.@org.jspecify.annotations.Nullable UUID owner;

    public SkyRoc(EntityType<? extends SkyRoc> type, Level level) { super(type, level); }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b) {
        super.defineSynchedData(b); b.define(TAME, false); b.define(SADDLED, false);
    }

    public boolean isTame() { return entityData.get(TAME); }
    public boolean isSaddled() { return entityData.get(SADDLED); }
    @Override protected boolean canSoar() { return !isTame(); }
    @Override public boolean requiresCustomPersistence() { return isTame() || super.requiresCustomPersistence(); }
    @Override public boolean removeWhenFarAway(double d) { return !isTame() && super.removeWhenFarAway(d); }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean mine = isTame() && player.getUUID().equals(owner);
        if (isBaby() && !isTame() && isFood(stack)) {                         // win a chick over with mutton
            usePlayerItem(player, hand, stack);
            if (!level().isClientSide()) {
                if (random.nextInt(3) == 0) { entityData.set(TAME, true); owner = player.getUUID(); setTarget(null); level().broadcastEntityEvent(this, (byte) 41); }
                else level().broadcastEntityEvent(this, (byte) 40);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        if (mine && !isBaby() && !isSaddled() && stack.is(Items.SADDLE)) {
            usePlayerItem(player, hand, stack);
            if (!level().isClientSide()) { entityData.set(SADDLED, true); playSound(SoundEvents.HORSE_SADDLE.value(), 1f, 0.8f); }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        if (mine && !isBaby() && isSaddled() && !isFood(stack) && !isVehicle()) {
            if (!level().isClientSide()) player.startRiding(this);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public net.minecraft.world.entity.@org.jspecify.annotations.Nullable LivingEntity getControllingPassenger() {
        return isSaddled() && getFirstPassenger() instanceof net.minecraft.world.entity.player.Player p ? p : super.getControllingPassenger();
    }

    /** Ridden: fly along the rider's gaze while forward is held; otherwise hover, sinking slowly. */
    @Override
    public void travel(net.minecraft.world.phys.Vec3 input) {
        if (!(getControllingPassenger() instanceof net.minecraft.world.entity.player.Player p)) { super.travel(input); return; }
        setYRot(p.getYRot()); yRotO = getYRot(); setXRot(p.getXRot() * 0.5f); yBodyRot = getYRot(); yHeadRot = getYRot();
        net.minecraft.world.phys.Vec3 want = p.zza > 0 ? p.getLookAngle().scale(FLY_SPEED * p.zza)
                : p.zza < 0 ? p.getLookAngle().scale(-0.2) : new net.minecraft.world.phys.Vec3(0, -0.03, 0);
        setDeltaMovement(getDeltaMovement().lerp(want, 0.12));
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
        resetFallDistance();
        calculateEntityAnimation(true);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putBoolean("Tame", isTame()); out.putBoolean("Saddled", isSaddled());
        if (owner != null) out.putString("Owner", owner.toString());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) {
        super.readAdditionalSaveData(in);
        entityData.set(TAME, in.getBooleanOr("Tame", false)); entityData.set(SADDLED, in.getBooleanOr("Saddled", false));
        owner = in.getString("Owner").map(java.util.UUID::fromString).orElse(null);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override protected double soarHeight() { return 30; }
    @Override protected double soarRadius() { return 34; }
    @Override protected int soarTime() { return 1200 + this.random.nextInt(2400); }

    @Override
    protected void addPrey() { this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); }

    /** A tame roc never turns on its owner. */
    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        return !(isTame() && owner != null && target.getUUID().equals(owner)) && super.canAttack(target);
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);
        if (isSaddled()) spawnAtLocation(level, new ItemStack(Items.SADDLE));
    }

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

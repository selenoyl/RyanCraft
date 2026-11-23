package com.ryancraft.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A muzzle-loading cannon on a wooden carriage. Load it in order: gunpowder, then a shot. Light it with flint and steel
 * or a fire charge; after a short fuse it fires where the barrel points. Then it must cool and be swabbed: eight seconds
 * before it takes powder again. Climb on (empty hand) to aim it: the barrel follows your look, and you can load and
 * light it from the seat. Left alone it holds its aim. Break it (a few hits) to pick it up.
 */
public class CannonEntity extends Entity {
    public static final int RELOAD = 160, FUSE = 30;
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.INT);   // 0 empty, 1 powder, 2 loaded
    private static final EntityDataAccessor<Integer> SHOT = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FUSE_LEFT = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RECOIL = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.INT);
    private int cooldown, hits;
    private @Nullable LivingEntity gunner;

    public CannonEntity(EntityType<? extends CannonEntity> type, Level level) { super(type, level); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(STAGE, 0); b.define(SHOT, 0); b.define(FUSE_LEFT, 0); b.define(RECOIL, 0);
    }

    public int stage() { return entityData.get(STAGE); }
    public float recoil(float partial) { int r = entityData.get(RECOIL); return r <= 0 ? 0 : (r - partial) / 10f; }
    public boolean lit() { return entityData.get(FUSE_LEFT) > 0; }

    @Override public boolean isPickable() { return !isRemoved(); }
    @Override public boolean canBeCollidedWith(@Nullable Entity e) { return true; }
    @Override public boolean isPushable() { return false; }
    @Override protected boolean canAddPassenger(Entity e) { return getPassengers().isEmpty(); }
    @Override public @Nullable LivingEntity getControllingPassenger() { return getFirstPassenger() instanceof Player p ? p : null; }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity e, EntityDimensions d, float scale) {
        return new Vec3(0, 0.15, -1.0).yRot(-getYRot() * Mth.DEG_TO_RAD);              // the gunner sits behind the breech
    }

    @Override
    public void tick() {
        super.tick();
        if (!isNoGravity()) setDeltaMovement(getDeltaMovement().add(0, -0.04, 0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.5, 0.98, 0.5));
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(0.6, 0, 0.6));

        if (getControllingPassenger() instanceof Player p) {                            // aim with the gunner's look
            setYRot(p.getYRot()); setXRot(Mth.clamp(p.getXRot(), -40f, 12f));
            yRotO = getYRot(); xRotO = getXRot();
        }
        int r = entityData.get(RECOIL);
        if (r > 0) entityData.set(RECOIL, r - 1);
        if (level().isClientSide()) {
            if (lit()) level().addParticle(ParticleTypes.SMOKE, getX() - muzzleDir().x * 0.6, getY() + 0.9, getZ() - muzzleDir().z * 0.6, 0, 0.03, 0);
            return;
        }
        if (cooldown > 0) cooldown--;
        int fuse = entityData.get(FUSE_LEFT);
        if (fuse > 0) {
            if (fuse % 6 == 0) playSound(SoundEvents.TNT_PRIMED, 0.4f, 1.6f);
            entityData.set(FUSE_LEFT, fuse - 1);
            if (fuse == 1) fire();
        }
    }

    /** The way the barrel points. */
    public Vec3 muzzleDir() { return Vec3.directionFromRotation(getXRot(), getYRot()); }

    private void fire() {
        if (!(level() instanceof ServerLevel sl) || stage() != 2) return;
        ShotType type = ShotType.values()[Mth.clamp(entityData.get(SHOT), 0, ShotType.values().length - 1)];
        Vec3 dir = muzzleDir(), mouth = position().add(0, 0.75, 0).add(dir.scale(1.5));
        if (type.pellets > 0) {
            for (int i = 0; i < type.pellets; i++) {
                CannonShot b = new CannonShot(sl, gunner, mouth.x, mouth.y, mouth.z, ShotType.PELLET);
                b.shoot(dir.x, dir.y, dir.z, type.speed, type.spread);
                sl.addFreshEntity(b);
            }
        } else {
            CannonShot s = new CannonShot(sl, gunner, mouth.x, mouth.y, mouth.z, type);
            s.shoot(dir.x, dir.y, dir.z, type.speed, 0.5f);
            sl.addFreshEntity(s);
        }
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 3.0f, 0.8f + random.nextFloat() * 0.15f);
        sl.sendParticles(ParticleTypes.EXPLOSION, mouth.x, mouth.y, mouth.z, 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y, mouth.z, 24, 0.3, 0.3, 0.3, 0.04);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.8, getZ(), 6, 0.2, 0.2, 0.2, 0.02);
        setDeltaMovement(getDeltaMovement().add(dir.x * -0.5, 0.1, dir.z * -0.5));     // the kick
        entityData.set(STAGE, 0); entityData.set(RECOIL, 10);
        cooldown = RELOAD;
    }

    /** Load or light the cannon with what's in hand; used both when clicking it and from the gunner's seat. */
    public InteractionResult handle(Player player, ItemStack stack) {
        boolean client = level().isClientSide();
        if (lit()) return InteractionResult.PASS;
        if (stack.is(Items.GUNPOWDER) && stage() == 0) {
            if (cooldown > 0) {
                if (!client) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("entity.ryancraft.cannon.cooling"), true);
                return InteractionResult.SUCCESS;
            }
            if (!client) { entityData.set(STAGE, 1); stack.consume(1, player); playSound(SoundEvents.SAND_PLACE, 0.8f, 1.2f); }
            return InteractionResult.SUCCESS;
        }
        ShotType shot = Cannons.shotOf(stack);
        if (shot != null && stage() == 1) {
            if (!client) { entityData.set(STAGE, 2); entityData.set(SHOT, shot.ordinal()); stack.consume(1, player); playSound(SoundEvents.IRON_TRAPDOOR_CLOSE, 0.9f, 0.7f); }
            return InteractionResult.SUCCESS;
        }
        if ((stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) && stage() == 2) {
            if (!client) {
                gunner = player; entityData.set(FUSE_LEFT, FUSE);
                playSound(SoundEvents.FLINTANDSTEEL_USE, 1f, 1f);
                if (stack.is(Items.FIRE_CHARGE)) stack.consume(1, player); else stack.hurtAndBreak(1, player, player.getUsedItemHand());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult r = handle(player, stack);
        if (r != InteractionResult.PASS) return r;
        if (stack.isEmpty() && !player.isSecondaryUseActive() && getPassengers().isEmpty()) {
            if (!level().isClientSide()) player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isRemoved() || isInvulnerableToBase(source)) return false;
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return false;   // its own blast and its neighbours'
        boolean creative = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
        if (++hits >= 4 || creative) {
            if (!creative) spawnAtLocation(level, new ItemStack(Cannons.CANNON));
            discard();
        } else playSound(SoundEvents.ANVIL_HIT, 0.5f, 1.4f);
        return true;
    }

    @Override protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("Stage", stage()); out.putInt("Shot", entityData.get(SHOT)); out.putInt("Cooldown", cooldown); out.putInt("Fuse", entityData.get(FUSE_LEFT));
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        entityData.set(STAGE, in.getIntOr("Stage", 0)); entityData.set(SHOT, in.getIntOr("Shot", 0)); cooldown = in.getIntOr("Cooldown", 0); entityData.set(FUSE_LEFT, in.getIntOr("Fuse", 0));
    }
}

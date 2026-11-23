package com.ryancraft.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A cannon's shot in flight. Round and heated shot burst like weak TNT (heated shot sets fires); explosive shells burst
 * harder; chain shot tears through wood, leaves, fences and wool, slowing as it goes; lead balls (from grapeshot and
 * canister) only hurt what they hit.
 */
public class CannonShot extends ThrowableItemProjectile {
    private ShotType type = ShotType.ROUND;
    private int tore;

    public CannonShot(EntityType<? extends CannonShot> t, Level level) { super(t, level); }

    public CannonShot(Level level, LivingEntity owner, double x, double y, double z, ShotType type) {
        super(Cannons.SHOT, x, y, z, level, new ItemStack(Cannons.item(type)));
        setOwner(owner); this.type = type;
    }

    @Override protected Item getDefaultItem() { return Cannons.item(ShotType.ROUND); }
    @Override protected double getDefaultGravity() { return type == ShotType.PELLET ? 0.03 : 0.025; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && type != ShotType.PELLET) {
            level().addParticle(type == ShotType.HEATED ? ParticleTypes.FLAME : ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
            if (type == ShotType.CHAIN) setXRot(getXRot() + 40);                       // spinning
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (level() instanceof ServerLevel sl) hit.getEntity().hurtServer(sl, damageSources().thrown(this, getOwner()), type.damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (type == ShotType.CHAIN && level() instanceof ServerLevel sl) {
            BlockPos p = hit.getBlockPos(); BlockState s = sl.getBlockState(p);
            if (tore < 6 && tears(s)) {                                                // rips through and flies on
                sl.destroyBlock(p, true, this); tore++;
                setDeltaMovement(getDeltaMovement().scale(0.8));
                return;
            }
        }
        super.onHitBlock(hit);
    }

    private static boolean tears(BlockState s) {
        return s.is(BlockTags.LOGS) || s.is(BlockTags.PLANKS) || s.is(BlockTags.LEAVES) || s.is(BlockTags.FENCES) || s.is(BlockTags.FENCE_GATES)
                || s.is(BlockTags.WOOL) || s.is(BlockTags.WOODEN_SLABS) || s.is(BlockTags.WOODEN_STAIRS) || s.is(BlockTags.WOODEN_DOORS)
                || s.is(BlockTags.WOODEN_TRAPDOORS) || s.is(BlockTags.WOOL_CARPETS);
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hit.getType() == HitResult.Type.BLOCK && type == ShotType.CHAIN && tore < 6 && tears(level().getBlockState(((BlockHitResult) hit).getBlockPos()))) {
            onHitBlock((BlockHitResult) hit); return;
        }
        super.onHit(hit);
        if (level().isClientSide()) return;
        if (type.blast > 0)
            level().explode(this, getX(), getY(), getZ(), type.blast, type.fire, Level.ExplosionInteraction.TNT);
        else if (level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0.1, 0.1, 0.1, 0.1);
        discard();
    }

    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putString("Shot", type.name()); out.putInt("Tore", tore); }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        try { type = ShotType.valueOf(in.getStringOr("Shot", "ROUND")); } catch (IllegalArgumentException e) { type = ShotType.ROUND; }
        tore = in.getIntOr("Tore", 0);
    }
}

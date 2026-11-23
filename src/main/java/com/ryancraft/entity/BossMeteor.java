package com.ryancraft.entity;

import com.ryancraft.moon.MoonBlocks;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A meteor: batted by the Great Moon Rabbit (it flies straight and bursts on impact, cratering the arena; hit it back
 * like a ghast's fireball), or falling out of the moon's sky (moon/MoonMeteors): those never break blocks, but their
 * blast still hurts.
 */
public class BossMeteor extends Fireball {
    /** A sky meteor: its blast spares blocks. */
    private boolean gentle;
    public BossMeteor(EntityType<? extends BossMeteor> type, Level level) { super(type, level); setItem(new ItemStack(MoonBlocks.RAW_METEOR_IRON)); }

    /** A meteor out of the sky, from a point. */
    public BossMeteor(Level level, Vec3 from, Vec3 dir) {
        super(RyanEntities.BOSS_METEOR, from.x, from.y, from.z, dir.scale(0.12), level);
        setItem(new ItemStack(MoonBlocks.RAW_METEOR_IRON));
    }

    public BossMeteor(Level level, LivingEntity owner, Vec3 dir) {
        super(RyanEntities.BOSS_METEOR, owner, dir.scale(0.12), level);
        setItem(new ItemStack(MoonBlocks.RAW_METEOR_IRON));
    }

    public BossMeteor gentle() { gentle = true; return this; }
    @Override protected boolean shouldBurn() { return false; }

    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) { super.addAdditionalSaveData(out); out.putBoolean("Gentle", gentle); }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) { super.readAdditionalSaveData(in); gentle = in.getBooleanOr("Gentle", false); }
    @Override protected ParticleOptions getTrailParticle() { return ParticleTypes.END_ROD; }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) {
            level().explode(this, getX(), getY(), getZ(), gentle ? 1.4f : 1.6f, false, gentle ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.MOB);
            discard();
        }
    }
}

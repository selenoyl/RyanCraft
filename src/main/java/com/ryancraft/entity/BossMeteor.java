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

/** A baby meteor batted by the Great Moon Rabbit: flies straight, bursts on impact (no fire); hit it back like a ghast's fireball. */
public class BossMeteor extends Fireball {
    public BossMeteor(EntityType<? extends BossMeteor> type, Level level) { super(type, level); setItem(new ItemStack(MoonBlocks.RAW_METEOR_IRON)); }

    public BossMeteor(Level level, LivingEntity owner, Vec3 dir) {
        super(RyanEntities.BOSS_METEOR, owner, dir.scale(0.12), level);
        setItem(new ItemStack(MoonBlocks.RAW_METEOR_IRON));
    }

    @Override protected boolean shouldBurn() { return false; }
    @Override protected ParticleOptions getTrailParticle() { return ParticleTypes.END_ROD; }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) {
            level().explode(this, getX(), getY(), getZ(), 1.6f, false, Level.ExplosionInteraction.MOB);
            discard();
        }
    }
}

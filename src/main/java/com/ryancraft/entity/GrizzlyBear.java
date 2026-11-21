package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Grizzly: the black bear's bigger, shorter-tempered cousin of the high country. It won't come after you from far off,
 * but walk into its sight and it comes for you: a steady walk at first, then a charge once it's close. Break line of
 * sight or get well away and it loses interest. It fights nearly to the end.
 */
public class GrizzlyBear extends BlackBear {
    public GrizzlyBear(EntityType<? extends GrizzlyBear> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 9.0).add(Attributes.FOLLOW_RANGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override protected int variantCount() { return 1; }
    @Override protected String variantName() { return null; }
    @Override protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) { return 0; }
    @Override protected double crowdReach(LivingEntity target) { return target.isCrouching() ? 10.0 : 16.0; }

    /** Any player it can see within reach (sneaking gets you closer before it notices). */
    @Override
    protected boolean crowding(LivingEntity target) {
        if (target instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) return false;
        return this.distanceTo(target) < crowdReach(target) && this.hasLineOfSight(target);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Walk toward what it's after until close, then the melee goal takes over with a charge
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.Goal() {
            { this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK)); }
            @Override public boolean canUse() {
                LivingEntity t = GrizzlyBear.this.getTarget();
                return t != null && t.isAlive() && !GrizzlyBear.this.isFleeing() && GrizzlyBear.this.distanceTo(t) > 6.0;
            }
            @Override public boolean canContinueToUse() { return canUse(); }
            @Override public void tick() {
                LivingEntity t = GrizzlyBear.this.getTarget();
                if (t == null) return;
                GrizzlyBear.this.getLookControl().setLookAt(t, 30f, 30f);
                if (GrizzlyBear.this.tickCount % 10 == 0) GrizzlyBear.this.getNavigation().moveTo(t, 0.85);
            }
            @Override public void stop() { GrizzlyBear.this.getNavigation().stop(); }
        });
    }
    @Override protected float fleeAt() { return 0.2f; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.82f; }
}

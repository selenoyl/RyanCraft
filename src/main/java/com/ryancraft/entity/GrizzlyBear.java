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

/** Grizzly: the black bear's bigger, shorter-tempered cousin of the high country. Keep well back; it fights nearly to the end. */
public class GrizzlyBear extends BlackBear {
    public GrizzlyBear(EntityType<? extends GrizzlyBear> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 9.0).add(Attributes.FOLLOW_RANGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override protected int variantCount() { return 1; }
    @Override protected String variantName() { return null; }
    @Override protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) { return 0; }
    @Override protected double crowdReach(LivingEntity target) { return target.isCrouching() ? 5.0 : 8.0; }
    @Override protected float fleeAt() { return 0.2f; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.82f; }
}

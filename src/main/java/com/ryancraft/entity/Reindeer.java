package com.ryancraft.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Reindeer: the deer of the snowy north, pale-coated and travelling in herds. Antlers grow and vary just like the whitetail's. */
public class Reindeer extends Deer {
    public Reindeer(EntityType<? extends Reindeer> type, Level level) { super(type, level); }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Reindeer calf = RyanEntities.REINDEER.create(level, EntitySpawnReason.BREEDING);
        if (calf != null && partner instanceof Deer d) calf.setAntlerColor(this.random.nextBoolean() ? this.getAntlerColor() : d.getAntlerColor());
        return calf;
    }

    @Override
    protected Component getTypeName() {
        if (getAntlers() == MAX_ANTLERS) return Component.translatable("entity.ryancraft.reindeer." + (getRack() > 0 ? "nontypical" : "trophy"));
        return Component.translatable(this.getType().getDescriptionId());
    }
}

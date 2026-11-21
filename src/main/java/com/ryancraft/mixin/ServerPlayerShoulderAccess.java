package com.ryancraft.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lets the bird cage take a parrot off your shoulder. */
@Mixin(ServerPlayer.class)
public interface ServerPlayerShoulderAccess {
    @Invoker("setShoulderEntityLeft") void ryancraft$setShoulderEntityLeft(CompoundTag tag);
    @Invoker("setShoulderEntityRight") void ryancraft$setShoulderEntityRight(CompoundTag tag);
}

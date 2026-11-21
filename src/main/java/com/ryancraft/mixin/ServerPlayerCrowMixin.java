package com.ryancraft.mixin;

import com.ryancraft.entity.CrowShoulders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the synced crow flags in step with whatever is sitting on each shoulder. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerCrowMixin {
    private static boolean ryancraft$isCrow(CompoundTag tag) { return "ryancraft:crow".equals(tag.getStringOr("id", "")); }

    private void ryancraft$set(int bit, boolean on) {
        CrowShoulders c = (CrowShoulders) this;
        byte b = c.ryancraft$crowShoulders();
        c.ryancraft$setCrowShoulders((byte) (on ? (b | bit) : (b & ~bit)));
    }

    @Inject(method = "setShoulderEntityLeft", at = @At("TAIL"))
    private void ryancraft$left(CompoundTag tag, CallbackInfo ci) { ryancraft$set(1, ryancraft$isCrow(tag)); }

    @Inject(method = "setShoulderEntityRight", at = @At("TAIL"))
    private void ryancraft$right(CompoundTag tag, CallbackInfo ci) { ryancraft$set(2, ryancraft$isCrow(tag)); }
}

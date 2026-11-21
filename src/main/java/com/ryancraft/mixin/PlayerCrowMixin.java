package com.ryancraft.mixin;

import com.ryancraft.entity.CrowShoulders;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Syncs whether a crow is riding either shoulder (vanilla only syncs parrot colours). */
@Mixin(Player.class)
public abstract class PlayerCrowMixin implements CrowShoulders {
    @Unique private static final EntityDataAccessor<Byte> RYANCRAFT_CROWS = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void ryancraft$defineCrows(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(RYANCRAFT_CROWS, (byte) 0);
    }

    @Override public byte ryancraft$crowShoulders() { return ((Player) (Object) this).getEntityData().get(RYANCRAFT_CROWS); }
    @Override public void ryancraft$setCrowShoulders(byte bits) { ((Player) (Object) this).getEntityData().set(RYANCRAFT_CROWS, bits); }
}

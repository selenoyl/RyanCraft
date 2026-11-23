package com.ryancraft.mixin;

import com.ryancraft.entity.Pegasus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.equine.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A horse bred with a pegasus has a winged hybrid foal, whichever of the pair the game asks for the foal. */
@Mixin(Horse.class)
public abstract class HorseBreedPegasusMixin {
    @Inject(method = "getBreedOffspring", at = @At("HEAD"), cancellable = true)
    private void ryancraft$pegasusFoal(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<AgeableMob> cir) {
        if (!((Object) this instanceof Pegasus) && partner instanceof Pegasus p) cir.setReturnValue(p.getBreedOffspring(level, (Horse) (Object) this));
    }
}

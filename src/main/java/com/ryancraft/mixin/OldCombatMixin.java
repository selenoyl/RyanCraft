package com.ryancraft.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Old combat (config oldCombat): attack strength is always full, so there is no cooldown to wait out and the hotbar's
 * attack indicator never shows. Weapons with their own charge (spears: piercing or kinetic, or a minimum attack charge)
 * keep the vanilla timing.
 */
@Mixin(Player.class)
public abstract class OldCombatMixin {
    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noCooldown(float partial, CallbackInfoReturnable<Float> cir) {
        if (!RyanConfig.get().oldCombat) return;
        ItemStack held = ((Player) (Object) this).getMainHandItem();
        if (held.has(DataComponents.PIERCING_WEAPON) || held.has(DataComponents.KINETIC_WEAPON) || held.has(DataComponents.MINIMUM_ATTACK_CHARGE)) return;
        cir.setReturnValue(1.0f);
    }
}

package com.ryancraft.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ryancraft.RyanConfig;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Old combat has no attack cooldown, so the attack indicator under the crosshair (and on the hotbar) has nothing to
 * show: hide it, unless you're holding a weapon that still charges (a spear).
 */
@Mixin(Gui.class)
public abstract class AttackIndicatorMixin {
    @WrapOperation(method = {"renderCrosshair", "renderItemHotbar"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object ryancraft$noIndicator(OptionInstance<?> option, Operation<Object> original) {
        Object v = original.call(option);
        if (!(v instanceof AttackIndicatorStatus) || !RyanConfig.get().oldCombat) return v;
        var player = Minecraft.getInstance().player;
        ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        boolean charges = held.has(DataComponents.PIERCING_WEAPON) || held.has(DataComponents.KINETIC_WEAPON) || held.has(DataComponents.MINIMUM_ATTACK_CHARGE);
        return charges ? v : AttackIndicatorStatus.OFF;
    }
}

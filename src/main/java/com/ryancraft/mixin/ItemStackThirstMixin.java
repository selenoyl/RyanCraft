package com.ryancraft.mixin;

import com.ryancraft.thirst.Thirst;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Thirst: drinks (water bottles, milk, potions) and juicy foods (stews, melon, berries) quench thirst when finished. */
@Mixin(ItemStack.class)
public abstract class ItemStackThirstMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void ryancraft$quench(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (!level.isClientSide() && entity instanceof ServerPlayer p) Thirst.consumed(p, (ItemStack) (Object) this);
    }
}

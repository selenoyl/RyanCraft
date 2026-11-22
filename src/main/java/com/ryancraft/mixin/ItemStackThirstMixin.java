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
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void ryancraft$quench(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        // a water bottle goes down in sips: keep the bottle (a little emptier) until the last one
        if (Thirst.enabled() && entity instanceof net.minecraft.world.entity.player.Player player && Thirst.isWater(self) && Thirst.sipsLeft(self) > 1) {
            int sips = Thirst.sipsLeft(self);
            Thirst.sip(level, player, sips);
            Thirst.setSipsLeft(self, sips - 1);
            level.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_DRINK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.1f);
            cir.setReturnValue(self);
            return;
        }
        if (!level.isClientSide() && entity instanceof ServerPlayer p) Thirst.consumed(p, self);
    }
}

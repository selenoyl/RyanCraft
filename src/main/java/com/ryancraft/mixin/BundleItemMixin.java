package com.ryancraft.mixin;

import com.ryancraft.bag.MobBag;
import com.ryancraft.bag.MobBags;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A bundle full of critters takes no items, and its fullness bar counts critters. */
@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noItemsInMobBag(ItemStack bundle, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (MobBags.has(bundle)) cir.setReturnValue(false);
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void ryancraft$noItemsIntoMobBag(ItemStack bundle, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        if (MobBags.has(bundle)) cir.setReturnValue(false);
    }

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void ryancraft$bar(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (MobBags.has(stack)) cir.setReturnValue(true);
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void ryancraft$barWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        MobBag bag = MobBags.get(stack);
        if (bag != null && bag.count() > 0) cir.setReturnValue(Math.min(13, Math.round(13f * bag.count() / Math.max(1, bag.capacity()))));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void ryancraft$barColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (MobBags.has(stack)) cir.setReturnValue(0x8FCF5A);
    }
}

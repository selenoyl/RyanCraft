package com.ryancraft.mixin;

import com.ryancraft.trade.Currency;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Regional currency: after a villager gains new trades, desert and savanna villagers price them in rubies, snowy ones in sapphires. */
@Mixin(Villager.class)
public abstract class RegionalCurrencyMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void ryancraft$regionalCurrency(ServerLevel level, CallbackInfo ci) {
        Villager v = (Villager) (Object) this;
        Item gem = Currency.gemFor(v.getVillagerData());
        if (gem == null) return;
        MerchantOffers offers = v.getOffers();
        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer c = Currency.convert(offers.get(i), gem);
            if (c != null) offers.set(i, c);
        }
    }
}

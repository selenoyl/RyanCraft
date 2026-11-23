package com.ryancraft.trade;

import com.google.common.collect.ImmutableSet;
import com.ryancraft.RyanCraft;
import com.ryancraft.deco.Gems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Regional currency. Desert and savanna villagers trade in rubies, snowy villagers in sapphires, everyone else in
 * emeralds (see mixin/RegionalCurrencyMixin). The Money Changer, who works at a Coin Scale, swaps one gem for another
 * at a fee: five for four.
 */
public final class Currency {
    public static final Block COIN_SCALE;
    public static final ResourceKey<PoiType> COIN_SCALE_POI = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, RyanCraft.id("coin_scale"));
    public static final ResourceKey<VillagerProfession> MONEY_CHANGER = ResourceKey.create(Registries.VILLAGER_PROFESSION, RyanCraft.id("money_changer"));

    static {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id("coin_scale"));
        COIN_SCALE = Registry.register(BuiltInRegistries.BLOCK, bk, new CoinScaleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                .strength(1.5f).sound(SoundType.LANTERN).noOcclusion().setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id("coin_scale"));
        Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(COIN_SCALE, new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        PointOfInterestHelper.register(COIN_SCALE_POI.identifier(), 1, 1, COIN_SCALE);
        Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, MONEY_CHANGER, new VillagerProfession(Component.translatable("entity.ryancraft.villager.money_changer"),
                h -> h.is(COIN_SCALE_POI), h -> h.is(COIN_SCALE_POI), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER));
    }

    /** The gem a villager of this type deals in, or null for emeralds. */
    public static @Nullable Item gemFor(VillagerData d) {
        if (d.profession().is(MONEY_CHANGER)) return null;
        if (d.type().is(VillagerType.DESERT) || d.type().is(VillagerType.SAVANNA)) return Gems.RUBY;
        if (d.type().is(VillagerType.SNOW)) return Gems.SAPPHIRE;
        return null;
    }

    /** The offer with every emerald (and emerald block) swapped for the gem; null if it has none. */
    public static @Nullable MerchantOffer convert(MerchantOffer o, Item gem) {
        Item block = gem == Gems.RUBY ? Gems.RUBY_BLOCK.asItem() : Gems.SAPPHIRE_BLOCK.asItem();
        ItemCost a = swap(o.getItemCostA(), gem, block);
        Optional<ItemCost> b = o.getItemCostB().map(c -> swap(c, gem, block));
        ItemStack r = o.getResult();
        ItemStack r2 = r.is(Items.EMERALD) ? new ItemStack(gem, r.getCount()) : r.is(Items.EMERALD_BLOCK) ? new ItemStack(block, r.getCount()) : r;
        if (a == o.getItemCostA() && b.equals(o.getItemCostB()) && r2 == r) return null;
        return new MerchantOffer(a, b, r2, o.getUses(), o.getMaxUses(), o.getXp(), o.getPriceMultiplier(), o.getDemand());
    }

    private static ItemCost swap(ItemCost c, Item gem, Item block) {
        if (c.item().value() == Items.EMERALD) return new ItemCost(gem, c.count());
        if (c.item().value() == Items.EMERALD_BLOCK) return new ItemCost(block, c.count());
        return c;
    }

    private static void exchange(int level, Item from, Item to, int give, int get, int maxUses, int xp) {
        TradeOfferHelper.registerVillagerOffers(MONEY_CHANGER, level, list ->
                list.add((sl, e, r) -> new MerchantOffer(new ItemCost(from, give), new ItemStack(to, get), maxUses, xp, 0.0f)));
    }

    public static void init() {
        Item E = Items.EMERALD, R = Gems.RUBY, S = Gems.SAPPHIRE;
        Item EB = Items.EMERALD_BLOCK, RB = Gems.RUBY_BLOCK.asItem(), SB = Gems.SAPPHIRE_BLOCK.asItem();
        exchange(1, E, R, 5, 4, 32, 1); exchange(1, R, E, 5, 4, 32, 1);
        exchange(2, E, S, 5, 4, 32, 2); exchange(2, S, E, 5, 4, 32, 2);
        exchange(3, R, S, 5, 4, 32, 5); exchange(3, S, R, 5, 4, 32, 5);
        exchange(4, EB, RB, 5, 4, 12, 10); exchange(4, RB, EB, 5, 4, 12, 10); exchange(4, EB, SB, 5, 4, 12, 10); exchange(4, SB, EB, 5, 4, 12, 10);
        exchange(5, RB, SB, 5, 4, 12, 15); exchange(5, SB, RB, 5, 4, 12, 15);
        TradeOfferHelper.registerVillagerOffers(MONEY_CHANGER, 5, list ->                // a whole fortune, the honest way
                list.add((sl, e, r) -> new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 6), new ItemStack(E, 1), 16, 15, 0.05f)));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e -> e.accept(COIN_SCALE));
    }

    private Currency() {}
}

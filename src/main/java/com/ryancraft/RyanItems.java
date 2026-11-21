package com.ryancraft;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class RyanItems {
    public static final Item VENISON = food("venison", 3, 0.3f);
    public static final Item COOKED_VENISON = food("cooked_venison", 8, 0.8f);
    public static final Item RAW_DUCK = food("raw_duck", 2, 0.3f);
    public static final Item COOKED_DUCK = food("cooked_duck", 6, 0.6f);

    private static Item food(String name, int nutrition, float saturation) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        FoodProperties food = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build();
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).food(food)));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.accept(VENISON);
            entries.accept(COOKED_VENISON);
            entries.accept(RAW_DUCK);
            entries.accept(COOKED_DUCK);
        });
    }

    private RyanItems() {}
}

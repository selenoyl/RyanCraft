package com.ryancraft.food;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Dishes made from the garden crops: roasted corn and baked squash (cooked like any food), and crafted stuffed
 * peppers, cabbage rolls, sandwiches, garden salad and cornbread. Assets and recipes: tools/dishes.py.
 */
public final class Dishes {
    private static final List<Item> ITEMS = new ArrayList<>();
    public static final Item ROASTED_CORN = dish("roasted_corn", 6, 0.6f, false);
    public static final Item BAKED_SQUASH = dish("baked_squash", 6, 0.6f, false);
    public static final Item STUFFED_PEPPER = dish("stuffed_pepper", 9, 0.8f, false);
    public static final Item CABBAGE_ROLL = dish("cabbage_roll", 7, 0.8f, false);
    public static final Item SANDWICH = dish("sandwich", 10, 0.85f, false);
    public static final Item GARDEN_SALAD = dish("garden_salad", 7, 0.6f, true);
    public static final Item CORNBREAD = dish("cornbread", 5, 0.6f, false);

    private static Item dish(String name, int nutrition, float sat, boolean bowl) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item.Properties p = new Item.Properties().food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat).build());
        if (bowl) p.stacksTo(1).usingConvertsTo(Items.BOWL);
        Item i = Registry.register(BuiltInRegistries.ITEM, key, new Item(p.setId(key)));
        ITEMS.add(i);
        return i;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(e -> ITEMS.forEach(e::accept));
    }

    private Dishes() {}
}

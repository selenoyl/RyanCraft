package com.ryancraft.trees;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;

/**
 * A boat and a chest boat for every one of our woods, built on vanilla's own boats (so they row, carry two and hold a
 * chest exactly like oak ones). Textures are vanilla's oak boat recoloured to each wood's planks: tools/boats.py.
 */
public final class RyanBoats {
    public record BoatSet(EntityType<Boat> boat, EntityType<ChestBoat> chestBoat, Item boatItem, Item chestBoatItem) {}
    public static final Map<TreeSpecies, BoatSet> BOATS = new EnumMap<>(TreeSpecies.class);

    static {
        for (TreeSpecies s : TreeSpecies.values()) {
            Item[] item = new Item[2];
            EntityType<Boat> boat = entity(s.id + "_boat", EntityType.Builder.<Boat>of((t, l) -> new Boat(t, l, () -> item[0]), MobCategory.MISC));
            EntityType<ChestBoat> chest = entity(s.id + "_chest_boat", EntityType.Builder.<ChestBoat>of((t, l) -> new ChestBoat(t, l, () -> item[1]), MobCategory.MISC));
            item[0] = item(s.id + "_boat", boat);
            item[1] = item(s.id + "_chest_boat", chest);
            BOATS.put(s, new BoatSet(boat, chest, item[0], item[1]));
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> BOATS.values().forEach(b -> {
            e.accept(b.boatItem()); e.accept(b.chestBoatItem());
        }));
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String name, EntityType.Builder<T> b) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                b.noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10).build(key));
    }

    private static Item item(String name, EntityType<? extends net.minecraft.world.entity.vehicle.boat.AbstractBoat> type) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, new BoatItem(type, new Item.Properties().stacksTo(1).setId(key)));
    }

    public static void init() {}

    private RyanBoats() {}
}

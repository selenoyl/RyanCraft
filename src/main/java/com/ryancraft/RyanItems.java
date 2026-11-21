package com.ryancraft;

import com.ryancraft.entity.RyanEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RyanItems {
    public static final TagKey<Item> FISH_FOOD = TagKey.create(Registries.ITEM, RyanCraft.id("kentucky_fish"));
    public static final TagKey<Item> REPAIRS_COONSKIN = TagKey.create(Registries.ITEM, RyanCraft.id("repairs_coonskin"));

    public static final Item VENISON = food("venison", 3, 0.3f);
    public static final Item COOKED_VENISON = food("cooked_venison", 8, 0.8f);
    public static final Item RAW_DUCK = food("raw_duck", 2, 0.3f);
    public static final Item COOKED_DUCK = food("cooked_duck", 6, 0.6f);
    public static final Item RAW_TURKEY = food("raw_turkey", 3, 0.3f);
    public static final Item COOKED_TURKEY = food("cooked_turkey", 8, 0.7f);

    public static final Item BLUEGILL = food("bluegill", 2, 0.1f);
    public static final Item COOKED_BLUEGILL = food("cooked_bluegill", 5, 0.6f);
    public static final Item CRAPPIE = food("crappie", 2, 0.1f);
    public static final Item COOKED_CRAPPIE = food("cooked_crappie", 5, 0.6f);
    public static final Item LARGEMOUTH_BASS = food("largemouth_bass", 2, 0.1f);
    public static final Item COOKED_LARGEMOUTH_BASS = food("cooked_largemouth_bass", 6, 0.8f);
    public static final Item CHANNEL_CATFISH = food("channel_catfish", 2, 0.1f);
    public static final Item COOKED_CHANNEL_CATFISH = food("cooked_channel_catfish", 6, 0.8f);

    public static final Item BLUEGILL_BUCKET = bucket("bluegill_bucket", RyanEntities.BLUEGILL);
    public static final Item CRAPPIE_BUCKET = bucket("crappie_bucket", RyanEntities.CRAPPIE);
    public static final Item LARGEMOUTH_BASS_BUCKET = bucket("largemouth_bass_bucket", RyanEntities.LARGEMOUTH_BASS);
    public static final Item CHANNEL_CATFISH_BUCKET = bucket("channel_catfish_bucket", RyanEntities.CHANNEL_CATFISH);

    public static final Item RACCOON_TAIL = register("raccoon_tail", Item::new, new Item.Properties());

    public static final ArmorMaterial COONSKIN = new ArmorMaterial(8,
            Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 0, ArmorType.LEGGINGS, 0, ArmorType.BOOTS, 0, ArmorType.BODY, 0),
            15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, REPAIRS_COONSKIN,
            ResourceKey.create(EquipmentAssets.ROOT_ID, RyanCraft.id("coonskin")));
    /** Daniel Boone would approve. */
    public static final Item COONSKIN_CAP = register("coonskin_cap", Item::new, new Item.Properties().humanoidArmor(COONSKIN, ArmorType.HELMET));

    private static final Map<EntityType<?>, Item> BUCKETS = new HashMap<>();
    static {
        BUCKETS.put(RyanEntities.BLUEGILL, BLUEGILL_BUCKET);
        BUCKETS.put(RyanEntities.CRAPPIE, CRAPPIE_BUCKET);
        BUCKETS.put(RyanEntities.LARGEMOUTH_BASS, LARGEMOUTH_BASS_BUCKET);
        BUCKETS.put(RyanEntities.CHANNEL_CATFISH, CHANNEL_CATFISH_BUCKET);
    }

    public static Item bucketFor(EntityType<?> type) {
        return BUCKETS.getOrDefault(type, Items.WATER_BUCKET);
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
    }

    private static Item food(String name, int nutrition, float saturation) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
        return register(name, Item::new, new Item.Properties().food(food));
    }

    private static Item bucket(String name, EntityType<? extends net.minecraft.world.entity.Mob> type) {
        return register(name, p -> new MobBucketItem(type, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, p),
                new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(e -> {
            for (Item i : new Item[]{VENISON, COOKED_VENISON, RAW_DUCK, COOKED_DUCK, RAW_TURKEY, COOKED_TURKEY,
                    BLUEGILL, COOKED_BLUEGILL, CRAPPIE, COOKED_CRAPPIE, LARGEMOUTH_BASS, COOKED_LARGEMOUTH_BASS,
                    CHANNEL_CATFISH, COOKED_CHANNEL_CATFISH}) e.accept(i);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> {
            for (Item i : new Item[]{BLUEGILL_BUCKET, CRAPPIE_BUCKET, LARGEMOUTH_BASS_BUCKET, CHANNEL_CATFISH_BUCKET}) e.accept(i);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(e -> e.accept(COONSKIN_CAP));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> e.accept(RACCOON_TAIL));
    }

    private RyanItems() {}
}

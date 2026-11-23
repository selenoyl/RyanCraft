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

    public static final Item CHERRIES = food("cherries", 2, 0.3f);
    public static final Item CHERRY_PIE = food("cherry_pie", 8, 0.3f);
    public static final Item BLACK_WALNUT = register("black_walnut", Item::new, new Item.Properties());
    public static final Item SHELLED_WALNUT = food("shelled_walnut", 3, 0.6f);

    public static final Item BLUEGILL = food("bluegill", 2, 0.1f);
    public static final Item COOKED_BLUEGILL = food("cooked_bluegill", 5, 0.6f);
    public static final Item CRAPPIE = food("crappie", 2, 0.1f);
    public static final Item COOKED_CRAPPIE = food("cooked_crappie", 5, 0.6f);
    public static final Item LARGEMOUTH_BASS = food("largemouth_bass", 2, 0.1f);
    public static final Item COOKED_LARGEMOUTH_BASS = food("cooked_largemouth_bass", 6, 0.8f);
    public static final Item CHANNEL_CATFISH = food("channel_catfish", 2, 0.1f);
    public static final Item COOKED_CHANNEL_CATFISH = food("cooked_channel_catfish", 6, 0.8f);
    public static final Item RAINBOW_TROUT = food("rainbow_trout", 2, 0.1f);
    public static final Item COOKED_RAINBOW_TROUT = food("cooked_rainbow_trout", 5, 0.6f);
    public static final Item SMALLMOUTH_BASS = food("smallmouth_bass", 2, 0.1f);
    public static final Item COOKED_SMALLMOUTH_BASS = food("cooked_smallmouth_bass", 6, 0.8f);
    public static final Item YELLOW_PERCH = food("yellow_perch", 2, 0.1f);
    public static final Item COOKED_YELLOW_PERCH = food("cooked_yellow_perch", 5, 0.6f);
    public static final Item WALLEYE = food("walleye", 2, 0.1f);
    public static final Item COOKED_WALLEYE = food("cooked_walleye", 6, 0.8f);
    public static final Item NORTHERN_PIKE = food("northern_pike", 2, 0.1f);
    public static final Item COOKED_NORTHERN_PIKE = food("cooked_northern_pike", 6, 0.8f);
    public static final Item MUSKELLUNGE = food("muskellunge", 2, 0.1f);
    public static final Item COOKED_MUSKELLUNGE = food("cooked_muskellunge", 7, 0.9f);

    public static final Item BLUEGILL_BUCKET = bucket("bluegill_bucket", RyanEntities.BLUEGILL);
    public static final Item CRAPPIE_BUCKET = bucket("crappie_bucket", RyanEntities.CRAPPIE);
    public static final Item LARGEMOUTH_BASS_BUCKET = bucket("largemouth_bass_bucket", RyanEntities.LARGEMOUTH_BASS);
    public static final Item CHANNEL_CATFISH_BUCKET = bucket("channel_catfish_bucket", RyanEntities.CHANNEL_CATFISH);
    public static final Item RAINBOW_TROUT_BUCKET = bucket("rainbow_trout_bucket", RyanEntities.RAINBOW_TROUT);
    public static final Item SMALLMOUTH_BASS_BUCKET = bucket("smallmouth_bass_bucket", RyanEntities.SMALLMOUTH_BASS);
    public static final Item YELLOW_PERCH_BUCKET = bucket("yellow_perch_bucket", RyanEntities.YELLOW_PERCH);
    public static final Item WALLEYE_BUCKET = bucket("walleye_bucket", RyanEntities.WALLEYE);
    public static final Item NORTHERN_PIKE_BUCKET = bucket("northern_pike_bucket", RyanEntities.NORTHERN_PIKE);
    public static final Item MUSKELLUNGE_BUCKET = bucket("muskellunge_bucket", RyanEntities.MUSKELLUNGE);

    public static final Item RACCOON_TAIL = register("raccoon_tail", Item::new, new Item.Properties());

    public static final ArmorMaterial COONSKIN = new ArmorMaterial(8,
            Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 0, ArmorType.LEGGINGS, 0, ArmorType.BOOTS, 0, ArmorType.BODY, 0),
            15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, REPAIRS_COONSKIN,
            ResourceKey.create(EquipmentAssets.ROOT_ID, RyanCraft.id("coonskin")));
    /** Daniel Boone would approve. */
    public static final Item COONSKIN_CAP = register("coonskin_cap", Item::new, new Item.Properties().humanoidArmor(COONSKIN, ArmorType.HELMET));

    /** Primitive skull mount; its rack comes from the buck it was taken from. */
    public static final Item DEER_SKULL_MOUNT = register("deer_skull_mount", com.ryancraft.item.DeerSkullMountItem::new,
            new Item.Properties().stacksTo(16));

    /** Spawn eggs for every RyanCraft mob (ids: <mob>_spawn_egg). */
    public static final java.util.List<Item> SPAWN_EGGS = new java.util.ArrayList<>();
    static {
        egg("boar", RyanEntities.BOAR); egg("deer", RyanEntities.DEER); egg("duck", RyanEntities.DUCK); egg("turkey", RyanEntities.TURKEY);
        egg("black_bear", RyanEntities.BLACK_BEAR); egg("raccoon", RyanEntities.RACCOON); egg("skunk", RyanEntities.SKUNK);
        egg("mink", RyanEntities.MINK); egg("opossum", RyanEntities.OPOSSUM); egg("beaver", RyanEntities.BEAVER);
        egg("squirrel", RyanEntities.SQUIRREL); egg("songbird", RyanEntities.SONGBIRD); egg("chipmunk", RyanEntities.CHIPMUNK);
        egg("coyote", RyanEntities.COYOTE); egg("bluegill", RyanEntities.BLUEGILL); egg("crappie", RyanEntities.CRAPPIE);
        egg("largemouth_bass", RyanEntities.LARGEMOUTH_BASS); egg("channel_catfish", RyanEntities.CHANNEL_CATFISH);
        egg("rainbow_trout", RyanEntities.RAINBOW_TROUT); egg("smallmouth_bass", RyanEntities.SMALLMOUTH_BASS); egg("yellow_perch", RyanEntities.YELLOW_PERCH); egg("walleye", RyanEntities.WALLEYE); egg("northern_pike", RyanEntities.NORTHERN_PIKE); egg("muskellunge", RyanEntities.MUSKELLUNGE);
        egg("elk", RyanEntities.ELK); egg("moose", RyanEntities.MOOSE); egg("pronghorn", RyanEntities.PRONGHORN);
        egg("grizzly_bear", RyanEntities.GRIZZLY_BEAR); egg("bobcat", RyanEntities.BOBCAT);
        egg("crow", RyanEntities.CROW); egg("zebra", RyanEntities.ZEBRA); egg("reindeer", RyanEntities.REINDEER); egg("moobloom", RyanEntities.MOOBLOOM); egg("swan", RyanEntities.SWAN); egg("mudling", RyanEntities.MUDLING); egg("sky_ray", RyanEntities.SKY_RAY); egg("pegasus", RyanEntities.PEGASUS); egg("cloud_sheep", RyanEntities.CLOUD_SHEEP); egg("sky_roc", RyanEntities.SKY_ROC); egg("moon_rabbit", RyanEntities.MOON_RABBIT); egg("lost_spaceman", RyanEntities.LOST_SPACEMAN); egg("great_moon_rabbit", RyanEntities.GREAT_MOON_RABBIT); egg("dust_drifter", RyanEntities.DUST_DRIFTER);
        egg("black_tailed_deer", RyanEntities.BLACK_TAILED_DEER); egg("bison", RyanEntities.BISON); egg("cougar", RyanEntities.COUGAR); egg("river_otter", RyanEntities.RIVER_OTTER); egg("groundhog", RyanEntities.GROUNDHOG); egg("porcupine", RyanEntities.PORCUPINE);
        egg("bald_eagle", RyanEntities.BALD_EAGLE); egg("golden_eagle", RyanEntities.GOLDEN_EAGLE); egg("red_tailed_hawk", RyanEntities.RED_TAILED_HAWK); egg("turkey_vulture", RyanEntities.TURKEY_VULTURE); egg("great_horned_owl", RyanEntities.GREAT_HORNED_OWL); egg("great_blue_heron", RyanEntities.GREAT_BLUE_HERON); egg("canada_goose", RyanEntities.CANADA_GOOSE);
        egg("orca", RyanEntities.ORCA); egg("blue_whale", RyanEntities.BLUE_WHALE); egg("shark", RyanEntities.SHARK); egg("manta_ray", RyanEntities.MANTA_RAY); egg("jellyfish", RyanEntities.JELLYFISH);
        egg("butterfly", RyanEntities.BUTTERFLY); egg("hummingbird", RyanEntities.HUMMINGBIRD); egg("pileated_woodpecker", RyanEntities.PILEATED_WOODPECKER); egg("bobwhite", RyanEntities.BOBWHITE); egg("box_turtle", RyanEntities.BOX_TURTLE); egg("rattlesnake", RyanEntities.RATTLESNAKE); egg("bullfrog", RyanEntities.BULLFROG); egg("alligator", RyanEntities.ALLIGATOR);
    }

    private static void egg(String mob, EntityType<?> type) {
        SPAWN_EGGS.add(register(mob + "_spawn_egg", net.minecraft.world.item.SpawnEggItem::new, new Item.Properties().spawnEgg(type)));
    }

    private static final Map<EntityType<?>, Item> BUCKETS = new HashMap<>();
    static {
        BUCKETS.put(RyanEntities.BLUEGILL, BLUEGILL_BUCKET);
        BUCKETS.put(RyanEntities.CRAPPIE, CRAPPIE_BUCKET);
        BUCKETS.put(RyanEntities.LARGEMOUTH_BASS, LARGEMOUTH_BASS_BUCKET);
        BUCKETS.put(RyanEntities.CHANNEL_CATFISH, CHANNEL_CATFISH_BUCKET);
        BUCKETS.put(RyanEntities.RAINBOW_TROUT, RAINBOW_TROUT_BUCKET);
        BUCKETS.put(RyanEntities.SMALLMOUTH_BASS, SMALLMOUTH_BASS_BUCKET);
        BUCKETS.put(RyanEntities.YELLOW_PERCH, YELLOW_PERCH_BUCKET);
        BUCKETS.put(RyanEntities.WALLEYE, WALLEYE_BUCKET);
        BUCKETS.put(RyanEntities.NORTHERN_PIKE, NORTHERN_PIKE_BUCKET);
        BUCKETS.put(RyanEntities.MUSKELLUNGE, MUSKELLUNGE_BUCKET);
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
                    CHANNEL_CATFISH, COOKED_CHANNEL_CATFISH, RAINBOW_TROUT, COOKED_RAINBOW_TROUT, SMALLMOUTH_BASS, COOKED_SMALLMOUTH_BASS, YELLOW_PERCH, COOKED_YELLOW_PERCH, WALLEYE, COOKED_WALLEYE, NORTHERN_PIKE, COOKED_NORTHERN_PIKE, MUSKELLUNGE, COOKED_MUSKELLUNGE, CHERRIES, CHERRY_PIE, SHELLED_WALNUT}) e.accept(i);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> {
            for (Item i : new Item[]{BLUEGILL_BUCKET, CRAPPIE_BUCKET, LARGEMOUTH_BASS_BUCKET, CHANNEL_CATFISH_BUCKET, RAINBOW_TROUT_BUCKET, SMALLMOUTH_BASS_BUCKET, YELLOW_PERCH_BUCKET, WALLEYE_BUCKET, NORTHERN_PIKE_BUCKET, MUSKELLUNGE_BUCKET}) e.accept(i);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(e -> e.accept(COONSKIN_CAP));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e -> {
            for (int[] v : new int[][]{{1, 0, 0, 0}, {2, 0, 0, 0}, {3, 0, 0, 0}, {4, 0, 0, 0}, {4, 1, 0, 1}, {4, 0, 1, 2}, {4, 0, 2, 0}, {4, 0, 3, 1}}) {
                e.accept(com.ryancraft.item.DeerSkullMountItem.create(v[0], v[1] == 1, v[2], v[3]));
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> { e.accept(RACCOON_TAIL); e.accept(BLACK_WALNUT); });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(e -> SPAWN_EGGS.forEach(e::accept));
    }

    private RyanItems() {}
}

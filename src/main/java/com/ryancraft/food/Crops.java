package com.ryancraft.food;

import com.ryancraft.RyanCraft;
import com.ryancraft.season.Seasons.Season;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Garden crops, each with its growing seasons. Onions, beans and rice are planted as they are; the rest from seeds,
 * which come from breaking grass to begin with and from the produce after that. Assets: tools/crops.py.
 */
public final class Crops {
    public record Crop(String name, Block block, Item produce, Item seed) {}
    public static final Map<String, Crop> ALL = new LinkedHashMap<>();
    private static final List<Item> ITEMS = new ArrayList<>();

    static {
        //   name       food (nutrition, saturation modifier; 0 = not eaten raw)  seed item ("" = plant the produce)  seasons
        crop("tomato",  3, 0.3f, "tomato_seeds",  EnumSet.of(Season.SUMMER));
        crop("pepper",  2, 0.3f, "pepper_seeds",  EnumSet.of(Season.SUMMER));
        crop("corn",    3, 0.4f, "corn_seeds",    EnumSet.of(Season.SUMMER));
        crop("squash",  0, 0,    "squash_seeds",  EnumSet.of(Season.SUMMER, Season.AUTUMN));
        crop("cabbage", 2, 0.4f, "cabbage_seeds", EnumSet.of(Season.SPRING, Season.AUTUMN));
        crop("onion",   2, 0.3f, "",              EnumSet.of(Season.SPRING));
        crop("beans",   0, 0,    "",              EnumSet.of(Season.SPRING, Season.SUMMER));
        crop("rice",    0, 0,    "",              EnumSet.of(Season.SUMMER));
    }

    private static void crop(String name, int food, float sat, String seedName, EnumSet<Season> seasons) {
        Item[] seed = new Item[1];
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name + "_crop"));
        Block block = Registry.register(BuiltInRegistries.BLOCK, bk, new SeasonalCropBlock(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollision()
                .randomTicks().instabreak().sound(net.minecraft.world.level.block.SoundType.CROP).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).setId(bk),
                () -> seed[0], seasons));
        Item.Properties pp = new Item.Properties();
        if (food > 0) pp.food(new FoodProperties.Builder().nutrition(food).saturationModifier(sat).build());
        Item produce;
        if (seedName.isEmpty()) {                                              // planted as it is
            produce = item(name, p -> new BlockItem(block, p), pp.useItemDescriptionPrefix());
            seed[0] = produce;
        } else {
            produce = item(name, Item::new, pp);
            seed[0] = item(seedName, p -> new BlockItem(block, p), new Item.Properties().useItemDescriptionPrefix());
        }
        ALL.put(name, new Crop(name, block, produce, seed[0]));
        CompostingChanceRegistry.INSTANCE.add(produce, 0.65f);
        if (seed[0] != produce) CompostingChanceRegistry.INSTANCE.add(seed[0], 0.3f);
    }

    private static Item item(String name, java.util.function.Function<Item.Properties, Item> f, Item.Properties p) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, key, f.apply(p.setId(key)));
        ITEMS.add(i);
        return i;
    }

    public static Item get(String name) { return ALL.get(name).produce(); }

    public static void init() {
        // breaking grass now and then turns up a seed (or an onion, a bean, a few grains of rice)
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (!source.isBuiltin() || !(Blocks.SHORT_GRASS.getLootTable().filter(key::equals).isPresent()
                    || Blocks.TALL_GRASS.getLootTable().filter(key::equals).isPresent())) return;
            LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.06f));
            for (Crop c : ALL.values()) pool.add(LootItem.lootTableItem(c.seed()));
            builder.withPool(pool);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> ALL.values().forEach(c -> e.accept(c.seed())));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(e -> ALL.values().forEach(c -> e.accept(c.produce())));
    }

    private Crops() {}
}

package com.ryancraft.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A free-form meal from the cooking pot (Vintage Story style): its kind (stew, soup, chowder, porridge) and what went
 * into it. The name comes from the main ingredient ("Venison Stew"); how filling a portion is comes from everything in
 * it, with a bonus for variety.
 */
public record Meal(String kind, List<Item> ingredients) {
    public static final Codec<Meal> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("kind").forGetter(Meal::kind),
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("ingredients").forGetter(Meal::ingredients)).apply(i, Meal::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Meal> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final int MAX_INGREDIENTS = 6;

    /** Non-food things that still go in a pot, with what they add (nutrition, saturation). */
    private static final Map<Item, float[]> EXTRAS = new HashMap<>();
    static {
        EXTRAS.put(Items.BROWN_MUSHROOM, new float[]{2, 1.2f}); EXTRAS.put(Items.RED_MUSHROOM, new float[]{2, 1.2f});
        EXTRAS.put(Items.WHEAT, new float[]{2, 1.0f}); EXTRAS.put(Items.EGG, new float[]{3, 2.0f}); EXTRAS.put(Items.SUGAR, new float[]{1, 0.2f});
        EXTRAS.put(Items.PUMPKIN, new float[]{3, 1.5f}); EXTRAS.put(Items.KELP, new float[]{1, 0.4f});
    }

    public static boolean isIngredient(ItemStack s) {
        if (s.isEmpty() || s.is(Cooking.MEAL) || s.has(DataComponents.USE_REMAINDER)) return false;   // no bowls of stew in the stew
        return s.has(DataComponents.FOOD) || EXTRAS.containsKey(s.getItem());
    }

    static boolean meaty(Item i) { ItemStack s = new ItemStack(i); return s.is(ItemTags.MEAT) || s.is(ItemTags.FISHES); }
    static boolean grain(Item i) { return i == Items.WHEAT || i == Items.BREAD; }

    /** The kind of meal these make in this base (true for milk). */
    public static String kindFor(boolean milk, List<Item> ings) {
        if (milk) return ings.stream().anyMatch(Meal::grain) ? "porridge" : "chowder";
        return ings.stream().anyMatch(Meal::meaty) ? "stew" : "soup";
    }

    /** The ingredient the meal is named for: the most used, meat or fish first on a tie. */
    public Item main() {
        Map<Item, Integer> n = new LinkedHashMap<>();
        for (Item i : ingredients) n.merge(i, 1, Integer::sum);
        Item best = ingredients.get(0); int bn = -1; boolean bm = false;
        for (var e : n.entrySet()) {
            boolean m = meaty(e.getKey());
            if (e.getValue() > bn || (e.getValue() == bn && m && !bm)) { best = e.getKey(); bn = e.getValue(); bm = m; }
        }
        return best;
    }

    public Component name() {
        Item m = main(); var id = BuiltInRegistries.ITEM.getKey(m);
        Component ing = Component.translatableWithFallback("meal.ryancraft.ingredient." + id.getNamespace() + "." + id.getPath(), "%s", m.getName());
        return Component.translatable("item.ryancraft.meal." + kind, ing);
    }

    /** One portion: what every ingredient brings (meat and fish counting as cooked), shared out, plus a variety bonus. */
    public FoodProperties portion(int portions) {
        float n = 0, s = 0;
        for (Item i : ingredients) {
            FoodProperties f = new ItemStack(i).get(DataComponents.FOOD);
            float[] x = EXTRAS.get(i);
            float in = f != null ? f.nutrition() : x != null ? x[0] : 1, is = f != null ? f.saturation() : x != null ? x[1] : 0.5f;
            if (meaty(i) && in < 5) { in += 3; is += 4; }                                        // raw meat cooks in the pot
            n += in; s += is;
        }
        long distinct = ingredients.stream().distinct().count();
        float mult = 1.1f + 0.1f * distinct;
        int p = Math.max(1, portions);
        return new FoodProperties(Math.max(2, Math.round(n * mult / p)), s * mult / p, false);
    }

    /** "with carrot, potato and onion"-style lines for the tooltip. */
    public List<Component> lines() {
        List<Component> out = new ArrayList<>();
        Map<Item, Integer> n = new LinkedHashMap<>();
        for (Item i : ingredients) n.merge(i, 1, Integer::sum);
        for (var e : n.entrySet())
            out.add(Component.literal(" ").append(e.getValue() > 1 ? Component.literal(e.getValue() + "x ") : Component.empty()).append(e.getKey().getName())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        return out;
    }
}

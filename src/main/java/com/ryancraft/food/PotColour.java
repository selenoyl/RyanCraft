package com.ryancraft.food;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The colour of what's in a cooking pot: water or milk, then whatever its main ingredient turns it (tomato red, carrot orange...). */
public final class PotColour {
    public static final int WATER = 0, MILK = 1, BROWN = 2, RED = 3, ORANGE = 4, PURPLE = 5, GREEN = 6, TAN = 7, YELLOW = 8,
            MUSHROOM = 9, BERRY = 10, CREAM = 11, PINK = 12, HONEY = 13, PALE = 14, PUMPKIN = 15;
    public static final int[] RGB = {0x3E6CD4, 0xF0EEE6, 0x7A4824, 0xC4321E, 0xE07A22, 0x7C2C5C, 0x6E9C38, 0xC6A66E, 0xE6C63C,
            0x8C705C, 0x9C2242, 0xE8DCB6, 0xE6B2A8, 0xD6A032, 0xD4CCB4, 0xD67C2C};

    /** The colour an ingredient gives a pot of water. */
    static int of(Item i) {
        String p = BuiltInRegistries.ITEM.getKey(i).getPath();
        if (p.contains("tomato") || p.contains("pepper")) return RED;
        if (p.contains("carrot")) return ORANGE;
        if (p.contains("beetroot")) return PURPLE;
        if (p.contains("cabbage") || p.contains("kelp")) return GREEN;
        if (p.contains("corn") || p.equals("egg")) return YELLOW;
        if (p.contains("mushroom")) return MUSHROOM;
        if (p.contains("berr") || p.contains("cherr") || p.contains("chorus")) return BERRY;
        if (p.contains("honey") || p.contains("sugar")) return HONEY;
        if (p.contains("pumpkin") || p.contains("squash")) return PUMPKIN;
        ItemStack s = new ItemStack(i);
        if (s.is(ItemTags.FISHES)) return PALE;
        if (s.is(ItemTags.MEAT)) return BROWN;
        return TAN;                                                      // potatoes, grain, beans, apples, onions and the rest
    }

    /** The pot's colour for this base and these ingredients. */
    public static int pot(boolean milk, List<Item> ingredients) {
        if (ingredients.isEmpty()) return milk ? MILK : WATER;
        int c = of(new Meal("", ingredients).main());
        if (!milk) return c;
        return c == RED || c == BERRY || c == PURPLE ? PINK : c == YELLOW || c == HONEY ? HONEY : CREAM;   // milk softens it
    }

    private PotColour() {}
}

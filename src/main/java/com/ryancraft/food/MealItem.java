package com.ryancraft.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** A bowl of a cooked meal; its name, look and nourishment come from its {@link Meal}. */
public class MealItem extends Item {
    public MealItem(Properties p) { super(p); }

    @Override
    public Component getName(ItemStack stack) {
        Meal m = stack.get(Cooking.MEAL_DATA);
        return m == null || m.ingredients().isEmpty() ? super.getName(stack) : m.name();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        Meal m = stack.get(Cooking.MEAL_DATA);
        if (m == null) return;
        out.accept(Component.translatable("item.ryancraft.meal.made_with").withStyle(net.minecraft.ChatFormatting.GRAY));
        m.lines().forEach(out);
    }
}

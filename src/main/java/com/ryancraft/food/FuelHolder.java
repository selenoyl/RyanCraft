package com.ryancraft.food;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Something that burns fuel like a campfire: a one-slot fuel stack and the burn time left on the current piece. */
public interface FuelHolder {
    NonNullList<ItemStack> ryancraft$fuel();
    int ryancraft$burn();
    void ryancraft$setBurn(int burn);
    int ryancraft$total();
    void ryancraft$setTotal(int total);
    void ryancraft$changed();

    /** Copies the fire over (campfire to pot-in-the-fire and back). */
    default void ryancraft$copyFireFrom(FuelHolder o) {
        ryancraft$fuel().set(0, o.ryancraft$fuel().get(0).copy());
        ryancraft$setBurn(o.ryancraft$burn()); ryancraft$setTotal(o.ryancraft$total()); ryancraft$changed();
    }
}

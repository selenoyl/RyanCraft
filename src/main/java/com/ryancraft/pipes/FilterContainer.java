package com.ryancraft.pipes;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The diamond pipe's 6 x 9 filter grid. */
public class FilterContainer implements Container {
    private final PipeBlockEntity pipe;
    public FilterContainer(PipeBlockEntity pipe) { this.pipe = pipe; }
    @Override public int getContainerSize() { return 54; }
    @Override public boolean isEmpty() { return pipe.filters.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int i) { return pipe.filters.get(i); }
    @Override public ItemStack removeItem(int i, int n) { ItemStack s = pipe.filters.get(i); pipe.filters.set(i, ItemStack.EMPTY); pipe.filtersChanged(); return s; }
    @Override public ItemStack removeItemNoUpdate(int i) { return removeItem(i, 1); }
    @Override public void setItem(int i, ItemStack s) { pipe.filters.set(i, s); pipe.filtersChanged(); }
    @Override public int getMaxStackSize() { return 1; }
    @Override public void setChanged() { pipe.filtersChanged(); }
    @Override public boolean stillValid(Player p) { return !pipe.isRemoved() && p.distanceToSqr(pipe.getBlockPos().getCenter()) < 64; }
    @Override public void clearContent() { for (int i = 0; i < 54; i++) pipe.filters.set(i, ItemStack.EMPTY); pipe.filtersChanged(); }
}

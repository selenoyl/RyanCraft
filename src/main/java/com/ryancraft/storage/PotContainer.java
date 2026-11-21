package com.ryancraft.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** One pot's store, written straight back to the block entity. Food (and seeds, grain, sugar, eggs and the like) only. */
public class PotContainer implements Container {
    private final ClayPotBlockEntity be;
    private final NonNullList<ItemStack> items;

    PotContainer(ClayPotBlockEntity be, NonNullList<ItemStack> items) { this.be = be; this.items = items; }

    /** Client-side placeholder for the menu. */
    PotContainer() { this.be = null; this.items = NonNullList.withSize(ClayPotBlockEntity.SLOTS, ItemStack.EMPTY); }

    public static boolean storable(ItemStack s) {
        return s.has(DataComponents.FOOD) || s.is(Items.WHEAT) || s.is(Items.WHEAT_SEEDS) || s.is(Items.BEETROOT_SEEDS)
                || s.is(Items.MELON_SEEDS) || s.is(Items.PUMPKIN_SEEDS) || s.is(Items.SUGAR) || s.is(Items.COCOA_BEANS)
                || s.is(Items.EGG) || s.is(Items.BROWN_EGG) || s.is(Items.BLUE_EGG) || s.is(Items.HONEYCOMB) || s.is(Items.SUGAR_CANE)
                || s.is(Items.BROWN_MUSHROOM) || s.is(Items.RED_MUSHROOM) || s.is(com.ryancraft.RyanItems.BLACK_WALNUT);
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int i) { return items.get(i); }
    @Override public ItemStack removeItem(int i, int n) { ItemStack r = ContainerHelper.removeItem(items, i, n); setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int i) { return ContainerHelper.takeItem(items, i); }
    @Override public void setItem(int i, ItemStack s) { items.set(i, s); setChanged(); }
    @Override public void setChanged() { if (be != null) be.setChanged(); }
    @Override public boolean stillValid(Player p) {
        return be == null || (!be.isRemoved() && p.distanceToSqr(be.getBlockPos().getCenter()) < 64);
    }
    @Override public boolean canPlaceItem(int i, ItemStack s) { return storable(s); }
    @Override public void clearContent() { items.clear(); setChanged(); }
}

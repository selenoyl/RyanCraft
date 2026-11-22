package com.ryancraft.pipes;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Six coloured filter rows, one per side (down, up, north, south, west, east). Clicking a filter slot copies the item
 * you hold (it isn't used up); clicking with an empty hand clears it.
 */
public class DiamondPipeMenu extends AbstractContainerMenu {
    private final Container filters;

    public DiamondPipeMenu(int id, Inventory inv) { this(id, inv, new SimpleContainer(54)); }

    public DiamondPipeMenu(int id, Inventory inv, Container filters) {
        super(RyanPipes.DIAMOND_MENU, id);
        this.filters = filters;
        for (int row = 0; row < 6; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(filters, row * 9 + col, 8 + col * 18, 18 + row * 18) {
                    @Override public boolean mayPickup(Player p) { return false; }
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                });
        this.addStandardInventorySlots(inv, 8, 140);
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId >= 0 && slotId < 54) {
            ItemStack carried = getCarried();
            filters.setItem(slotId, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            return;
        }
        super.clicked(slotId, button, type, player);
    }

    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return filters.stillValid(p); }
}

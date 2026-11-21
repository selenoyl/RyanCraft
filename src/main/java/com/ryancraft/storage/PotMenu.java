package com.ryancraft.storage;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A pot's five slots over the player inventory (laid out like a hopper). */
public class PotMenu extends AbstractContainerMenu {
    private final Container pot;

    public PotMenu(int id, Inventory inv) { this(id, inv, new PotContainer()); }

    public PotMenu(int id, Inventory inv, Container pot) {
        super(RyanStorage.POT_MENU, id);
        this.pot = pot;
        pot.startOpen(inv.player);
        for (int i = 0; i < ClayPotBlockEntity.SLOTS; i++) {
            this.addSlot(new Slot(pot, i, 44 + i * 18, 20) {
                @Override public boolean mayPlace(ItemStack s) { return PotContainer.storable(s); }
            });
        }
        this.addStandardInventorySlots(inv, 8, 51);
    }

    @Override public boolean stillValid(Player p) { return pot.stillValid(p); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack s = slot.getItem(), copy = s.copy();
        int n = ClayPotBlockEntity.SLOTS;
        if (index < n) { if (!this.moveItemStackTo(s, n, this.slots.size(), true)) return ItemStack.EMPTY; }
        else if (!PotContainer.storable(s) || !this.moveItemStackTo(s, 0, n, false)) return ItemStack.EMPTY;
        if (s.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public void removed(Player p) { super.removed(p); pot.stopOpen(p); }
}

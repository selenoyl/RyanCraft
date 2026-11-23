package com.ryancraft.food;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A campfire's fuel slot (under its flame) over the player inventory, laid out like a furnace's. */
public class CampfireMenu extends AbstractContainerMenu {
    private final Container fuel;
    private final ContainerData data;

    public CampfireMenu(int id, Inventory inv) { this(id, inv, new SimpleContainer(1), new SimpleContainerData(2)); }

    public CampfireMenu(int id, Inventory inv, Container fuel, ContainerData data) {
        super(CampfireFuel.MENU, id);
        this.fuel = fuel; this.data = data;
        Player p = inv.player;
        this.addSlot(new Slot(fuel, 0, 80, 53) {
            @Override public boolean mayPlace(ItemStack s) { return p.level().fuelValues().isFuel(s); }
        });
        this.addStandardInventorySlots(inv, 8, 84);
        this.addDataSlots(data);
    }

    /** Seconds left on the burning piece, and how long it lasts in all. */
    public int burnSeconds() { return data.get(0); }
    public int totalSeconds() { return data.get(1); }

    @Override public boolean stillValid(Player p) { return fuel.stillValid(p); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack s = slot.getItem(), copy = s.copy();
        if (index == 0) { if (!this.moveItemStackTo(s, 1, this.slots.size(), true)) return ItemStack.EMPTY; }
        else if (!player.level().fuelValues().isFuel(s) || !this.moveItemStackTo(s, 0, 1, false)) return ItemStack.EMPTY;
        if (s.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}

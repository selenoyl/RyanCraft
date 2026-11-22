package com.ryancraft.pipes;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The terminal screen: a scrolling view of the whole network on top (drawn by the screen, not real slots), the two rows
 * where requested items arrive, then your inventory. Shift-click your own items to send them off into storage.
 */
public class EnderTerminalMenu extends AbstractContainerMenu {
    public static final int BUFFER_Y = 90;
    private final Container buffer;
    final @Nullable EnderTerminalBlockEntity terminal;
    private final Player player;
    private int refresh;
    /** Client copy of the network contents. */
    public List<ItemStack> shown = new ArrayList<>();
    public List<Long> counts = new ArrayList<>();

    public EnderTerminalMenu(int id, Inventory inv) { this(id, inv, new SimpleContainer(18), null); }

    public EnderTerminalMenu(int id, Inventory inv, Container buffer, @Nullable EnderTerminalBlockEntity terminal) {
        super(RyanPipes.TERMINAL_MENU, id);
        this.buffer = buffer; this.terminal = terminal; this.player = inv.player;
        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(buffer, row * 9 + col, 8 + col * 18, BUFFER_Y + row * 18) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                });
        this.addStandardInventorySlots(inv, 8, 140);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (terminal != null && player instanceof ServerPlayer sp && refresh-- <= 0) {
            refresh = 20;
            Map<ItemVariant, Long> all = terminal.contents(sp.level());
            List<ItemStack> items = new ArrayList<>(); List<Long> n = new ArrayList<>();
            all.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue(), a.getValue())).forEach(e -> { items.add(e.getKey().toStack(1)); n.add(e.getValue()); });
            ServerPlayNetworking.send(sp, new TerminalPayloads.Contents(items, n));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack s = slot.getItem();
        if (index < 18) {                                    // arrived items -> your inventory
            ItemStack copy = s.copy();
            if (!this.moveItemStackTo(s, 18, this.slots.size(), true)) return ItemStack.EMPTY;
            slot.setChanged();
            return copy;
        }
        if (terminal != null) {                              // your items -> off into storage through the pipes
            slot.set(terminal.deposit(p.level(), s.copy()));
            refresh = 0;
        }
        return ItemStack.EMPTY;
    }

    @Override public boolean stillValid(Player p) { return terminal == null || (!terminal.isRemoved() && p.distanceToSqr(terminal.getBlockPos().getCenter()) < 64); }
}

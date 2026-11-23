package com.ryancraft.food;

import com.ryancraft.RyanCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Campfires burn fuel (mixin/CampfireFuelMixin): put logs, planks, coal or anything a furnace burns in their fuel slot
 * (right-click the campfire). A campfire burns fuel three times slower than a furnace, and goes out when it runs dry.
 * A newly made campfire comes with two minutes of kindling. The pot set right in a fire burns the same way.
 */
public final class CampfireFuel {
    public static final int SLOWER = 3, KINDLING = 2400;
    public static final MenuType<CampfireMenu> MENU = Registry.register(BuiltInRegistries.MENU, RyanCraft.id("campfire"),
            new MenuType<>(CampfireMenu::new, FeatureFlags.VANILLA_SET));

    /** One tick of burning; false once the fire has nothing left. */
    public static boolean burn(ServerLevel level, FuelHolder h) {
        if (h.ryancraft$burn() < 0) { h.ryancraft$setBurn(KINDLING); h.ryancraft$setTotal(KINDLING); }   // fresh kindling
        if (h.ryancraft$burn() > 0) {
            h.ryancraft$setBurn(h.ryancraft$burn() - 1);
            if (h.ryancraft$burn() > 0) return true;
        }
        ItemStack f = h.ryancraft$fuel().get(0);
        int d = level.fuelValues().burnDuration(f);
        if (d <= 0) return false;
        ItemStack rem = f.getItem().getCraftingRemainder();
        f.shrink(1);
        if (f.isEmpty() && !rem.isEmpty()) h.ryancraft$fuel().set(0, rem.copy());
        h.ryancraft$setBurn(d * SLOWER); h.ryancraft$setTotal(d * SLOWER); h.ryancraft$changed();
        return true;
    }

    public static void goOut(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.6f, 1.2f);
    }

    /** Adds fuel from a hand stack (the pot in the fire takes it by right-click); true if any went in. */
    public static boolean addFuel(Level level, FuelHolder h, ItemStack hand) {
        if (!level.fuelValues().isFuel(hand)) return false;
        ItemStack slot = h.ryancraft$fuel().get(0);
        if (slot.isEmpty()) { h.ryancraft$fuel().set(0, hand.split(hand.getMaxStackSize())); h.ryancraft$changed(); return true; }
        if (!ItemStack.isSameItemSameComponents(slot, hand) || slot.getCount() >= slot.getMaxStackSize()) return false;
        int n = Math.min(hand.getCount(), slot.getMaxStackSize() - slot.getCount());
        slot.grow(n); hand.shrink(n); h.ryancraft$changed();
        return true;
    }

    /** "Burning: 1:30 left on this piece, 12 Oak Log to come" for a status line. */
    public static Component describe(FuelHolder h, boolean lit) {
        ItemStack f = h.ryancraft$fuel().get(0);
        int secs = Math.max(0, h.ryancraft$burn()) / 20;
        Component rest = f.isEmpty() ? Component.translatable("container.ryancraft.campfire.no_fuel") : Component.literal(f.getCount() + " ").append(f.getHoverName());
        return lit ? Component.translatable("container.ryancraft.campfire.burning", secs / 60 + ":" + String.format("%02d", secs % 60), rest)
                   : Component.translatable("container.ryancraft.campfire.out", rest);
    }

    public static void openMenu(Player player, FuelHolder h, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new CampfireMenu(id, inv, container(h, pos), data(h)),
                Component.translatable("container.ryancraft.campfire")));
    }

    /** Burn time is sent in seconds (menu data is only 16 bits). */
    static ContainerData data(FuelHolder h) {
        return new ContainerData() {
            @Override public int get(int i) { return Math.max(0, i == 0 ? h.ryancraft$burn() : h.ryancraft$total()) / 20; }
            @Override public void set(int i, int v) {}
            @Override public int getCount() { return 2; }
        };
    }

    static Container container(FuelHolder h, BlockPos pos) {
        return new Container() {
            @Override public int getContainerSize() { return 1; }
            @Override public boolean isEmpty() { return h.ryancraft$fuel().get(0).isEmpty(); }
            @Override public ItemStack getItem(int i) { return h.ryancraft$fuel().get(0); }
            @Override public ItemStack removeItem(int i, int n) { ItemStack s = ContainerHelper.removeItem(h.ryancraft$fuel(), 0, n); if (!s.isEmpty()) h.ryancraft$changed(); return s; }
            @Override public ItemStack removeItemNoUpdate(int i) { return ContainerHelper.takeItem(h.ryancraft$fuel(), 0); }
            @Override public void setItem(int i, ItemStack s) { h.ryancraft$fuel().set(0, s); h.ryancraft$changed(); }
            @Override public void setChanged() { h.ryancraft$changed(); }
            @Override public boolean stillValid(Player p) { return p.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64; }
            @Override public void clearContent() { h.ryancraft$fuel().set(0, ItemStack.EMPTY); }
        };
    }

    public static void init() {}
    private CampfireFuel() {}
}

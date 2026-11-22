package com.ryancraft.pipes;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds what is travelling through one pipe. The server decides where items go; the client runs the same motion so the
 * flow looks smooth, and is corrected whenever an item enters or leaves.
 */
public class PipeBlockEntity extends BlockEntity implements MenuProvider {
    public final List<TravelingItem> items = new ArrayList<>();
    public FluidVariant fluid = FluidVariant.blank();
    public long amount;
    private @Nullable Direction fluidFrom;
    public static final long FLUID_CAPACITY = FluidConstants.BUCKET / 2;
    /** Diamond pipes: one filter row of 9 per side, in Direction order (down, up, north, south, west, east). */
    public final NonNullList<ItemStack> filters = NonNullList.withSize(54, ItemStack.EMPTY);
    private boolean dirty;
    private long lastFluidSync;
    private int extractSide;

    public PipeBlockEntity(BlockPos pos, BlockState state) { super(RyanPipes.PIPE_ENTITY, pos, state); }

    public PipeType type() { return getBlockState().getBlock() instanceof PipeBlock p ? p.type : PipeType.COBBLESTONE; }
    private boolean connected(Direction d) { return getBlockState().getValue(PipeBlock.SIDES.get(d)); }

    // ------------------------------------------------------------------ ticking
    public static void serverTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity be) {
        if (be.type().fluid) be.tickFluid(level);
        else {
            be.tickItems(level, true);
            if (be.type() == PipeType.OBSIDIAN && level.getGameTime() % 5 == 0) be.suckUpItems(level);
        }
        if (be.dirty) { be.dirty = false; be.setChanged(); level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS); }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity be) { be.tickItems(level, false); }

    private void tickItems(Level level, boolean server) {
        Iterator<TravelingItem> it = items.iterator();
        List<TravelingItem> bounced = new ArrayList<>();
        while (it.hasNext()) {
            TravelingItem t = it.next();
            float before = t.progress;
            t.progress += t.speed;
            if (before < 0.5f && t.progress >= 0.5f) {
                if (server) { chooseExit(level, t); dirty = true; }
                else if (t.to == null) t.progress = 0.5f;            // wait for the server's decision
            }
            if (t.progress >= 1f) {
                if (!server) { t.progress = 1f; continue; }          // the server's update moves it on
                it.remove(); dirty = true;
                TravelingItem back = handOff(level, t);
                if (back != null) bounced.add(back);
            }
        }
        items.addAll(bounced);
    }

    // ------------------------------------------------------------------ routing
    private void chooseExit(Level level, TravelingItem t) {
        PipeType type = type();
        // speed: gold pipes push items along; elsewhere they slowly ease back to the pipe's own pace
        t.speed = type == PipeType.GOLD ? Math.min(0.16f, t.speed + 0.025f) : Math.max(type.baseSpeed, t.speed * 0.97f);
        if (t.routed && !t.route.isEmpty()) { t.to = t.route.remove(0); return; }
        List<Direction> options = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d == t.from || !connected(d)) continue;
            if (canAccept(level, d, t.stack, type == PipeType.WOODEN)) options.add(d);
        }
        if (type == PipeType.IRON) { Direction out = getBlockState().getValue(PipeBlock.OUTPUT); options.removeIf(d -> d != out); }
        if (type == PipeType.DIAMOND) {
            List<Direction> match = new ArrayList<>(), open = new ArrayList<>();
            for (Direction d : options) {
                boolean empty = true, hit = false;
                for (int i = 0; i < 9; i++) {
                    ItemStack f = filters.get(d.get3DDataValue() * 9 + i);
                    if (!f.isEmpty()) { empty = false; if (ItemStack.isSameItem(f, t.stack)) hit = true; }
                }
                if (hit) match.add(d); else if (empty) open.add(d);
            }
            options = match.isEmpty() ? open : match;
        }
        t.to = options.isEmpty() ? t.from : options.get(level.getRandom().nextInt(options.size()));
    }

    private boolean canAccept(Level level, Direction d, ItemStack stack, boolean noInventories) {
        BlockPos n = worldPosition.relative(d);
        BlockState ns = level.getBlockState(n);
        if (ns.getBlock() instanceof PipeBlock p) return !p.type.fluid;
        if (ns.getBlock() instanceof EnderTerminalBlock) return false;          // only routed items go to a terminal
        if (noInventories) return false;                                         // wooden pipes pull, never push
        Storage<ItemVariant> s = ItemStorage.SIDED.find(level, n, d.getOpposite());
        return s != null && StorageUtil.simulateInsert(s, ItemVariant.of(stack), stack.getCount(), null) > 0;
    }

    /** Pass an item on through its exit face. Returns an item to send back the way it came, if it couldn't go. */
    private @Nullable TravelingItem handOff(Level level, TravelingItem t) {
        Direction d = t.to == null ? t.from : t.to;
        BlockPos n = worldPosition.relative(d);
        BlockEntity nbe = level.getBlockEntity(n);
        if (nbe instanceof PipeBlockEntity next && !next.type().fluid) {
            next.receive(new TravelingItem(t.stack, 0f, t.speed, d.getOpposite(), null, t.route, t.routed));
            return null;
        }
        if (nbe instanceof EnderTerminalBlockEntity term) {
            ItemStack left = term.receive(t.stack);
            if (!left.isEmpty()) Containers.dropItemStack(level, n.getX() + 0.5, n.getY() + 1.1, n.getZ() + 0.5, left);
            return null;
        }
        Storage<ItemVariant> s = ItemStorage.SIDED.find(level, n, d.getOpposite());
        if (s != null) {
            long moved;
            try (Transaction tx = Transaction.openOuter()) { moved = s.insert(ItemVariant.of(t.stack), t.stack.getCount(), tx); tx.commit(); }
            if (moved >= t.stack.getCount()) return null;
            ItemStack rest = t.stack.copyWithCount(t.stack.getCount() - (int) moved);
            return new TravelingItem(rest, 0f, type().baseSpeed, d, null, List.of(), false);    // full: back it comes
        }
        // open end: spill out like the old days
        double x = worldPosition.getX() + 0.5 + d.getStepX() * 0.7, y = worldPosition.getY() + 0.5 + d.getStepY() * 0.7, z = worldPosition.getZ() + 0.5 + d.getStepZ() * 0.7;
        ItemEntity e = new ItemEntity(level, x, y, z, t.stack);
        e.setDeltaMovement(d.getStepX() * 0.1, d.getStepY() * 0.1, d.getStepZ() * 0.1);
        level.addFreshEntity(e);
        return null;
    }

    public void receive(TravelingItem t) { items.add(t); dirty = true; }

    /** Start an item off inside this pipe, entering from the given face. */
    public void launch(ItemStack stack, Direction from, List<Direction> route, boolean routed) {
        receive(new TravelingItem(stack, 0f, type().baseSpeed, from, null, route, routed));
    }

    // ------------------------------------------------------------------ wooden pipes: engine pulses pull items out
    public void pulse(Level level, int energy) {
        if (type() != PipeType.WOODEN) return;
        for (int k = 0; k < 6; k++) {
            Direction d = Direction.from3DDataValue((extractSide + k) % 6);
            if (!connected(d) || level.getBlockState(worldPosition.relative(d)).getBlock() instanceof PipeBlock) continue;
            Storage<ItemVariant> s = ItemStorage.SIDED.find(level, worldPosition.relative(d), d.getOpposite());
            if (s == null) continue;
            for (StorageView<ItemVariant> view : s.nonEmptyViews()) {
                ItemVariant v = view.getResource();
                long got;
                try (Transaction tx = Transaction.openOuter()) { got = s.extract(v, energy, tx); tx.commit(); }
                if (got > 0) { launch(v.toStack((int) got), d, List.of(), false); extractSide = (d.get3DDataValue() + 1) % 6; return; }
            }
        }
    }

    // ------------------------------------------------------------------ obsidian pipes: vacuum up items nearby
    private void suckUpItems(Level level) {
        AABB box = new AABB(worldPosition).inflate(1.5);
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, box, ie -> ie.isAlive() && !ie.hasPickUpDelay())) {
            Direction from = Direction.getApproximateNearest(e.getX() - worldPosition.getX() - 0.5, e.getY() - worldPosition.getY() - 0.5, e.getZ() - worldPosition.getZ() - 0.5);
            if (connected(from)) continue;
            launch(e.getItem().copy(), from, List.of(), false);
            e.discard();
        }
    }

    // ------------------------------------------------------------------ fluids
    /** Offer fluid to this pipe from a side; returns how much it took. */
    public long offerFluid(FluidVariant v, long max, Direction from) {
        if (!type().fluid || (!fluid.isBlank() && !fluid.equals(v))) return 0;
        long take = Math.min(max, FLUID_CAPACITY - amount);
        if (take <= 0) return 0;
        fluid = v; amount += take; fluidFrom = from;
        return take;
    }

    private void tickFluid(Level level) {
        if (amount <= 0) { if (!fluid.isBlank()) { fluid = FluidVariant.blank(); dirty = true; } return; }
        long before = amount;
        long share = Math.max(FluidConstants.BUCKET / 40, amount / 3);
        for (Direction d : Direction.values()) {
            if (amount <= 0 || d == fluidFrom || !connected(d)) continue;
            BlockPos n = worldPosition.relative(d);
            if (level.getBlockEntity(n) instanceof PipeBlockEntity next && next.type().fluid) {
                if (next.amount < amount) amount -= next.offerFluid(fluid, Math.min(share, (amount - next.amount + 1) / 2 + 1), d.getOpposite());
            } else {
                Storage<FluidVariant> s = FluidStorage.SIDED.find(level, n, d.getOpposite());
                if (s == null) continue;
                try (Transaction tx = Transaction.openOuter()) { amount -= s.insert(fluid, Math.min(share, amount), tx); tx.commit(); }
            }
        }
        if (amount <= 0) { amount = 0; fluid = FluidVariant.blank(); }
        if (amount != before && level.getGameTime() - lastFluidSync > 5) { lastFluidSync = level.getGameTime(); dirty = true; }
    }

    // ------------------------------------------------------------------ saving / syncing
    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("Travel", TravelingItem.CODEC.listOf(), new ArrayList<>(items));
        if (!fluid.isBlank()) { out.store("Fluid", FluidVariant.CODEC, fluid); out.putLong("Amount", amount); }
        if (type() == PipeType.DIAMOND) ContainerHelper.saveAllItems(out, filters);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items.clear();
        in.read("Travel", TravelingItem.CODEC.listOf()).ifPresent(items::addAll);
        fluid = in.read("Fluid", FluidVariant.CODEC).orElse(FluidVariant.blank());
        amount = in.getLongOr("Amount", 0L);
        for (int i = 0; i < filters.size(); i++) filters.set(i, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, filters);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveCustomOnly(registries); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) for (TravelingItem t : items) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, t.stack);
        super.preRemoveSideEffects(pos, state);
    }

    // ------------------------------------------------------------------ diamond pipe screen
    @Override public Component getDisplayName() { return Component.translatable("block.ryancraft.diamond_pipe"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new DiamondPipeMenu(id, inv, new FilterContainer(this)); }

    public void filtersChanged() { dirty = true; setChanged(); }
}

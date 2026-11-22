package com.ryancraft.pipes;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnderTerminalBlockEntity extends BlockEntity implements MenuProvider {
    /** Where requested items arrive. */
    public final SimpleContainer buffer = new SimpleContainer(18) {
        @Override public void setChanged() { super.setChanged(); EnderTerminalBlockEntity.this.setChanged(); }
    };

    public EnderTerminalBlockEntity(BlockPos pos, BlockState state) { super(RyanPipes.TERMINAL_ENTITY, pos, state); }

    /** An inventory on the network, the pipe next to it, and the turns from there to the terminal. */
    public record Endpoint(BlockPos inv, BlockPos pipe, Direction pipeToInv, List<Direction> toTerminal, List<Direction> fromTerminal, BlockPos firstPipe, Direction firstPipeFromTerminal) {}

    /** Walk the pipes out from the terminal and list every inventory they touch. */
    public List<Endpoint> scan(Level level) {
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        Map<BlockPos, Direction> stepFromParent = new HashMap<>();
        Map<BlockPos, Direction> startFace = new HashMap<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        for (Direction d : Direction.values()) {
            BlockPos p = worldPosition.relative(d);
            if (level.getBlockEntity(p) instanceof PipeBlockEntity pipe && !pipe.type().fluid && !parent.containsKey(p)) {
                parent.put(p, null); startFace.put(p, d.getOpposite()); q.add(p);
            }
        }
        List<Endpoint> out = new ArrayList<>();
        Set<BlockPos> seenInv = new HashSet<>();
        while (!q.isEmpty() && parent.size() < 4096) {
            BlockPos p = q.poll();
            BlockState ps = level.getBlockState(p);
            for (Direction d : Direction.values()) {
                if (!ps.getValue(PipeBlock.SIDES.get(d))) continue;
                BlockPos n = p.relative(d);
                if (level.getBlockEntity(n) instanceof PipeBlockEntity np && !np.type().fluid) {
                    if (!parent.containsKey(n)) { parent.put(n, p); stepFromParent.put(n, d); q.add(n); }
                    continue;
                }
                if (n.equals(worldPosition) || level.getBlockEntity(n) instanceof EnderTerminalBlockEntity) continue;
                if (ItemStorage.SIDED.find(level, n, d.getOpposite()) == null || !seenInv.add(n)) continue;
                BlockState is = level.getBlockState(n);                      // count a double chest once
                if (is.getBlock() instanceof ChestBlock && is.getValue(ChestBlock.TYPE) != ChestType.SINGLE
                        && seenInv.contains(n.relative(ChestBlock.getConnectedDirection(is)))) continue;
                // path: pipe chain back to the terminal
                List<BlockPos> chain = new ArrayList<>();
                for (BlockPos c = p; c != null; c = parent.get(c)) chain.add(c);
                List<Direction> toTerm = new ArrayList<>();
                for (int i = 0; i < chain.size() - 1; i++) toTerm.add(stepFromParent.get(chain.get(i)).getOpposite());
                BlockPos first = chain.get(chain.size() - 1);
                toTerm.add(startFace.get(first));                           // out of the first pipe into the terminal
                List<Direction> fromTerm = new ArrayList<>();
                for (int i = chain.size() - 2; i >= 0; i--) fromTerm.add(stepFromParent.get(chain.get(i)));
                fromTerm.add(d);                                             // out of the last pipe into the inventory
                out.add(new Endpoint(n, p, d, toTerm, fromTerm, first, startFace.get(first)));
            }
        }
        return out;
    }

    private static Storage<ItemVariant> storage(Level level, Endpoint e) { return ItemStorage.SIDED.find(level, e.inv(), e.pipeToInv().getOpposite()); }

    /** Everything on the network: item -> count. */
    public Map<ItemVariant, Long> contents(Level level) {
        Map<ItemVariant, Long> all = new LinkedHashMap<>();
        for (Endpoint e : scan(level)) {
            Storage<ItemVariant> s = storage(level, e);
            if (s == null) continue;
            for (StorageView<ItemVariant> v : s.nonEmptyViews()) all.merge(v.getResource(), v.getAmount(), Long::sum);
        }
        return all;
    }

    /** Pull up to {@code count} of an item off the network and send it down the pipes here. */
    public void request(Level level, ItemVariant item, int count) {
        int left = count;
        for (Endpoint e : scan(level)) {
            if (left <= 0) break;
            Storage<ItemVariant> s = storage(level, e);
            if (s == null || !(level.getBlockEntity(e.pipe()) instanceof PipeBlockEntity pipe)) continue;
            long got;
            try (Transaction tx = Transaction.openOuter()) { got = s.extract(item, left, tx); tx.commit(); }
            while (got > 0) {
                int n = (int) Math.min(got, item.getItem().getDefaultMaxStackSize());
                pipe.launch(item.toStack(n), e.pipeToInv(), e.toTerminal(), true);
                got -= n; left -= n;
            }
        }
        if (left < count) level.playSound(null, worldPosition, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 0.3f, 1.6f);
    }

    /** Send items off to wherever on the network has room. Returns what couldn't go anywhere. */
    public ItemStack deposit(Level level, ItemStack stack) {
        if (stack.isEmpty()) return stack;
        ItemVariant v = ItemVariant.of(stack);
        List<Endpoint> ends = scan(level);
        ends.sort(Comparator.comparingInt(e -> holds(level, e, v) ? 0 : 1));       // join existing stacks first
        for (Endpoint e : ends) {
            Storage<ItemVariant> s = storage(level, e);
            if (s == null) continue;
            long room = StorageUtil.simulateInsert(s, v, stack.getCount(), null);
            if (room <= 0 || !(level.getBlockEntity(e.firstPipe()) instanceof PipeBlockEntity first)) continue;
            ItemStack send = stack.split((int) room);
            first.launch(send, e.firstPipeFromTerminal(), e.fromTerminal(), true);
            if (stack.isEmpty()) break;
        }
        level.playSound(null, worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.15f, 1.8f);
        return stack;
    }

    private static boolean holds(Level level, Endpoint e, ItemVariant v) {
        Storage<ItemVariant> s = storage(level, e);
        if (s == null) return false;
        for (StorageView<ItemVariant> view : s.nonEmptyViews()) if (view.getResource().equals(v)) return true;
        return false;
    }

    /** A routed item arriving from the pipes. */
    public ItemStack receive(ItemStack stack) {
        return buffer.addItem(stack);
    }

    @Override public Component getDisplayName() { return Component.translatable("block.ryancraft.ender_terminal"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new EnderTerminalMenu(id, inv, buffer, this); }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, buffer.getItems()); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); ContainerHelper.loadAllItems(in, buffer.getItems()); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropContents(level, pos, buffer);
        super.preRemoveSideEffects(pos, state);
    }
}

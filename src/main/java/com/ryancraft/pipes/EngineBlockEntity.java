package com.ryancraft.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** Pulses what the engine faces. Stirling engines hold one stack of fuel (hoppers can feed them). */
public class EngineBlockEntity extends BlockEntity implements WorldlyContainer {
    private final NonNullList<ItemStack> fuel = NonNullList.withSize(1, ItemStack.EMPTY);
    private int burn;
    /** 0 = cold, 1 = fully warmed up. Builds while running, fades when stopped (tracked on both sides alike). */
    private float warmth;
    /** Server: progress toward the next pulse. Client: the piston's phase (whole numbers = closed). */
    private float progress, phase, prevPhase;

    public EngineBlockEntity(BlockPos pos, BlockState state) { super(RyanPipes.ENGINE_ENTITY, pos, state); }

    private EngineBlock.Kind kind() { return getBlockState().getBlock() instanceof EngineBlock e ? e.kind : EngineBlock.Kind.REDSTONE; }
    /** Ticks between pulses once warm. */
    private int period() { return kind() == EngineBlock.Kind.REDSTONE ? 40 : 16; }
    /** Ticks to warm up from cold. */
    private int warmup() { return kind() == EngineBlock.Kind.REDSTONE ? 200 : 160; }
    /** Stroke speed: a slow, lazy start that picks up as it warms. */
    private float speed() { return 0.12f + 0.88f * warmth * warmth; }

    private void warm(boolean running) {
        warmth = running ? Math.min(1f, warmth + 1f / warmup()) : Math.max(0f, warmth - 2f / warmup());
    }

    public float warmth() { return warmth; }

    /** Piston extension 0 (closed) .. 1 (out), smoothly between ticks. */
    public float stroke(float partial) {
        float p = prevPhase + (phase - prevPhase) * partial;
        return (float) (0.5 - 0.5 * Math.cos(p * Math.PI * 2));
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EngineBlockEntity be) {
        boolean running = state.getValue(EngineBlock.RUNNING);
        be.warm(running);
        be.prevPhase = be.phase;
        float frac = be.phase - (float) Math.floor(be.phase);
        if (running || frac > 0.0001f) {                     // stopped engines finish their stroke and settle closed
            float next = be.phase + Math.max(be.speed(), running ? 0f : 0.35f) / be.period();
            if (!running && Math.floor(next) > Math.floor(be.phase)) next = (float) Math.floor(next);
            be.phase = next;
        }
        if (be.phase > 1000f) { be.phase -= 1000f; be.prevPhase -= 1000f; }
    }
    /** Items a wooden pipe pulls per pulse. */
    private int energy() { return kind() == EngineBlock.Kind.REDSTONE ? 1 : 8; }

    public boolean isFuel(ItemStack s) { return level != null && level.fuelValues().isFuel(s); }

    public void addFuel(ItemStack s, Player p) {
        ItemStack cur = fuel.get(0);
        if (cur.isEmpty()) { fuel.set(0, s.split(s.getMaxStackSize())); }
        else if (ItemStack.isSameItemSameComponents(cur, s)) { int n = Math.min(s.getCount(), cur.getMaxStackSize() - cur.getCount()); cur.grow(n); s.shrink(n); }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EngineBlockEntity be) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean running = powered;
        if (be.kind() == EngineBlock.Kind.STIRLING) {               // Stirling engines just need fuel, like a furnace
            if (be.burn <= 0 && !be.fuel.get(0).isEmpty()) {
                be.burn = level.fuelValues().burnDuration(be.fuel.get(0));
                ItemStack f = be.fuel.get(0); ItemStack rem = f.getItem().getCraftingRemainder();
                f.shrink(1); if (f.isEmpty() && rem != null && !rem.isEmpty()) be.fuel.set(0, rem.copy());
                be.setChanged();
            }
            running = be.burn > 0;
            if (be.burn > 0) be.burn--;
        }
        if (running != state.getValue(EngineBlock.RUNNING)) level.setBlock(pos, state = state.setValue(EngineBlock.RUNNING, running), Block.UPDATE_CLIENTS);
        be.warm(running);
        if (!running) { be.progress = 0; return; }
        be.progress += be.speed() / be.period();                // pulses come slowly at first, then at full rate
        if (be.progress >= 1f) {
            be.progress -= 1f;
            // stacked engines (BC style): an engine facing another engine passes its pulse down the line
            Direction f = state.getValue(EngineBlock.FACING);
            BlockPos target = pos.relative(f);
            for (int hop = 0; hop < 8 && level.getBlockEntity(target) instanceof EngineBlockEntity next; hop++)
                target = target.relative(next.getBlockState().getValue(EngineBlock.FACING));
            if (level.getBlockEntity(target) instanceof PipeBlockEntity pipe) pipe.pulse(level, be.energy());
            else if (level.getBlockEntity(target) instanceof PumpBlockEntity pump) pump.pulse(level);
            else if (level.getBlockEntity(target) instanceof HandPumpBlockEntity hp) hp.stroke(level, null, net.minecraft.world.InteractionHand.MAIN_HAND);
            else if (level.getBlockEntity(target) instanceof net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity bf && level instanceof net.minecraft.server.level.ServerLevel sl)
                com.ryancraft.metal.Steel.stoke(sl, target, bf);        // bellows: hot enough for steel
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.12f, be.kind() == EngineBlock.Kind.REDSTONE ? 1.4f : 1.7f);
        }
    }

    // fuel slot for hoppers
    @Override public int[] getSlotsForFace(Direction d) { return kind() == EngineBlock.Kind.STIRLING ? new int[]{0} : new int[0]; }
    @Override public boolean canPlaceItemThroughFace(int i, ItemStack s, @Nullable Direction d) { return kind() == EngineBlock.Kind.STIRLING && isFuel(s); }
    @Override public boolean canTakeItemThroughFace(int i, ItemStack s, Direction d) { return false; }
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return fuel.get(0).isEmpty(); }
    @Override public ItemStack getItem(int i) { return fuel.get(0); }
    @Override public ItemStack removeItem(int i, int n) { return ContainerHelper.removeItem(fuel, i, n); }
    @Override public ItemStack removeItemNoUpdate(int i) { return ContainerHelper.takeItem(fuel, i); }
    @Override public void setItem(int i, ItemStack s) { fuel.set(0, s); setChanged(); }
    @Override public boolean stillValid(Player p) { return true; }
    @Override public void clearContent() { fuel.set(0, ItemStack.EMPTY); }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, fuel); out.putInt("Burn", burn); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); ContainerHelper.loadAllItems(in, fuel); burn = in.getIntOr("Burn", 0); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropContents(level, pos, this);
        super.preRemoveSideEffects(pos, state);
    }
}

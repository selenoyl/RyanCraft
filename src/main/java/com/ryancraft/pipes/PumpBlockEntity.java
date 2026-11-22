package com.ryancraft.pipes;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Vanilla-friendly pumping: it finds the pool below (down through up to 64 blocks of air or fluid), and takes the source
 * farthest from it. Water that's an infinite source (two or more connected sources, the same rule a bucket follows) is
 * left in place; lava pools are drained block by block.
 */
public class PumpBlockEntity extends BlockEntity {
    public final SingleVariantStorage<FluidVariant> tank = new SingleVariantStorage<>() {
        @Override protected FluidVariant getBlankVariant() { return FluidVariant.blank(); }
        @Override protected long getCapacity(FluidVariant v) { return FluidConstants.BUCKET * 4; }
        @Override protected boolean canInsert(FluidVariant v) { return false; }
        @Override protected void onFinalCommit() { setChanged(); }
    };

    public PumpBlockEntity(BlockPos pos, BlockState state) { super(RyanPipes.PUMP_ENTITY, pos, state); }

    public void pulse(Level level) {
        if (tank.amount > FluidConstants.BUCKET * 3) return;
        BlockPos p = worldPosition.below();
        for (int i = 0; i < 64 && level.getFluidState(p).isEmpty() && level.getBlockState(p).isAir(); i++) p = p.below();
        FluidState fs = level.getFluidState(p);
        if (fs.isEmpty()) return;
        boolean water = fs.is(FluidTags.WATER);
        FluidVariant v = FluidVariant.of(water ? Fluids.WATER : Fluids.LAVA);
        if (!tank.isResourceBlank() && !tank.variant.equals(v)) return;
        // the connected sources, nearest first; take the farthest
        Set<BlockPos> seen = new HashSet<>(); ArrayDeque<BlockPos> q = new ArrayDeque<>(); BlockPos far = null; int sources = 0;
        q.add(p); seen.add(p);
        while (!q.isEmpty() && seen.size() < 512) {
            BlockPos c = q.poll();
            FluidState f = level.getFluidState(c);
            if (f.isEmpty() || f.is(FluidTags.WATER) != water) continue;
            if (f.isSource()) { far = c; sources++; }
            for (Direction d : Direction.values()) {
                if (d == Direction.UP && c.getY() >= p.getY()) continue;
                BlockPos n = c.relative(d);
                if (n.distManhattan(p) <= 32 && seen.add(n)) q.add(n);
            }
        }
        if (far == null) return;
        if (!water || sources < 2) level.setBlock(far, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        try (Transaction tx = Transaction.openOuter()) {
            tank.variant = v; tank.amount += FluidConstants.BUCKET; tx.commit();
        }
        setChanged();
        level.playSound(null, worldPosition, water ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.4f, 1.0f);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PumpBlockEntity be) {
        if (be.tank.amount <= 0) return;
        for (Direction d : Direction.values()) {
            if (d == Direction.DOWN || be.tank.amount <= 0) continue;
            BlockPos n = pos.relative(d);
            long give = Math.min(be.tank.amount, FluidConstants.BUCKET / 10);
            long took = 0;
            if (level.getBlockEntity(n) instanceof PipeBlockEntity pipe) took = pipe.offerFluid(be.tank.variant, give, d.getOpposite());
            else {
                Storage<FluidVariant> s = FluidStorage.SIDED.find(level, n, d.getOpposite());
                if (s != null) try (Transaction tx = Transaction.openOuter()) { took = s.insert(be.tank.variant, give, tx); tx.commit(); }
            }
            if (took > 0) { be.tank.amount -= took; if (be.tank.amount <= 0) be.tank.variant = FluidVariant.blank(); be.setChanged(); }
        }
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.store("Fluid", FluidVariant.CODEC, tank.variant); out.putLong("Amount", tank.amount); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); tank.variant = in.read("Fluid", FluidVariant.CODEC).orElse(FluidVariant.blank()); tank.amount = in.getLongOr("Amount", 0L); }
}

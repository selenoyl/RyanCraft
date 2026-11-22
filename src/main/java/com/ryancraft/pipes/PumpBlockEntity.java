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

    /** How far down the pool is (the intake tube's length), and when it last pumped (the screw spins for a while). */
    public int tubeDepth;
    public long lastPump = -1000;

    public void pulse(Level level) {
        if (tank.amount > FluidConstants.BUCKET * 3) return;
        FluidWell.Draw d = FluidWell.draw(level, worldPosition, 64, false);
        if (d == null || (!tank.isResourceBlank() && !tank.variant.equals(d.fluid()))) return;
        tank.variant = d.fluid(); tank.amount += FluidConstants.BUCKET;
        tubeDepth = d.depth(); lastPump = level.getGameTime();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        boolean water = d.fluid().getFluid() == Fluids.WATER;
        level.playSound(null, worldPosition, water ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.35f, 1.0f);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PumpBlockEntity be) {
        if (be.tank.amount <= 0) return;
        for (Direction d : Direction.values()) {
            if (d == Direction.DOWN || be.tank.amount <= 0) continue;
            BlockPos n = pos.relative(d);
            long took = FluidWell.push(level, pos, d, be.tank.variant, Math.min(be.tank.amount, FluidConstants.BUCKET / 10));
            if (took > 0) { be.tank.amount -= took; if (be.tank.amount <= 0) be.tank.variant = FluidVariant.blank(); be.setChanged(); }
        }
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.store("Fluid", FluidVariant.CODEC, tank.variant); out.putLong("Amount", tank.amount); out.putInt("Tube", tubeDepth); out.putLong("LastPump", lastPump); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); tank.variant = in.read("Fluid", FluidVariant.CODEC).orElse(FluidVariant.blank()); tank.amount = in.getLongOr("Amount", 0L); tubeDepth = in.getIntOr("Tube", 0); lastPump = in.getLongOr("LastPump", -1000L); }
    @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider r) { return this.saveCustomOnly(r); }
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() { return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this); }
}

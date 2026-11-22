package com.ryancraft.pipes;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class TankBlockEntity extends BlockEntity {
    public final SingleVariantStorage<FluidVariant> tank = new SingleVariantStorage<>() {
        @Override protected FluidVariant getBlankVariant() { return FluidVariant.blank(); }
        @Override protected long getCapacity(FluidVariant v) { return FluidConstants.BUCKET * 16; }
        @Override protected void onFinalCommit() {
            setChanged();
            if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    };

    public TankBlockEntity(BlockPos pos, BlockState state) { super(RyanPipes.TANK_ENTITY, pos, state); }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.store("Fluid", FluidVariant.CODEC, tank.variant); out.putLong("Amount", tank.amount); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); tank.variant = in.read("Fluid", FluidVariant.CODEC).orElse(FluidVariant.blank()); tank.amount = in.getLongOr("Amount", 0L); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r) { return this.saveCustomOnly(r); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}

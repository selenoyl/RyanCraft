package com.ryancraft.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class FishPlaqueBlockEntity extends BlockEntity {
    public ItemStack fish = ItemStack.EMPTY;

    public FishPlaqueBlockEntity(BlockPos pos, BlockState state) { super(RyanFishing.PLAQUE_ENTITY, pos, state); }

    public void mount(ItemStack f) {
        fish = f.copy();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); if (!fish.isEmpty()) out.store("Fish", ItemStack.CODEC, fish); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); fish = in.read("Fish", ItemStack.CODEC).orElse(ItemStack.EMPTY); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r) { return this.saveCustomOnly(r); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !fish.isEmpty()) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, fish);
    }
}

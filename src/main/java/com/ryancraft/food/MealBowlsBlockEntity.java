package com.ryancraft.food;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** The meals in each set-down bowl, synced so each bowl shows its own colour. */
public class MealBowlsBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> meals = NonNullList.withSize(4, ItemStack.EMPTY);

    public MealBowlsBlockEntity(BlockPos pos, BlockState state) { super(Cooking.MEAL_BOWLS_ENTITY, pos, state); }

    public int count() { int n = 0; for (ItemStack s : meals) if (!s.isEmpty()) n++; return n; }
    void add(ItemStack s) { for (int i = 0; i < 4; i++) if (meals.get(i).isEmpty()) { meals.set(i, s); break; } sync(); }
    ItemStack takeLast() {
        for (int i = 3; i >= 0; i--) if (!meals.get(i).isEmpty()) { ItemStack s = meals.get(i); meals.set(i, ItemStack.EMPTY); sync(); return s; }
        return ItemStack.EMPTY;
    }

    /** The colour of bowl i's meal (PotColour.RGB), for the block colour handler. */
    public int colour(int i) {
        if (i >= 4) return -1;
        Meal m = meals.get(i).get(Cooking.MEAL_DATA);
        if (m == null) return PotColour.RGB[PotColour.BROWN];
        return PotColour.RGB[PotColour.pot(m.kind().equals("chowder") || m.kind().equals("porridge"), m.ingredients())];
    }

    private void sync() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) for (ItemStack s : meals) if (!s.isEmpty()) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .3, pos.getZ() + .5, s);
        meals.clear();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, meals, true); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); meals.clear(); ContainerHelper.loadAllItems(in, meals); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r) { return saveCustomOnly(r); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}

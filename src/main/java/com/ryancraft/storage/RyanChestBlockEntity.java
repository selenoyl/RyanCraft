package com.ryancraft.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** The special chests' contents and lid. See-through chests keep their contents synced to clients so they can be drawn. */
public class RyanChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private NonNullList<ItemStack> items;
    final ChestLidController lid = new ChestLidController();
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override protected void onOpen(Level l, BlockPos p, BlockState s) { sound(SoundEvents.CHEST_OPEN); }
        @Override protected void onClose(Level l, BlockPos p, BlockState s) { sound(SoundEvents.CHEST_CLOSE); }
        @Override protected void openerCountChanged(Level l, BlockPos p, BlockState s, int was, int now) { l.blockEvent(p, s.getBlock(), 1, now); }
        @Override public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu m && m.getContainer() == RyanChestBlockEntity.this;
        }
    };

    public RyanChestBlockEntity(BlockPos pos, BlockState state) {
        super(RyanChests.ENTITY, pos, state);
        items = NonNullList.withSize(kind().slots, ItemStack.EMPTY);
    }

    public RyanChestBlock.Kind kind() { return getBlockState().getBlock() instanceof RyanChestBlock b ? b.kind : RyanChestBlock.Kind.OBSIDIAN; }
    public NonNullList<ItemStack> contents() { return items; }

    private void sound(SoundEvent e) {
        if (level == null) return;
        SoundEvent s = kind() == RyanChestBlock.Kind.OBSIDIAN ? e : (e == SoundEvents.CHEST_OPEN ? SoundEvents.COPPER_CHEST_OPEN : SoundEvents.COPPER_CHEST_CLOSE);
        level.playSound(null, worldPosition, s, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> list) { items = list; }
    @Override protected Component getDefaultName() { return Component.translatable("container.ryancraft." + kind().id); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return items.size() >= 54 ? ChestMenu.sixRows(id, inv, this) : ChestMenu.threeRows(id, inv, this);
    }

    @Override public float getOpenNess(float partial) { return lid.getOpenness(partial); }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == 1) { lid.shouldBeOpen(data > 0); return true; }
        return super.triggerEvent(id, data);
    }

    @Override public void startOpen(ContainerUser u) {
        if (!remove && !u.getLivingEntity().isSpectator()) openers.incrementOpeners(u.getLivingEntity(), getLevel(), getBlockPos(), getBlockState(), u.getContainerInteractionRange());
    }
    @Override public void stopOpen(ContainerUser u) {
        if (!remove && !u.getLivingEntity().isSpectator()) openers.decrementOpeners(u.getLivingEntity(), getLevel(), getBlockPos(), getBlockState());
    }
    public void recheckOpen() { if (!remove) openers.recheckOpeners(getLevel(), getBlockPos(), getBlockState()); }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide() && kind().seeThrough) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (!trySaveLootTable(out)) ContainerHelper.saveAllItems(out, items);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(in)) ContainerHelper.loadAllItems(in, items);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r) { return kind().seeThrough ? saveCustomOnly(r) : super.getUpdateTag(r); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return kind().seeThrough ? ClientboundBlockEntityDataPacket.create(this) : null; }
}

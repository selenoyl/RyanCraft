package com.ryancraft.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The contents of each pot on the block. */
public class ClayPotBlockEntity extends BlockEntity {
    public static final int SLOTS = 5;
    private final NonNullList<ItemStack>[] pots;
    private boolean dropped;

    @SuppressWarnings("unchecked")
    public ClayPotBlockEntity(BlockPos pos, BlockState state) {
        super(RyanStorage.CLAY_POT_ENTITY, pos, state);
        pots = new NonNullList[4];
        for (int i = 0; i < 4; i++) pots[i] = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    }

    public PotContainer container(int pot) { return new PotContainer(this, pots[pot]); }

    void fillFrom(int pot, ItemStack item) {
        ItemContainerContents c = item.get(DataComponents.CONTAINER);
        if (c != null) { c.copyInto(pots[pot]); setChanged(); }
    }

    /** Puts as much of the stack as fits into one pot; returns what's left. */
    ItemStack insert(int pot, ItemStack stack) {
        NonNullList<ItemStack> items = pots[pot];
        for (int i = 0; i < SLOTS && !stack.isEmpty(); i++) {
            ItemStack s = items.get(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, stack)) {
                int move = Math.min(stack.getCount(), s.getMaxStackSize() - s.getCount());
                s.grow(move); stack.shrink(move);
            }
        }
        for (int i = 0; i < SLOTS && !stack.isEmpty(); i++) {
            if (items.get(i).isEmpty()) { items.set(i, stack.copy()); stack.setCount(0); }
        }
        setChanged();
        return stack;
    }

    /** Each pot drops as an item carrying its contents. */
    void dropPots(boolean drop) {
        if (dropped || level == null) return;
        dropped = true;
        int n = getBlockState().getValue(ClayPotBlock.POTS);
        if (!drop) return;
        for (int i = 0; i < n; i++) {
            ItemStack pot = new ItemStack(RyanStorage.CLAY_POT_ITEM);
            if (pots[i].stream().anyMatch(s -> !s.isEmpty())) pot.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(pots[i]));
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.3, worldPosition.getZ() + 0.5, pot);
            pots[i].clear();
        }
    }

    /** Broken some other way (explosion, piston, support removed): pots still drop with their contents. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        dropPots(true);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        for (int i = 0; i < 4; i++) net.minecraft.world.ContainerHelper.saveAllItems(out.child("Pot" + i), pots[i]);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        for (int i = 0; i < 4; i++) {
            pots[i].clear();
            int k = i;
            in.child("Pot" + i).ifPresent(c -> net.minecraft.world.ContainerHelper.loadAllItems(c, pots[k]));
        }
    }
}

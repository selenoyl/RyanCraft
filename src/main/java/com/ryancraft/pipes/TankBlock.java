package com.ryancraft.pipes;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A glass tank: holds 16 buckets of one fluid, shows its level. Fill and empty it with buckets or fluid pipes. */
public class TankBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    public TankBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(TankBlock::new); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TankBlockEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TankBlockEntity be && FluidStorageUtil.interactWithFluidStorage(be.tank, player, hand)) return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}

package com.ryancraft.pipes;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * An iron-hooped wooden vat: an open tub holding 8 buckets of one fluid. It catches rain like a cauldron, fills glass
 * bottles and buckets, and takes fluid from pipes and pumps.
 */
public class VatBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.join(Block.box(1, 0, 1, 15, 15, 15), Block.box(3, 1, 3, 13, 15, 13), net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST);
    public VatBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(VatBlock::new); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }
    @Override protected boolean isRandomlyTicking(BlockState s) { return true; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new VatBlockEntity(pos, state); }

    /** Rain trickles in (water only, open sky). */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isRainingAt(pos.above()) || !(level.getBlockEntity(pos) instanceof VatBlockEntity be)) return;
        if (!be.tank.isResourceBlank() && be.tank.variant.getFluid() != Fluids.WATER) return;
        be.fill(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET / 4);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof VatBlockEntity be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.is(Items.GLASS_BOTTLE) && be.tank.variant.getFluid() == Fluids.WATER && be.tank.amount >= FluidConstants.BOTTLE) {
            if (!level.isClientSide()) {
                be.drain(FluidConstants.BOTTLE);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        if (FluidStorageUtil.interactWithFluidStorage(be.tank, player, hand)) return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}

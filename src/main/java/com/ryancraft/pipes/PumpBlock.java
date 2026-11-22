package com.ryancraft.pipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Sits over water or lava; each engine pulse draws up a bucketful and pushes it into fluid pipes or tanks. */
public class PumpBlock extends BaseEntityBlock {
    public PumpBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(PumpBlock::new); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PumpBlockEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof PumpBlockEntity be && net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil.interactWithFluidStorage(be.tank, player, hand))
            return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> t) {
        return level.isClientSide() ? null : createTickerHelper(t, RyanPipes.PUMP_ENTITY, PumpBlockEntity::serverTick);
    }
}

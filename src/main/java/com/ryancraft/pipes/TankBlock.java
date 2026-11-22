package com.ryancraft.pipes;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A glass tank in a dark-iron frame: 16 buckets of one fluid, its level showing through the glass. Stack them and the
 * frame between them drops away and the fluid settles downward, so a stack fills like one tall tank.
 */
public class TankBlock extends BaseEntityBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP, DOWN = BlockStateProperties.DOWN;
    public TankBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(UP, false).setValue(DOWN, false)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(TankBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(UP, DOWN); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TankBlockEntity(pos, state); }

    private BlockState joined(BlockGetter level, BlockPos pos, BlockState s) {
        return s.setValue(UP, level.getBlockState(pos.above()).getBlock() instanceof TankBlock).setValue(DOWN, level.getBlockState(pos.below()).getBlock() instanceof TankBlock);
    }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) { return joined(ctx.getLevel(), ctx.getClickedPos(), defaultBlockState()); }
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess t, BlockPos pos, Direction dir, BlockPos nPos, BlockState nState, RandomSource r) {
        return dir.getAxis() == Direction.Axis.Y ? joined(level, pos, state) : state;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TankBlockEntity be && FluidStorageUtil.interactWithFluidStorage(be.tank, player, hand)) return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> t) {
        return level.isClientSide() ? null : createTickerHelper(t, RyanPipes.TANK_ENTITY, TankBlockEntity::serverTick);
    }
}

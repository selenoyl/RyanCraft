package com.ryancraft.fish;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/** A wooden plaque for the wall: use a fish on it to mount it, with its length engraved underneath. */
public class FishPlaqueBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape N = Block.box(1, 3, 14, 15, 13, 16), S = Block.box(1, 3, 0, 15, 13, 2),
            W = Block.box(14, 3, 1, 16, 13, 15), E = Block.box(0, 3, 1, 2, 13, 15);

    public FishPlaqueBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(FishPlaqueBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FishPlaqueBlockEntity(pos, state); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) { case SOUTH -> S; case WEST -> W; case EAST -> E; default -> N; };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        for (Direction d : ctx.getNearestLookingDirections()) {
            if (d.getAxis().isVertical()) continue;
            BlockState s = defaultBlockState().setValue(FACING, d.getOpposite());
            if (s.canSurvive(ctx.getLevel(), ctx.getClickedPos())) return s;
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction back = state.getValue(FACING).getOpposite();
        return level.getBlockState(pos.relative(back)).isFaceSturdy(level, pos.relative(back), state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos np, BlockState ns, RandomSource r) {
        return dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FishPlaqueBlockEntity be)) return InteractionResult.PASS;
        if (be.fish.isEmpty() && stack.has(FishSize.LENGTH)) {
            if (!level.isClientSide()) be.mount(stack.consumeAndReturn(1, player));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FishPlaqueBlockEntity be) || be.fish.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide()) { ItemStack f = be.fish; be.mount(ItemStack.EMPTY); if (!player.addItem(f)) player.drop(f, false); }
        return InteractionResult.SUCCESS;
    }
}

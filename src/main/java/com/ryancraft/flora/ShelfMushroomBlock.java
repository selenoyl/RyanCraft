package com.ryancraft.flora;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A bracket fungus on a tree trunk: one to three shelves stacked up the bark. Place it on the side of a log; use more
 * on it to add shelves, and left alone in the shade it slowly grows its own (up to three). FACING points out from the bark.
 */
public class ShelfMushroomBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ShelfMushroomBlock> CODEC = simpleCodec(ShelfMushroomBlock::new);
    public static final IntegerProperty SHELVES = IntegerProperty.create("shelves", 1, 3);
    private static final VoxelShape N = Block.box(3, 3, 9, 13, 13, 16), S = Block.box(3, 3, 0, 13, 13, 7),
            E = Block.box(0, 3, 3, 7, 13, 13), W = Block.box(9, 3, 3, 16, 13, 13);

    public ShelfMushroomBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SHELVES, 1));
    }

    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, SHELVES); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) { case SOUTH -> S; case EAST -> E; case WEST -> W; default -> N; };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).is(BlockTags.LOGS);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos np, BlockState ns, RandomSource r) {
        return dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(asItem()) && state.getValue(SHELVES) < 3 || super.canBeReplaced(state, ctx);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState here = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (here.is(this)) return here.setValue(SHELVES, Math.min(3, here.getValue(SHELVES) + 1));
        Direction face = ctx.getClickedFace();
        if (face.getAxis().isHorizontal()) {
            BlockState s = defaultBlockState().setValue(FACING, face);
            if (s.canSurvive(ctx.getLevel(), ctx.getClickedPos())) return s;
        }
        for (Direction d : ctx.getNearestLookingDirections()) {      // clicked a top or bottom: try the bark beside
            if (d.getAxis().isVertical()) continue;
            BlockState s = defaultBlockState().setValue(FACING, d.getOpposite());
            if (s.canSurvive(ctx.getLevel(), ctx.getClickedPos())) return s;
        }
        return null;
    }

    @Override protected boolean isRandomlyTicking(BlockState state) { return state.getValue(SHELVES) < 3; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource r) {
        if (r.nextInt(20) == 0 && level.getRawBrightness(pos, 0) < 13) level.setBlock(pos, state.setValue(SHELVES, state.getValue(SHELVES) + 1), 2);
    }
}

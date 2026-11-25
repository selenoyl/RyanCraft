package com.ryancraft.season;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The season calendar hung on a wall, like a painting: its page shows the part of the year (early, mid or late spring,
 * summer, autumn or winter) and turns by itself as the seasons pass. Use it to read the date.
 */
public class SeasonCalendarBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SeasonCalendarBlock> CODEC = simpleCodec(SeasonCalendarBlock::new);
    public static final IntegerProperty PAGE = IntegerProperty.create("page", 0, Seasons.YEAR - 1);
    private static final VoxelShape NORTH = Block.box(2, 1, 15, 14, 15, 16), SOUTH = Block.box(2, 1, 0, 14, 15, 1),
            WEST = Block.box(15, 1, 2, 16, 15, 14), EAST = Block.box(0, 1, 2, 1, 15, 14);

    public SeasonCalendarBlock(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(PAGE, 0)); }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, PAGE); }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (s.getValue(FACING)) { case SOUTH -> SOUTH; case WEST -> WEST; case EAST -> EAST; default -> NORTH; };
    }

    /** It hangs on the side of a block: facing out from the wall you put it on. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getClickedFace();
        if (face.getAxis().isVertical()) return null;
        BlockState s = defaultBlockState().setValue(FACING, face).setValue(PAGE, Seasons.sub(ctx.getLevel()));
        return s.canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? s : null;
    }

    @Override
    protected boolean canSurvive(BlockState s, LevelReader level, BlockPos pos) {
        Direction f = s.getValue(FACING); BlockPos wall = pos.relative(f.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, f);
    }

    @Override
    protected BlockState updateShape(BlockState s, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos nPos, BlockState n, RandomSource r) {
        return dir == s.getValue(FACING).getOpposite() && !s.canSurvive(level, pos) ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : s;
    }

    @Override
    protected void onPlace(BlockState s, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) level.scheduleTick(pos, this, 20);
    }

    /** Turn the page when the season moves on (checked every ten seconds while the chunk is loaded). */
    @Override
    protected void tick(BlockState s, ServerLevel level, BlockPos pos, RandomSource r) {
        int page = Seasons.sub(level);
        if (s.getValue(PAGE) != page) level.setBlock(pos, s.setValue(PAGE, page), Block.UPDATE_CLIENTS);
        level.scheduleTick(pos, this, 200);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) player.displayClientMessage(Seasons.describe(level.getDayTime()), true);
        return InteractionResult.SUCCESS;
    }
}

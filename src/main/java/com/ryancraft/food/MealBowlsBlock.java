package com.ryancraft.food;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Bowls of cooked meals set down on a table: up to four to a block, each showing its meal in its colour. Sneak-use a
 * meal on the top of a block to set it down; use an empty hand to pick the last one back up, meal and all.
 */
public class MealBowlsBlock extends BaseEntityBlock {
    public static final IntegerProperty BOWLS = IntegerProperty.create("bowls", 1, 4);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 4, 15);

    public MealBowlsBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(BOWLS, 1)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(MealBowlsBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BOWLS); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MealBowlsBlockEntity(pos, state); }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState s, LevelReader level, ScheduledTickAccess t, BlockPos pos, Direction dir, BlockPos np, BlockState ns, RandomSource r) {
        return dir == Direction.DOWN && !s.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : s;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MealBowlsBlockEntity be)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ItemStack meal = be.takeLast();
            if (!meal.isEmpty() && !player.addItem(meal)) player.drop(meal, false);
            int n = be.count();
            if (n == 0) level.removeBlock(pos, false); else level.setBlock(pos, state.setValue(BOWLS, n), 3);
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6f, 1.2f);
        }
        return InteractionResult.SUCCESS;
    }

    /** Sets a meal down at pos (a new block, or one more bowl on an existing one); false if there's no room. */
    public static boolean place(Level level, BlockPos pos, ItemStack meal) {
        BlockState here = level.getBlockState(pos);
        if (here.is(Cooking.MEAL_BOWLS)) {
            if (here.getValue(BOWLS) >= 4 || !(level.getBlockEntity(pos) instanceof MealBowlsBlockEntity be)) return false;
            be.add(meal.copyWithCount(1));
            level.setBlock(pos, here.setValue(BOWLS, be.count()), 3);
        } else {
            if (!here.canBeReplaced() || !Cooking.MEAL_BOWLS.defaultBlockState().canSurvive(level, pos)) return false;
            level.setBlock(pos, Cooking.MEAL_BOWLS.defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof MealBowlsBlockEntity be) be.add(meal.copyWithCount(1));
        }
        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7f, 1.3f);
        return true;
    }
}

package com.ryancraft.food;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A fired clay cooking pot. Set it on a campfire (it settles into the flames), pour in water or milk, add up to six
 * ingredients and let it simmer; then ladle portions out with bowls. Sneak with an empty hand to take back the last
 * thing you put in; an empty hand alone tells you what's in it.
 */
public class CookingPotBlock extends BaseEntityBlock {
    public enum Fill implements StringRepresentable {
        EMPTY, WATER, MILK, STEW, SOUP, CHOWDER, PORRIDGE;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }
    public static final BooleanProperty SEATED = BooleanProperty.create("seated"), COOKING = BooleanProperty.create("cooking");
    public static final EnumProperty<Fill> FILL = EnumProperty.create("fill", Fill.class);
    private static final VoxelShape FREE = Block.box(2, 0, 2, 14, 11, 14), SEATED_SHAPE = Block.box(2, 0, 2, 14, 9, 14);

    public CookingPotBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(SEATED, false).setValue(COOKING, false).setValue(FILL, Fill.EMPTY));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(CookingPotBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(SEATED, COOKING, FILL); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return s.getValue(SEATED) ? SEATED_SHAPE : FREE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CookingPotBlockEntity(pos, state); }

    static boolean onCampfire(LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).getBlock() instanceof CampfireBlock; }
    static boolean heated(LevelReader level, BlockPos pos) {
        BlockState b = level.getBlockState(pos.below());
        return b.getBlock() instanceof CampfireBlock && b.getValue(CampfireBlock.LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(SEATED, onCampfire(ctx.getLevel(), ctx.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState s, LevelReader level, ScheduledTickAccess t, BlockPos pos, Direction dir, BlockPos np, BlockState ns, RandomSource r) {
        return dir == Direction.DOWN ? s.setValue(SEATED, onCampfire(level, pos)) : s;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) return InteractionResult.PASS;
        Fill fill = state.getValue(FILL);
        if (fill == Fill.EMPTY && (stack.is(Items.WATER_BUCKET) || stack.is(Items.MILK_BUCKET))) {
            if (!level.isClientSide()) {
                pot.setBase(stack.is(Items.MILK_BUCKET) ? Fill.MILK : Fill.WATER);
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.BOWL) && pot.portions() > 0) {
            if (!level.isClientSide()) {
                ItemStack meal = pot.serve();
                stack.consume(1, player);
                if (!player.addItem(meal)) player.drop(meal, false);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
            return InteractionResult.SUCCESS;
        }
        if ((fill == Fill.WATER || fill == Fill.MILK) && Meal.isIngredient(stack) && pot.ingredientCount() < Meal.MAX_INGREDIENTS) {
            if (!level.isClientSide()) {
                pot.add(stack.copyWithCount(1)); stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4f, 1.4f);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (player.isSecondaryUseActive() && pot.portions() == 0 && pot.ingredientCount() > 0) {
                ItemStack back = pot.takeLast();
                if (!player.addItem(back)) player.drop(back, false);
            } else player.displayClientMessage(pot.describe(), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!state.getValue(COOKING)) return;
        double y = pos.getY() + (state.getValue(SEATED) ? 0.6 : 0.7);
        level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.4, y, pos.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.4, 0, 0.04, 0);
        if (r.nextInt(8) == 0) level.playLocalSound(pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.3f, 1.2f, false);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Cooking.POT_ENTITY, (l, p, s, be) -> be.tick());
    }
}

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A cast-iron cooking pot. Set it over a campfire and it stands on an iron tripod, or, with solid blocks on two
 * opposite sides (a fireplace), hangs from a crane bar between them. Use it on the campfire itself and it sits right in
 * the fire, one block that is both: it keeps the campfire's fuel burning (feed it more by right-clicking with logs or
 * other fuel), a shovel puts the fire out, flint and steel relights it, and sneaking with an empty hand on an empty pot
 * lifts it off, leaving the campfire burning as it was. Pour in water or milk, add up to six
 * ingredients and let it simmer; then ladle portions out with bowls. Sneak with an empty hand to take back the last
 * thing you put in; an empty hand alone tells you what's in it.
 */
public class CookingPotBlock extends BaseEntityBlock {
    public enum Fill implements StringRepresentable {
        EMPTY, WATER, MILK, STEW, SOUP, CHOWDER, PORRIDGE;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }
    /** How the pot is held over a campfire: not at all, on a tripod, hung from a bar between two blocks (a fireplace), or
     *  sitting right in the campfire (FIRE: the pot was put on the campfire itself, and this block is both). */
    public enum Support implements StringRepresentable {
        NONE, TRIPOD, BAR_X, BAR_Z, FIRE;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }
    public static final EnumProperty<Support> SUPPORT = EnumProperty.create("support", Support.class);
    /** Boiling: the pot has been on the fire long enough (30 seconds) to bubble, and only then does it cook. */
    public static final BooleanProperty COOKING = BooleanProperty.create("cooking");
    /** The colour of what's in it (food/PotColour), tinted onto the liquid. */
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty TINT =
            net.minecraft.world.level.block.state.properties.IntegerProperty.create("tint", 0, 15);
    /** For a pot in the fire: whether the fire is burning. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Fill> FILL = EnumProperty.create("fill", Fill.class);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14), IN_FIRE = Block.box(0, 0, 0, 16, 16, 16);

    public CookingPotBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(SUPPORT, Support.NONE).setValue(LIT, false).setValue(TINT, 0).setValue(COOKING, false).setValue(FILL, Fill.EMPTY));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(CookingPotBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(SUPPORT, LIT, TINT, COOKING, FILL); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return s.getValue(SUPPORT) == Support.FIRE ? IN_FIRE : SHAPE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CookingPotBlockEntity(pos, state); }

    static boolean onCampfire(LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).getBlock() instanceof CampfireBlock; }

    /** Over a campfire: a bar if there are solid blocks on two opposite sides to hang it from, otherwise a tripod. */
    static Support supportFor(LevelReader level, BlockPos pos) {
        if (!onCampfire(level, pos)) return Support.NONE;
        if (solid(level, pos, Direction.EAST) && solid(level, pos, Direction.WEST)) return Support.BAR_X;
        if (solid(level, pos, Direction.NORTH) && solid(level, pos, Direction.SOUTH)) return Support.BAR_Z;
        return Support.TRIPOD;
    }
    private static boolean solid(LevelReader level, BlockPos pos, Direction d) {
        BlockPos n = pos.relative(d); return level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite());
    }
    static boolean heated(LevelReader level, BlockPos pos) {
        BlockState self = level.getBlockState(pos);
        if (self.hasProperty(SUPPORT) && self.getValue(SUPPORT) == Support.FIRE) return self.getValue(LIT);
        BlockState b = level.getBlockState(pos.below());
        return b.getBlock() instanceof CampfireBlock && b.getValue(CampfireBlock.LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(SUPPORT, supportFor(ctx.getLevel(), ctx.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState s, LevelReader level, ScheduledTickAccess t, BlockPos pos, Direction dir, BlockPos np, BlockState ns, RandomSource r) {
        return s.getValue(SUPPORT) == Support.FIRE ? s : s.setValue(SUPPORT, supportFor(level, pos));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) return InteractionResult.PASS;
        Fill fill = state.getValue(FILL);
        if (state.getValue(SUPPORT) == Support.FIRE) {
            if (state.getValue(LIT) && stack.is(net.minecraft.tags.ItemTags.SHOVELS)) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(LIT, false), 3);
                    level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1f, 1f);
                    stack.hurtAndBreak(1, player, hand);
                }
                return InteractionResult.SUCCESS;
            }
            if (!Meal.isIngredient(stack) && level.fuelValues().isFuel(stack)) {                 // feed the fire
                if (!level.isClientSide()) {
                    if (CampfireFuel.addFuel(level, pot, stack)) level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7f, 1f);
                    player.displayClientMessage(CampfireFuel.describe(pot, state.getValue(LIT)), true);
                }
                return InteractionResult.SUCCESS;
            }
            if (!state.getValue(LIT) && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(LIT, true), 3);
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1f, 1f);
                    if (stack.is(Items.FIRE_CHARGE)) stack.consume(1, player); else stack.hurtAndBreak(1, player, hand);
                }
                return InteractionResult.SUCCESS;
            }
        }
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
            if (player.isSecondaryUseActive() && state.getValue(SUPPORT) == Support.FIRE && state.getValue(FILL) == Fill.EMPTY && pot.ingredientCount() == 0) {
                level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, state.getValue(LIT)), 3);    // lift the pot off
                if (level.getBlockEntity(pos) instanceof FuelHolder camp) camp.ryancraft$copyFireFrom(pot);                       // the fire stays
                ItemStack p = new ItemStack(Cooking.COOKING_POT_ITEM);
                if (!player.addItem(p)) player.drop(p, false);
                level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 0.8f, 1f);
            } else if (player.isSecondaryUseActive() && pot.portions() == 0 && pot.ingredientCount() > 0) {
                ItemStack back = pot.takeLast();
                if (!player.addItem(back)) player.drop(back, false);
            } else player.displayClientMessage(pot.describe(), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (state.getValue(SUPPORT) == Support.FIRE && state.getValue(LIT)) {                   // the campfire's own smoke and crackle
            if (r.nextInt(10) == 0) level.playLocalSound(pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f + r.nextFloat(), r.nextFloat() * 0.7f + 0.6f, false);
            if (r.nextInt(3) == 0) level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5 + r.nextDouble() / 3 * (r.nextBoolean() ? 1 : -1),
                    pos.getY() + 1.2, pos.getZ() + 0.5 + r.nextDouble() / 3 * (r.nextBoolean() ? 1 : -1), 0, 0.07, 0);
        }
        if (!state.getValue(COOKING)) return;                                                        // boiling: bubbles, steam and a low simmer
        double y = pos.getY() + (state.getValue(SUPPORT) == Support.FIRE ? 0.92 : 0.55);
        for (int i = 0; i < 3; i++)
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.45, y, pos.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.45, 0, 0.01, 0);
        if (r.nextInt(2) == 0) level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.3, y + 0.2, pos.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.3, 0, 0.03, 0);
        if (r.nextInt(2) == 0) level.playLocalSound(pos, com.ryancraft.RyanSounds.POT_SIMMER, SoundSource.BLOCKS, 0.5f + r.nextFloat() * 0.2f, 0.9f + r.nextFloat() * 0.2f, false);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Cooking.POT_ENTITY, (l, p, s, be) -> be.tick());
    }
}

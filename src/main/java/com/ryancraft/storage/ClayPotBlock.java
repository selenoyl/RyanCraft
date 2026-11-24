package com.ryancraft.storage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

/**
 * Fired clay pots for keeping food, Vintage Story style. Up to four sit together on one block (like candles); each
 * has its own little store. Right-click a pot to open it, or right-click it holding food to drop the food straight in.
 * A broken pot keeps what's inside it. Use a dye on a pot to colour its band (just that pot), or dye the pot item in
 * the crafting grid like leather armour; the colour stays with the pot when it's broken and placed again.
 */
public class ClayPotBlock extends BaseEntityBlock {
    public static final IntegerProperty POTS = IntegerProperty.create("pots", 1, 4);
    /** Where each pot sits (pixel x, z of its 6x6 footprint) for 1..4 pots. */
    static final int[][][] SPOTS = {
            {{5, 5}},
            {{1, 5}, {9, 5}},
            {{1, 2}, {9, 2}, {5, 9}},
            {{1, 1}, {9, 1}, {1, 9}, {9, 9}}};
    private static final VoxelShape[] SHAPES = new VoxelShape[4];
    static {
        for (int n = 0; n < 4; n++) {
            VoxelShape s = Shapes.empty();
            for (int[] p : SPOTS[n]) s = Shapes.or(s, Block.box(p[0], 0, p[1], p[0] + 6, 7, p[1] + 6));
            SHAPES[n] = s;
        }
    }

    public ClayPotBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(POTS, 1));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(ClayPotBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(POTS); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPES[s.getValue(POTS) - 1]; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ClayPotBlockEntity(pos, state); }

    // ---- placing up to four
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState here = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (here.is(this)) return here.setValue(POTS, Math.min(4, here.getValue(POTS) + 1));
        return this.defaultBlockState();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) && state.getValue(POTS) < 4 || super.canBeReplaced(state, ctx);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos nPos, BlockState nState, RandomSource r) {
        return dir == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, level, ticks, pos, dir, nPos, nState, r);
    }

    /** The pot this placed item becomes takes the item's contents. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.@Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof ClayPotBlockEntity be) be.fillFrom(state.getValue(POTS) - 1, stack);
    }

    /** Which pot the player is pointing at. */
    static int potAt(BlockState state, BlockPos pos, Vec3 hit) {
        int n = state.getValue(POTS);
        double hx = (hit.x - pos.getX()) * 16, hz = (hit.z - pos.getZ()) * 16;
        int best = 0; double bd = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int[] p = SPOTS[n - 1][i];
            double d = Math.pow(hx - (p[0] + 3), 2) + Math.pow(hz - (p[1] + 3), 2);
            if (d < bd) { bd = d; best = i; }
        }
        return best;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof net.minecraft.world.item.DyeItem dye && level.getBlockEntity(pos) instanceof ClayPotBlockEntity pot) {
            // dye the band of just the pot you're pointing at
            if (!level.isClientSide() && pot.dye(potAt(state, pos, hit.getLocation()), dye.getDyeColor().getTextureDiffuseColor() & 0xFFFFFF)) {
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(this.asItem()) || !PotContainer.storable(stack)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof ClayPotBlockEntity be)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ItemStack rest = be.insert(potAt(state, pos, hit.getLocation()), stack.copy());
            if (rest.getCount() != stack.getCount()) {
                player.setItemInHand(hand, rest);
                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ClayPotBlockEntity be)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            int i = potAt(state, pos, hit.getLocation());
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new PotMenu(id, inv, be.container(i)), Component.translatable("container.ryancraft.clay_pot")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof ClayPotBlockEntity be && level instanceof ServerLevel) be.dropPots(!player.isCreative());
        return super.playerWillDestroy(level, pos, state, player);
    }
}

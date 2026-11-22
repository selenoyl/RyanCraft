package com.ryancraft.pipes;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** A pipe: a thin core with an arm toward each neighbour it connects to. Items (or fluids) travel through, visibly. */
public class PipeBlock extends BaseEntityBlock {
    public static final Map<Direction, BooleanProperty> SIDES = net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION;
    /** Iron pipes only: the one side items leave by. */
    public static final EnumProperty<Direction> OUTPUT = BlockStateProperties.FACING;
    private static final VoxelShape CORE = Block.box(4, 4, 4, 12, 12, 12);
    private static final Map<Direction, VoxelShape> ARMS = Map.of(
            Direction.NORTH, Block.box(4, 4, 0, 12, 12, 4), Direction.SOUTH, Block.box(4, 4, 12, 12, 12, 16),
            Direction.WEST, Block.box(0, 4, 4, 4, 12, 12), Direction.EAST, Block.box(12, 4, 4, 16, 12, 12),
            Direction.DOWN, Block.box(4, 0, 4, 12, 4, 12), Direction.UP, Block.box(4, 12, 4, 12, 16, 12));

    public final PipeType type;

    public PipeBlock(PipeType type, Properties props) {
        super(props);
        this.type = type;
        BlockState s = this.stateDefinition.any().setValue(OUTPUT, Direction.NORTH);
        for (BooleanProperty p : SIDES.values()) s = s.setValue(p, false);
        this.registerDefaultState(s);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(p -> new PipeBlock(type, p)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { SIDES.values().forEach(b::add); b.add(OUTPUT); }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        VoxelShape shape = CORE;
        for (var e : SIDES.entrySet()) if (s.getValue(e.getValue())) shape = Shapes.or(shape, ARMS.get(e.getKey()));
        return shape;
    }

    /** Whether this pipe joins up with whatever is on that side. */
    public boolean connectsTo(BlockGetter level, BlockPos pos, Direction dir) {
        BlockPos n = pos.relative(dir);
        BlockState ns = level.getBlockState(n);
        if (ns.getBlock() instanceof PipeBlock other) return other.type.fluid == type.fluid;
        if (type.fluid) {
            if (ns.getBlock() instanceof PumpBlock || ns.getBlock() instanceof TankBlock) return true;
            return level instanceof Level l && FluidStorage.SIDED.find(l, n, dir.getOpposite()) != null;
        }
        if (ns.getBlock() instanceof EnderTerminalBlock) return true;
        if (ns.getBlock() instanceof EngineBlock) return false;
        if (level instanceof Level l) return ItemStorage.SIDED.find(l, n, dir.getOpposite()) != null;
        return level.getBlockEntity(n) instanceof net.minecraft.world.Container;
    }

    private BlockState connections(BlockGetter level, BlockPos pos, BlockState s) {
        for (var e : SIDES.entrySet()) s = s.setValue(e.getValue(), connectsTo(level, pos, e.getKey()));
        return s;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return connections(ctx.getLevel(), ctx.getClickedPos(), this.defaultBlockState().setValue(OUTPUT, ctx.getNearestLookingDirection()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos nPos, BlockState nState, RandomSource r) {
        return state.setValue(SIDES.get(dir), connectsTo(level, pos, dir));
    }

    /** Inventories don't tell their neighbours when they appear, so refresh on any neighbour change too. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable net.minecraft.world.level.redstone.Orientation o, boolean moved) {
        BlockState now = connections(level, pos, state);
        if (now != state) level.setBlock(pos, now, Block.UPDATE_CLIENTS);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof WrenchItem && type == PipeType.IRON) {       // turn the iron pipe to its next open side
            Direction cur = state.getValue(OUTPUT);
            for (int i = 1; i <= 6; i++) {
                Direction d = Direction.from3DDataValue((cur.get3DDataValue() + i) % 6);
                if (state.getValue(SIDES.get(d))) {
                    if (!level.isClientSide()) level.setBlock(pos, state.setValue(OUTPUT, d), Block.UPDATE_ALL);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (type != PipeType.DIAMOND) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PipeBlockEntity be) player.openMenu(be);
        return InteractionResult.SUCCESS;
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PipeBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> t) {
        return level.isClientSide() ? createTickerHelper(t, RyanPipes.PIPE_ENTITY, PipeBlockEntity::clientTick)
                                    : createTickerHelper(t, RyanPipes.PIPE_ENTITY, PipeBlockEntity::serverTick);
    }
}

package com.ryancraft.pipes;

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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
 * The little engines. A redstone engine runs while powered and gives a slow, weak pulse; a Stirling engine burns coal or
 * any furnace fuel (no redstone needed) for faster, stronger pulses, and glows like a lit furnace while it burns. Each pulse goes to what the engine faces: a wooden
 * pipe pulls items out of its inventory, a pump draws up a bucket of fluid. Turn it with a wrench.
 */
public class EngineBlock extends BaseEntityBlock {
    public enum Kind { REDSTONE, STIRLING }
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public final Kind kind;

    public EngineBlock(Kind kind, Properties p) {
        super(p); this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(RUNNING, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(p -> new EngineBlock(kind, p)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, RUNNING); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    static boolean powerable(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return (s.getBlock() instanceof PipeBlock p && p.type == PipeType.WOODEN) || s.getBlock() instanceof PumpBlock || s.getBlock() instanceof HandPumpBlock || s.getBlock() instanceof EngineBlock;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        for (Direction d : Direction.values()) if (powerable(ctx.getLevel(), ctx.getClickedPos().relative(d))) return defaultBlockState().setValue(FACING, d);
        return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof WrenchItem) {
            Direction cur = state.getValue(FACING);
            Direction next = Direction.from3DDataValue((cur.get3DDataValue() + 1) % 6);
            for (int i = 1; i <= 6; i++) {
                Direction d = Direction.from3DDataValue((cur.get3DDataValue() + i) % 6);
                if (powerable(level, pos.relative(d))) { next = d; break; }
            }
            if (!level.isClientSide()) level.setBlock(pos, state.setValue(FACING, next), Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        }
        if (kind == Kind.STIRLING && level.getBlockEntity(pos) instanceof EngineBlockEntity be && be.isFuel(stack)) {
            if (!level.isClientSide()) be.addFuel(stack, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EngineBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> t) {
        return level.isClientSide() ? createTickerHelper(t, RyanPipes.ENGINE_ENTITY, EngineBlockEntity::clientTick)
                                    : createTickerHelper(t, RyanPipes.ENGINE_ENTITY, EngineBlockEntity::serverTick);
    }
}

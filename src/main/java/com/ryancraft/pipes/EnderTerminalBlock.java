package com.ryancraft.pipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The Ender Terminal: hook it to a pipe network and it sees every chest, barrel and pot along the pipes. Ask for an item
 * and it's pulled out and sent down the pipes to you; put items in and they travel off to wherever there's room.
 */
public class EnderTerminalBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public EnderTerminalBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(EnderTerminalBlock::new); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) { return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EnderTerminalBlockEntity(pos, state); }

    /** Development worlds only: a redstone pulse asks for 8 of the first thing on the network (to test routing without the screen). */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable net.minecraft.world.level.redstone.Orientation o, boolean moved) {
        if (level.isClientSide() || !net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment() || !level.hasNeighborSignal(pos)) return;
        if (level.getBlockEntity(pos) instanceof EnderTerminalBlockEntity be) {
            var all = be.contents(level);
            all.keySet().stream().findFirst().ifPresent(v -> be.request(level, v, 8));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EnderTerminalBlockEntity be) player.openMenu(be);
        return InteractionResult.SUCCESS;
    }
}

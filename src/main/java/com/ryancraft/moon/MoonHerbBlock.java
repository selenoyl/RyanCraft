package com.ryancraft.moon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Moon herb: a low silvery herb rooted in moon dust (or moonstone, or Earth's soil). Moon rabbits love it. */
public class MoonHerbBlock extends VegetationBlock {
    public static final MapCodec<MoonHerbBlock> CODEC = simpleCodec(MoonHerbBlock::new);
    private static final VoxelShape SHAPE = net.minecraft.world.level.block.Block.box(3, 0, 3, 13, 9, 13);
    public MoonHerbBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends VegetationBlock> codec() { return CODEC; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(MoonBlocks.MOON_DUST) || state.is(MoonBlocks.MOONSTONE) || super.mayPlaceOn(state, level, pos);
    }
}

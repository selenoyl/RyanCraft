package com.ryancraft.sky;

import com.mojang.serialization.MapCodec;
import com.ryancraft.entity.RyanEntities;
import com.ryancraft.entity.SkyRoc;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A roc egg: it cracks a little at a time and hatches a roc chick. Found in roc nests; carry one home with silk touch. */
public class RocEggBlock extends Block {
    public static final MapCodec<RocEggBlock> CODEC = simpleCodec(RocEggBlock::new);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 10, 12);

    public RocEggBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(HATCH, 0)); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(HATCH); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) != 0) return;
        int h = state.getValue(HATCH);
        if (h < 2) {
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7f, 0.7f + random.nextFloat() * 0.2f);
            level.setBlock(pos, state.setValue(HATCH, h + 1), Block.UPDATE_CLIENTS);
            return;
        }
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7f, 0.6f);
        level.removeBlock(pos, false);
        SkyRoc chick = RyanEntities.SKY_ROC.create(level, EntitySpawnReason.BREEDING);
        if (chick != null) {
            chick.setAge(-24000);
            chick.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0f);
            level.addFreshEntity(chick);
        }
    }
}

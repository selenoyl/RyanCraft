package com.ryancraft.bag;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** A cage one small mob lives in. Hollow: a floor, four barred walls and a roof, with room inside to walk around. */
public class CageBlock extends BaseEntityBlock {
    public final Baggable.CageKind kind;
    /** Interior, in pixels: walls inset from the block edge, floor height, ceiling height, and a perch height (birds). */
    public final double inset, floor, ceiling, perch;
    private final VoxelShape outline, collision;

    public CageBlock(Baggable.CageKind kind, Properties props) {
        super(props);
        this.kind = kind;
        if (kind == Baggable.CageKind.BIRD) {
            inset = 2; floor = 1; ceiling = 15.5; perch = 8.5;
            outline = Block.box(1.5, 0, 1.5, 14.5, 16, 14.5);
            collision = Shapes.or(Block.box(2, 0, 2, 14, 1, 14), Block.box(2, 15.5, 2, 14, 16, 14),
                    Block.box(1.5, 0, 1.5, 14.5, 16, 2), Block.box(1.5, 0, 14, 14.5, 16, 14.5),
                    Block.box(1.5, 0, 2, 2, 16, 14), Block.box(14, 0, 2, 14.5, 16, 14));
        } else {
            inset = 1; floor = 2; ceiling = 12; perch = 0;
            outline = Block.box(0, 0, 0, 16, 13, 16);
            collision = Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 12, 0, 16, 13, 16),
                    Block.box(0, 2, 0, 16, 12, 1), Block.box(0, 2, 15, 16, 12, 16),
                    Block.box(0, 2, 1, 1, 12, 15), Block.box(15, 2, 1, 16, 12, 15));
        }
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new CageBlock(kind, p));
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return outline; }
    @Override protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return collision; }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CageBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Cages.CAGE_ENTITY, CageBlockEntity::serverTick);
    }
}

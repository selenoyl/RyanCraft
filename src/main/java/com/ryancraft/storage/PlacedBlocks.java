package com.ryancraft.storage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bowls and buckets set down on a surface (sneak-right-click), showing what's in them. Right-click to pick up. */
public final class PlacedBlocks {
    public enum BowlContents implements StringRepresentable {
        EMPTY(Items.BOWL), MUSHROOM_STEW(Items.MUSHROOM_STEW), BEETROOT_SOUP(Items.BEETROOT_SOUP), RABBIT_STEW(Items.RABBIT_STEW);
        public final Item item;
        BowlContents(Item item) { this.item = item; }
        @Override public String getSerializedName() { return name().toLowerCase(); }
        public static BowlContents of(ItemStack s) {
            for (BowlContents c : values()) if (s.is(c.item)) return c;
            return null;
        }
    }

    public enum BucketContents implements StringRepresentable {
        EMPTY(Items.BUCKET), WATER(Items.WATER_BUCKET), LAVA(Items.LAVA_BUCKET), MILK(Items.MILK_BUCKET), POWDER_SNOW(Items.POWDER_SNOW_BUCKET);
        public final Item item;
        BucketContents(Item item) { this.item = item; }
        @Override public String getSerializedName() { return name().toLowerCase(); }
        public static BucketContents of(ItemStack s) {
            for (BucketContents c : values()) if (s.is(c.item)) return c;
            return null;
        }
    }

    /** Shared behaviour: sits on a sturdy surface, picks back up with an empty hand. */
    abstract static class Placed extends Block {
        private final VoxelShape shape;
        Placed(Properties p, VoxelShape shape) { super(p); this.shape = shape; }
        abstract ItemStack item(BlockState state);
        @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return shape; }
        @Override protected boolean canSurvive(BlockState s, LevelReader l, BlockPos p) { return Block.canSupportCenter(l, p.below(), Direction.UP); }
        @Override protected BlockState updateShape(BlockState s, LevelReader l, ScheduledTickAccess t, BlockPos p, Direction d, BlockPos np, BlockState ns, RandomSource r) {
            return d == Direction.DOWN && !s.canSurvive(l, p) ? Blocks.AIR.defaultBlockState() : super.updateShape(s, l, t, p, d, np, ns, r);
        }
        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!level.isClientSide()) {
                level.removeBlock(pos, false);
                if (!player.getInventory().add(item(state))) player.drop(item(state), false);
            }
            return InteractionResult.SUCCESS;
        }
        @Override protected ItemStack getCloneItemStack(LevelReader l, BlockPos p, BlockState s, boolean data) { return item(s); }
    }

    public static class PlacedBowl extends Placed {
        public static final EnumProperty<BowlContents> CONTENTS = EnumProperty.create("contents", BowlContents.class);
        public PlacedBowl(Properties p) { super(p, Block.box(4, 0, 4, 12, 3, 12)); registerDefaultState(stateDefinition.any().setValue(CONTENTS, BowlContents.EMPTY)); }
        @Override protected MapCodec<? extends Block> codec() { return simpleCodec(PlacedBowl::new); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(CONTENTS); }
        @Override ItemStack item(BlockState s) { return new ItemStack(s.getValue(CONTENTS).item); }

        /** A placed stew can be eaten straight from the bowl (sneak to pick it up instead). */
        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            BowlContents c = state.getValue(CONTENTS);
            if (c != BowlContents.EMPTY && !player.isSecondaryUseActive() && player.canEat(false)) {
                if (!level.isClientSide()) {
                    ItemStack meal = new ItemStack(c.item);
                    var food = meal.get(net.minecraft.core.component.DataComponents.FOOD);
                    if (food != null) player.getFoodData().eat(food.nutrition(), food.saturation());
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                    level.setBlock(pos, state.setValue(CONTENTS, BowlContents.EMPTY), 3);
                }
                return InteractionResult.SUCCESS;
            }
            return super.useWithoutItem(state, level, pos, player, hit);
        }
    }

    public static class PlacedBucket extends Placed {
        public static final EnumProperty<BucketContents> CONTENTS = EnumProperty.create("contents", BucketContents.class);
        public PlacedBucket(Properties p) { super(p, Block.box(4, 0, 4, 12, 8, 12)); registerDefaultState(stateDefinition.any().setValue(CONTENTS, BucketContents.EMPTY)); }
        @Override protected MapCodec<? extends Block> codec() { return simpleCodec(PlacedBucket::new); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(CONTENTS); }
        @Override ItemStack item(BlockState s) { return new ItemStack(s.getValue(CONTENTS).item); }
    }

    private PlacedBlocks() {}
}

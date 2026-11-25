package com.ryancraft.moon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The moon cake, set down and eaten a slice at a time like vanilla's cake: golden sponge under pale moonlit icing,
 * studded with moon herb. (Candles don't go on it: vanilla would turn it into its own candle cake.)
 */
public class MoonCakeBlock extends CakeBlock {
    public static final MapCodec<CakeBlock> CODEC = simpleCodec(MoonCakeBlock::new);
    public MoonCakeBlock(Properties p) { super(p); }
    @Override public MapCodec<CakeBlock> codec() { return CODEC; }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ItemTags.CANDLES)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}

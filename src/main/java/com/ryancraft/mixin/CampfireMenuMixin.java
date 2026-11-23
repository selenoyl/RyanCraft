package com.ryancraft.mixin;

import com.ryancraft.food.CampfireFuel;
import com.ryancraft.food.FuelHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Right-clicking a campfire (with nothing it would cook) opens its fuel menu, like a furnace's. Lighting and dousing
 * tools and buckets keep their vanilla uses.
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireMenuMixin extends BaseEntityBlock {
    protected CampfireMenuMixin(Properties p) { super(p); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack held = player.getMainHandItem();
        if (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE) || held.is(ItemTags.SHOVELS) || held.is(Items.WATER_BUCKET)
                || held.is(Items.BUCKET) || held.is(com.ryancraft.food.Cooking.COOKING_POT_ITEM)) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FuelHolder h) CampfireFuel.openMenu(player, h, pos);
        return InteractionResult.SUCCESS;
    }
}

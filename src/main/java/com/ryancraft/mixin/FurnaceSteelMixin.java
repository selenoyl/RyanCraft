package com.ryancraft.mixin;

import com.ryancraft.metal.Steel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Steel: a blast furnace stoked by engines turns iron ingots into steel (see {@link Steel}). */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceSteelMixin implements Steel.Stoked, Steel.FurnaceTimers {
    @Shadow int litTimeRemaining;
    @Shadow int litTotalTime;
    @Shadow int cookingTimer;
    @Shadow int cookingTotalTime;
    @Shadow protected NonNullList<ItemStack> items;
    @Unique private int ryancraft$heatTicks;

    @Override public int ryancraft$heat() { return ryancraft$heatTicks; }
    @Override public void ryancraft$setHeat(int heat) { ryancraft$heatTicks = heat; }
    @Override public int ryancraft$lit() { return litTimeRemaining; }
    @Override public void ryancraft$setLit(int v) { litTimeRemaining = v; }
    @Override public void ryancraft$setLitTotal(int v) { litTotalTime = v; }
    @Override public int ryancraft$cook() { return cookingTimer; }
    @Override public void ryancraft$setCook(int v) { cookingTimer = v; }
    @Override public void ryancraft$setCookTotal(int v) { cookingTotalTime = v; }
    @Override public NonNullList<ItemStack> ryancraft$items() { return items; }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void ryancraft$steel(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity be, CallbackInfo ci) {
        if (be instanceof BlastFurnaceBlockEntity && Steel.tick(level, pos, state, be)) ci.cancel();
    }
}

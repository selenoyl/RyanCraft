package com.ryancraft.mixin;

import com.ryancraft.food.CampfireFuel;
import com.ryancraft.food.FuelHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Campfires burn fuel from a fuel slot and go out when it's gone (food/CampfireFuel). */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireFuelMixin implements FuelHolder {
    @Unique private final NonNullList<ItemStack> ryancraft$fuel = NonNullList.withSize(1, ItemStack.EMPTY);
    @Unique private int ryancraft$burn = -1, ryancraft$total = 0;

    @Override public NonNullList<ItemStack> ryancraft$fuel() { return ryancraft$fuel; }
    @Override public int ryancraft$burn() { return ryancraft$burn; }
    @Override public void ryancraft$setBurn(int b) { ryancraft$burn = b; }
    @Override public int ryancraft$total() { return ryancraft$total; }
    @Override public void ryancraft$setTotal(int t) { ryancraft$total = t; }
    @Override public void ryancraft$changed() { ((CampfireBlockEntity) (Object) this).setChanged(); }

    @Inject(method = "cookTick", at = @At("HEAD"), cancellable = true)
    private static void ryancraft$burnFuel(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity be,
                                           RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> check, CallbackInfo ci) {
        if (CampfireFuel.burn(level, (FuelHolder) be)) return;
        level.setBlock(pos, state.setValue(CampfireBlock.LIT, false), 3);
        CampfireFuel.goOut(level, pos);
        ci.cancel();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void ryancraft$saveFuel(ValueOutput out, CallbackInfo ci) {
        ContainerHelper.saveAllItems(out.child("RyanFuel"), ryancraft$fuel);
        out.putInt("RyanBurn", ryancraft$burn); out.putInt("RyanBurnTotal", ryancraft$total);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void ryancraft$loadFuel(ValueInput in, CallbackInfo ci) {
        ryancraft$fuel.set(0, ItemStack.EMPTY);
        in.child("RyanFuel").ifPresent(c -> ContainerHelper.loadAllItems(c, ryancraft$fuel));
        ryancraft$burn = in.getIntOr("RyanBurn", -1); ryancraft$total = in.getIntOr("RyanBurnTotal", 0);
    }

    @Inject(method = "preRemoveSideEffects", at = @At("TAIL"))
    private void ryancraft$dropFuel(BlockPos pos, BlockState state, CallbackInfo ci) {
        var be = (CampfireBlockEntity) (Object) this;
        if (be.getLevel() != null && !ryancraft$fuel.get(0).isEmpty())
            Containers.dropItemStack(be.getLevel(), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, ryancraft$fuel.get(0));
        ryancraft$fuel.set(0, ItemStack.EMPTY);
    }
}

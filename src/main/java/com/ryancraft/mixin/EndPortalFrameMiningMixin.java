package com.ryancraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** End portal frames can be mined (as hard as obsidian); with Silk Touch on a diamond pickaxe they drop themselves. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class EndPortalFrameMiningMixin {
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void ryancraft$mineableFrame(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (((BlockBehaviour.BlockStateBase) (Object) this).is(Blocks.END_PORTAL_FRAME)) cir.setReturnValue(50.0f);
    }
}

package com.ryancraft.mixin;

import com.ryancraft.metal.Silver;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Silver blocks tarnish on random ticks; none of the vanilla block classes they use override randomTick. */
@Mixin(BlockBehaviour.class)
public abstract class SilverTarnishMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void ryancraft$tarnish(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (Silver.NEXT.containsKey(state.getBlock())) Silver.tarnish(state, level, pos, random);
    }
}

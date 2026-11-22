package com.ryancraft.mixin;

import com.ryancraft.season.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Seasons: crops don't grow through a temperate winter unless they're under a roof (a greenhouse). */
@Mixin(CropBlock.class)
public abstract class CropSeasonMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void ryancraft$winter(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (Seasons.cropsSleep(level, level.getBiome(pos).value(), !level.canSeeSky(pos.above()))) ci.cancel();
    }
}

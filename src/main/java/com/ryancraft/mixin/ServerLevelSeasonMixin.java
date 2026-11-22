package com.ryancraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Seasons: once it's warm enough to rain again, winter's snow melts a layer at a time and pond ice thaws. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSeasonMixin {
    @Inject(method = "tickPrecipitation", at = @At("HEAD"))
    private void ryancraft$melt(BlockPos pos, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        Biome biome = level.getBiome(top).value();
        if (!com.ryancraft.season.Seasons.temperate(biome) || !biome.warmEnoughToRain(top, level.getSeaLevel())) return;
        for (BlockPos p : new BlockPos[]{top, top.below()}) {
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.SNOW)) {
                int layers = s.getValue(SnowLayerBlock.LAYERS);
                if (layers > 1) level.setBlockAndUpdate(p, s.setValue(SnowLayerBlock.LAYERS, layers - 1));
                else level.removeBlock(p, false);
                return;
            }
            if (s.is(Blocks.ICE)) { level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState()); return; }
        }
    }
}

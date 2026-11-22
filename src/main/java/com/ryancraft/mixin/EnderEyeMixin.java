package com.ryancraft.mixin;

import com.ryancraft.pipes.Strongholds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Eyes of Ender skip strongholds whose portal was broken up for an Ender Terminal. */
@Mixin(EnderEyeItem.class)
public abstract class EnderEyeMixin {
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"))
    private BlockPos ryancraft$skipSacrificed(ServerLevel level, TagKey<Structure> tag, BlockPos from, int radius, boolean skipKnown) {
        BlockPos living = Strongholds.nearestLiving(level, from);
        return living != null ? living : level.findNearestMapStructure(tag, from, radius, skipKnown);
    }
}

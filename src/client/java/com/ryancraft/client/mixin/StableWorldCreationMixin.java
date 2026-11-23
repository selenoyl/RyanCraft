package com.ryancraft.client.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Creating a world doesn't ask about "experimental settings" because of the moon (see StableWorldgenMixin). */
@Mixin(WorldOpenFlows.class)
public abstract class StableWorldCreationMixin {
    @ModifyVariable(method = "confirmWorldCreation", at = @At("HEAD"), argsOnly = true)
    private static Lifecycle ryancraft$stable(Lifecycle lifecycle) { return Lifecycle.stable(); }
}

package com.ryancraft.mixin;

import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lets horse variants (the pegasus) pick their own coats. */
@Mixin(Horse.class)
public interface HorseVariantInvoker {
    @Invoker("setVariantAndMarkings") void ryancraft$setVariantAndMarkings(Variant variant, Markings markings);
}

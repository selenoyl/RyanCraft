package com.ryancraft.trees;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** {"species": "white_oak"} */
public record SpeciesTreeConfig(String species) implements FeatureConfiguration {
    public static final Codec<SpeciesTreeConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("species").forGetter(SpeciesTreeConfig::species)).apply(i, SpeciesTreeConfig::new));
}

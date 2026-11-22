package com.ryancraft.trees;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** {"species": "white_oak"} or {"species": "sugar_maple", "autumn": true} for its autumn colours. */
public record SpeciesTreeConfig(String species, boolean autumn) implements FeatureConfiguration {
    public static final Codec<SpeciesTreeConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("species").forGetter(SpeciesTreeConfig::species),
            Codec.BOOL.optionalFieldOf("autumn", false).forGetter(SpeciesTreeConfig::autumn)).apply(i, SpeciesTreeConfig::new));
}

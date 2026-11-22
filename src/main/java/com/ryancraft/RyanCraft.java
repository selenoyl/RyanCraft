package com.ryancraft;

import com.ryancraft.entity.RyanEntities;
import com.ryancraft.worldgen.RyanBiomes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RyanCraft implements ModInitializer {
    public static final String MOD_ID = "ryancraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        RyanConfig.get();
        RyanSounds.init();
        RyanItems.init();
        RyanEntities.init();
        RyanBiomes.init();
        com.ryancraft.trees.RyanWood.init();
        com.ryancraft.bag.Cages.init();
        com.ryancraft.storage.RyanStorage.init();
        com.ryancraft.flora.RyanFlora.init();
        com.ryancraft.fish.FishSize.init();
        com.ryancraft.pipes.RyanPipes.init();
        com.ryancraft.season.Seasons.init();
        com.ryancraft.thirst.Thirst.init();
        com.ryancraft.metal.Steel.init();
        com.ryancraft.weather.DustStorms.init();
        com.ryancraft.pipes.Strongholds.init();
        LOGGER.info("RyanCraft loaded. Boars are in the woods.");
    }
}

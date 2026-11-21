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
        LOGGER.info("RyanCraft loaded. Boars are in the woods.");
    }
}

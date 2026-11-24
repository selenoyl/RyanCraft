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
        com.ryancraft.worldgen.TemperateSurroundings.init();
        com.ryancraft.worldgen.DevBiomeTp.init();
        com.ryancraft.worldgen.MudlingCampFeature.init();
        RyanBiomes.init();
        com.ryancraft.trees.RyanWood.init();
        com.ryancraft.trees.RyanBoats.init();
        com.ryancraft.deep.DeepCaves.init();
        com.ryancraft.bag.Cages.init();
        com.ryancraft.storage.RyanStorage.init();
        com.ryancraft.storage.RyanChests.init();
        com.ryancraft.combat.Cannons.init();
        com.ryancraft.trade.Currency.init();
        com.ryancraft.food.CampfireFuel.init();
        com.ryancraft.food.Cooking.init();
        com.ryancraft.food.Crops.init();
        com.ryancraft.food.Dishes.init();
        com.ryancraft.flora.RyanFlora.init();
        com.ryancraft.fish.FishSize.init();
        com.ryancraft.pipes.RyanPipes.init();
        com.ryancraft.season.Seasons.init();
        com.ryancraft.thirst.Thirst.init();
        com.ryancraft.metal.Steel.init();
        com.ryancraft.metal.Metals.init();
        com.ryancraft.metal.Silver.init();
        com.ryancraft.metal.LeadedGlass.init();
        com.ryancraft.moon.MoonBlocks.init();
        com.ryancraft.deco.GemBlocks.init();
        com.ryancraft.deco.ConcreteShapes.init();
        com.ryancraft.deco.Gems.init();
        com.ryancraft.flora.ShelfMushrooms.init();
        com.ryancraft.sky.Skylands.init();
        com.ryancraft.weather.DustStorms.init();
        com.ryancraft.pipes.Strongholds.init();
        LOGGER.info("RyanCraft loaded. Boars are in the woods.");
    }
}

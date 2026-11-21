package com.ryancraft.worldgen;

import com.ryancraft.RyanCraft;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

/**
 * Only called when TerraBlender is installed (fabric.mod.json "terrablender" entrypoint).
 */
public class RyanTerraBlender implements TerraBlenderApi {
    @Override
    public void onTerraBlenderInitialized() {
        Regions.register(new BetaRegion(RyanCraft.id("beta_biomes"), 4));
    }
}

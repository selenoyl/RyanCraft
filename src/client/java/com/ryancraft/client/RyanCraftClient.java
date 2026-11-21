package com.ryancraft.client;

import com.ryancraft.client.model.BoarModel;
import com.ryancraft.client.model.DeerModel;
import com.ryancraft.client.model.DuckModel;
import com.ryancraft.client.render.RyanAnimalRenderer;
import com.ryancraft.entity.RyanEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RyanCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(BoarModel.LAYER, BoarModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DeerModel.LAYER, DeerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DuckModel.LAYER, DuckModel::createBodyLayer);

        EntityRendererRegistry.register(RyanEntities.BOAR,
                ctx -> new RyanAnimalRenderer<>(ctx, new BoarModel(ctx.bakeLayer(BoarModel.LAYER)), 0.7f, "boar"));
        EntityRendererRegistry.register(RyanEntities.DEER,
                ctx -> new RyanAnimalRenderer<>(ctx, new DeerModel(ctx.bakeLayer(DeerModel.LAYER)), 0.6f, "deer"));
        EntityRendererRegistry.register(RyanEntities.DUCK,
                ctx -> new RyanAnimalRenderer<>(ctx, new DuckModel(ctx.bakeLayer(DuckModel.LAYER)), 0.3f, "duck"));
    }
}

package com.ryancraft.client;

import com.ryancraft.client.model.BoarModel;
import com.ryancraft.client.model.DeerModel;
import com.ryancraft.client.model.DuckModel;
import com.ryancraft.client.model.TurkeyModel;
import com.ryancraft.client.ambience.AmbienceEngine;
import com.ryancraft.client.classic.ClassicPack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.ryancraft.client.render.RyanAnimalRenderer;
import com.ryancraft.client.render.CritterRenderer;
import com.ryancraft.client.model.CritterModel;
import com.ryancraft.entity.RyanEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RyanCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for (com.ryancraft.trees.WoodSet w : com.ryancraft.trees.RyanWood.sets())
            net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlocks(net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT, w.door(), w.trapdoor(), w.sapling());
        EntityModelLayerRegistry.registerModelLayer(BoarModel.LAYER, BoarModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DeerModel.LAYER, DeerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DuckModel.LAYER, DuckModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(TurkeyModel.LAYER, TurkeyModel::createBodyLayer);

        EntityRendererRegistry.register(RyanEntities.BOAR,
                ctx -> new RyanAnimalRenderer<>(ctx, new BoarModel(ctx.bakeLayer(BoarModel.LAYER)), 0.7f, "boar"));
        EntityRendererRegistry.register(RyanEntities.DEER, com.ryancraft.client.render.DeerRenderer::new);
        EntityRendererRegistry.register(RyanEntities.DUCK,
                ctx -> new RyanAnimalRenderer<>(ctx, new DuckModel(ctx.bakeLayer(DuckModel.LAYER)), 0.3f, "duck"));
        EntityRendererRegistry.register(RyanEntities.TURKEY,
                ctx -> new RyanAnimalRenderer<>(ctx, new TurkeyModel(ctx.bakeLayer(TurkeyModel.LAYER)), 0.5f, "turkey"));

        for (String m : new String[]{"black_bear", "raccoon", "skunk", "mink", "opossum", "beaver", "squirrel", "songbird",
                "bluegill", "crappie", "largemouth_bass", "channel_catfish", "chipmunk", "coyote"}) {
            EntityModelLayerRegistry.registerModelLayer(CritterModel.layer(m), () -> CritterModel.createLayer(m));
        }
        EntityRendererRegistry.register(RyanEntities.BLACK_BEAR, c -> new CritterRenderer<>(c, "black_bear", 0.9f, 1.0f, false, "black", "cinnamon"));
        EntityRendererRegistry.register(RyanEntities.RACCOON, c -> new CritterRenderer<>(c, "raccoon", 0.4f, 1.0f, false, "raccoon"));
        EntityRendererRegistry.register(RyanEntities.SKUNK, c -> new CritterRenderer<>(c, "skunk", 0.4f, 1.0f, false, "skunk"));
        EntityRendererRegistry.register(RyanEntities.MINK, c -> new CritterRenderer<>(c, "mink", 0.3f, 1.0f, false, "mink"));
        EntityRendererRegistry.register(RyanEntities.OPOSSUM, c -> new CritterRenderer<>(c, "opossum", 0.4f, 1.0f, false, "opossum"));
        EntityRendererRegistry.register(RyanEntities.BEAVER, c -> new CritterRenderer<>(c, "beaver", 0.5f, 1.0f, false, "beaver"));
        EntityRendererRegistry.register(RyanEntities.SQUIRREL, c -> new CritterRenderer<>(c, "squirrel", 0.3f, 1.0f, false, "gray_squirrel", "fox_squirrel"));
        EntityRendererRegistry.register(RyanEntities.SONGBIRD, c -> new CritterRenderer<>(c, "songbird", 0.25f, 0.8f, false,
                "cardinal", "goldfinch", "robin", "blue_jay", "eastern_bluebird", "mourning_dove"));
        EntityRendererRegistry.register(RyanEntities.COYOTE, c -> new CritterRenderer<>(c, "coyote", 0.5f, 1.0f, false, "coyote"));
        EntityRendererRegistry.register(RyanEntities.CHIPMUNK, c -> new CritterRenderer<>(c, "chipmunk", 0.25f, 1.0f, false, "chipmunk"));
        EntityRendererRegistry.register(RyanEntities.BLUEGILL, c -> new CritterRenderer<>(c, "bluegill", 0.2f, 1.0f, true, "bluegill"));
        EntityRendererRegistry.register(RyanEntities.CRAPPIE, c -> new CritterRenderer<>(c, "crappie", 0.2f, 1.0f, true, "crappie"));
        EntityRendererRegistry.register(RyanEntities.LARGEMOUTH_BASS, c -> new CritterRenderer<>(c, "largemouth_bass", 0.25f, 1.0f, true, "largemouth_bass"));
        EntityRendererRegistry.register(RyanEntities.CHANNEL_CATFISH, c -> new CritterRenderer<>(c, "channel_catfish", 0.3f, 1.0f, true, "channel_catfish"));

        EntityModelLayerRegistry.registerModelLayer(com.ryancraft.client.model.DeerMountModel.LAYER, com.ryancraft.client.model.DeerMountModel::createBodyLayer);
        EntityRendererRegistry.register(RyanEntities.DEER_MOUNT, com.ryancraft.client.render.DeerMountRenderer::new);

        // Skeleton variants (cracked skulls, missing ribs, dark bones), rebuilt whenever resources reload
        EntityRendererRegistry.register(net.minecraft.world.entity.EntityType.SKELETON, com.ryancraft.client.skeleton.VariantSkeletonRenderer::new);
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
                .registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                    @Override public net.minecraft.resources.Identifier getFabricId() { return com.ryancraft.RyanCraft.id("skeleton_variants"); }
                    @Override public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager) {
                        com.ryancraft.client.skeleton.SkeletonVariants.invalidate();
                    }
                });

        AmbienceEngine ambience = new AmbienceEngine();
        ClientTickEvents.END_CLIENT_TICK.register(ambience::tick);
        ClientLifecycleEvents.CLIENT_STARTED.register(ClassicPack::start);
    }
}

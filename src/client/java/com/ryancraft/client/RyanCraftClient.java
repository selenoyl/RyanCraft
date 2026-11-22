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
        net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlocks(net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT,
                com.ryancraft.bag.Cages.BIRD_CAGE, com.ryancraft.bag.Cages.CHIPMUNK_CAGE);
        net.minecraft.client.gui.screens.MenuScreens.register(com.ryancraft.storage.RyanStorage.POT_MENU, com.ryancraft.client.screen.PotScreen::new);
        // Water in a placed bucket takes the biome's water colour
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.BLOCK.register((state, level, pos, tint) ->
                level != null && pos != null ? net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(level, pos) : 0x3F76E4,
                com.ryancraft.storage.RyanStorage.PLACED_BUCKET);
        // Mob bags list who's inside
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
            com.ryancraft.bag.MobBag bag = com.ryancraft.bag.MobBags.get(stack);
            if (bag == null || bag.count() == 0) return;
            lines.add(1, net.minecraft.network.chat.Component.translatable("tooltip.ryancraft.mob_bag", bag.count(), bag.capacity())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            for (int i = 0; i < bag.names().size(); i++) {
                lines.add(2 + i, net.minecraft.network.chat.Component.literal("  ").append(bag.names().get(i)).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
            }
        });
        for (com.ryancraft.trees.WoodSet w : com.ryancraft.trees.RyanWood.sets())
            net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlocks(net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT, w.door(), w.trapdoor(), w.sapling(), w.pottedSapling());
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
                "bluegill", "crappie", "largemouth_bass", "channel_catfish", "chipmunk", "coyote",
                "elk", "moose", "pronghorn", "grizzly_bear", "bobcat", "crow", "black_tailed_deer", "bison", "cougar", "river_otter", "groundhog", "porcupine", "bald_eagle", "golden_eagle", "red_tailed_hawk", "turkey_vulture", "great_horned_owl", "great_blue_heron", "canada_goose", "orca", "blue_whale", "shark", "manta_ray", "jellyfish", "butterfly", "hummingbird", "pileated_woodpecker", "bobwhite", "box_turtle", "rattlesnake", "bullfrog", "alligator"}) {
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
        EntityRendererRegistry.register(RyanEntities.ELK, c -> new CritterRenderer<>(c, "elk", 1.0f, 1.0f, false, "elk", "elk_bull", "elk_bull_dark", "elk_bull_chocolate").plainBabies());
        EntityRendererRegistry.register(RyanEntities.MOOSE, c -> new CritterRenderer<>(c, "moose", 1.1f, 1.0f, false, "moose", "moose_bull").plainBabies());
        EntityRendererRegistry.register(RyanEntities.PRONGHORN, c -> new CritterRenderer<>(c, "pronghorn", 0.6f, 1.0f, false, "pronghorn_fawn", "pronghorn").plainBabies());
        EntityRendererRegistry.register(RyanEntities.GRIZZLY_BEAR, c -> new CritterRenderer<>(c, "grizzly_bear", 1.1f, 1.0f, false, "grizzly_bear"));
        EntityRendererRegistry.register(RyanEntities.BOBCAT, c -> new CritterRenderer<>(c, "bobcat", 0.4f, 1.0f, false, "bobcat"));
        EntityRendererRegistry.register(RyanEntities.BLACK_TAILED_DEER, c -> new CritterRenderer<>(c, "black_tailed_deer", 0.6f, 1.0f, false, "black_tailed_deer", "black_tailed_deer_buck", "black_tailed_deer_buck_dark", "black_tailed_deer_buck_chocolate").plainBabies());
        EntityRendererRegistry.register(RyanEntities.BISON, c -> new CritterRenderer<>(c, "bison", 1.1f, 1.0f, false, "bison"));
        EntityRendererRegistry.register(RyanEntities.COUGAR, c -> new CritterRenderer<>(c, "cougar", 0.6f, 1.0f, false, "cougar"));
        EntityRendererRegistry.register(RyanEntities.RIVER_OTTER, c -> new CritterRenderer<>(c, "river_otter", 0.35f, 1.0f, false, "river_otter"));
        EntityRendererRegistry.register(RyanEntities.GROUNDHOG, c -> new CritterRenderer<>(c, "groundhog", 0.4f, 1.0f, false, "groundhog"));
        EntityRendererRegistry.register(RyanEntities.PORCUPINE, c -> new CritterRenderer<>(c, "porcupine", 0.4f, 1.0f, false, "porcupine"));
        EntityRendererRegistry.register(RyanEntities.BALD_EAGLE, c -> new CritterRenderer<>(c, "bald_eagle", 0.5f, 1.0f, false, "bald_eagle"));
        EntityRendererRegistry.register(RyanEntities.GOLDEN_EAGLE, c -> new CritterRenderer<>(c, "golden_eagle", 0.5f, 1.0f, false, "golden_eagle"));
        EntityRendererRegistry.register(RyanEntities.RED_TAILED_HAWK, c -> new CritterRenderer<>(c, "red_tailed_hawk", 0.4f, 1.0f, false, "red_tailed_hawk"));
        EntityRendererRegistry.register(RyanEntities.TURKEY_VULTURE, c -> new CritterRenderer<>(c, "turkey_vulture", 0.5f, 1.0f, false, "turkey_vulture"));
        EntityRendererRegistry.register(RyanEntities.GREAT_HORNED_OWL, c -> new CritterRenderer<>(c, "great_horned_owl", 0.4f, 1.0f, false, "great_horned_owl"));
        EntityRendererRegistry.register(RyanEntities.GREAT_BLUE_HERON, c -> new CritterRenderer<>(c, "great_blue_heron", 0.4f, 1.0f, false, "great_blue_heron"));
        EntityRendererRegistry.register(RyanEntities.CANADA_GOOSE, c -> new CritterRenderer<>(c, "canada_goose", 0.4f, 1.0f, false, "canada_goose"));
        EntityRendererRegistry.register(RyanEntities.ORCA, c -> new CritterRenderer<>(c, "orca", 1.0f, 2.0f, true, "orca"));
        EntityRendererRegistry.register(RyanEntities.BLUE_WHALE, c -> new CritterRenderer<>(c, "blue_whale", 2.0f, 3.0f, true, "blue_whale"));
        EntityRendererRegistry.register(RyanEntities.SHARK, c -> new CritterRenderer<>(c, "shark", 0.7f, 1.6f, true, "shark"));
        EntityRendererRegistry.register(RyanEntities.MANTA_RAY, c -> new CritterRenderer<>(c, "manta_ray", 0.8f, 1.5f, true, "manta_ray"));
        EntityRendererRegistry.register(RyanEntities.JELLYFISH, c -> new CritterRenderer<>(c, "jellyfish", 0.3f, 1.0f, true, "jellyfish"));
        EntityRendererRegistry.register(RyanEntities.BUTTERFLY, c -> new CritterRenderer<>(c, "butterfly", 0.0f, 0.35f, false, "butterfly_0", "butterfly_1", "butterfly_2", "butterfly_3", "butterfly_4", "butterfly_5", "butterfly_6", "butterfly_7", "butterfly_8", "butterfly_9", "butterfly_10", "butterfly_11", "butterfly_12", "butterfly_13", "butterfly_14", "butterfly_15", "butterfly_16", "butterfly_17", "butterfly_18", "butterfly_19"));
        EntityRendererRegistry.register(RyanEntities.HUMMINGBIRD, c -> new CritterRenderer<>(c, "hummingbird", 0.1f, 1.0f, false, "hummingbird"));
        EntityRendererRegistry.register(RyanEntities.PILEATED_WOODPECKER, c -> new CritterRenderer<>(c, "pileated_woodpecker", 0.25f, 1.0f, false, "pileated_woodpecker"));
        EntityRendererRegistry.register(RyanEntities.BOBWHITE, c -> new CritterRenderer<>(c, "bobwhite", 0.25f, 1.0f, false, "bobwhite"));
        EntityRendererRegistry.register(RyanEntities.BOX_TURTLE, c -> new CritterRenderer<>(c, "box_turtle", 0.3f, 1.0f, false, "box_turtle"));
        EntityRendererRegistry.register(RyanEntities.RATTLESNAKE, c -> new CritterRenderer<>(c, "rattlesnake", 0.3f, 1.0f, false, "rattlesnake"));
        EntityRendererRegistry.register(RyanEntities.BULLFROG, c -> new CritterRenderer<>(c, "bullfrog", 0.3f, 1.0f, false, "bullfrog"));
        EntityRendererRegistry.register(RyanEntities.ALLIGATOR, c -> new CritterRenderer<>(c, "alligator", 0.8f, 1.2f, false, "alligator"));
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(com.ryancraft.trees.RyanWood.DOGWOOD_PETALS,
                net.minecraft.client.particle.FallingLeavesParticle.CherryProvider::new);
        EntityRendererRegistry.register(RyanEntities.MOOBLOOM, com.ryancraft.client.render.MoobloomRenderer::create);
        net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlocks(net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT,
                com.ryancraft.flora.RyanFlora.BUTTERCUP, com.ryancraft.flora.RyanFlora.POTTED_BUTTERCUP);
        EntityRendererRegistry.register(RyanEntities.REINDEER, com.ryancraft.client.render.ReindeerRenderer::new);
        EntityRendererRegistry.register(RyanEntities.ZEBRA, net.minecraft.client.renderer.entity.HorseRenderer::new);
        EntityRendererRegistry.register(RyanEntities.CROW, c -> new CritterRenderer<>(c, "crow", 0.3f, 1.0f, false, "crow"));
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof net.minecraft.client.renderer.entity.player.AvatarRenderer<?> avatar) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                var parent = (net.minecraft.client.renderer.entity.RenderLayerParent) avatar;
                helper.register(new com.ryancraft.client.render.CrowOnShoulderLayer(parent, ctx.getModelSet()));
            }
        });
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
        ClassicPack.register();
        ClientLifecycleEvents.CLIENT_STARTED.register(ClassicPack::start);
    }
}

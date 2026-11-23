package com.ryancraft.entity;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class RyanEntities {
    public static final EntityType<Boar> BOAR = register("boar",
            EntityType.Builder.of(Boar::new, MobCategory.CREATURE).sized(0.9f, 1.0f).clientTrackingRange(10));
    public static final EntityType<Deer> DEER = register("deer",
            EntityType.Builder.of(Deer::new, MobCategory.CREATURE).sized(0.9f, 1.6f).clientTrackingRange(10));
    public static final EntityType<Duck> DUCK = register("duck",
            EntityType.Builder.of(Duck::new, MobCategory.CREATURE).sized(0.4f, 0.7f).clientTrackingRange(10));

    public static final EntityType<Turkey> TURKEY = register("turkey",
            EntityType.Builder.of(Turkey::new, MobCategory.CREATURE).sized(0.7f, 1.2f).clientTrackingRange(10));

    public static final EntityType<BlackBear> BLACK_BEAR = register("black_bear",
            EntityType.Builder.of(BlackBear::new, MobCategory.CREATURE).sized(1.2f, 1.3f).clientTrackingRange(10));
    public static final EntityType<Raccoon> RACCOON = register("raccoon",
            EntityType.Builder.of(Raccoon::new, MobCategory.CREATURE).sized(0.6f, 0.6f).clientTrackingRange(10));
    public static final EntityType<Skunk> SKUNK = register("skunk",
            EntityType.Builder.of(Skunk::new, MobCategory.CREATURE).sized(0.6f, 0.6f).clientTrackingRange(10));
    public static final EntityType<Mink> MINK = register("mink",
            EntityType.Builder.of(Mink::new, MobCategory.CREATURE).sized(0.5f, 0.4f).clientTrackingRange(10));
    public static final EntityType<Opossum> OPOSSUM = register("opossum",
            EntityType.Builder.of(Opossum::new, MobCategory.CREATURE).sized(0.6f, 0.55f).clientTrackingRange(10));
    public static final EntityType<Beaver> BEAVER = register("beaver",
            EntityType.Builder.of(Beaver::new, MobCategory.CREATURE).sized(0.7f, 0.6f).clientTrackingRange(10));
    public static final EntityType<Squirrel> SQUIRREL = register("squirrel",
            EntityType.Builder.of(Squirrel::new, MobCategory.CREATURE).sized(0.4f, 0.45f).clientTrackingRange(10));
    public static final EntityType<Songbird> SONGBIRD = register("songbird",
            EntityType.Builder.of(Songbird::new, MobCategory.CREATURE).sized(0.4f, 0.5f).clientTrackingRange(10));
    public static final EntityType<KentuckyFish> BLUEGILL = fish("bluegill", 0.4f, 0.45f);
    public static final EntityType<KentuckyFish> CRAPPIE = fish("crappie", 0.4f, 0.45f);
    public static final EntityType<KentuckyFish> LARGEMOUTH_BASS = fish("largemouth_bass", 0.5f, 0.4f);
    public static final EntityType<KentuckyFish> CHANNEL_CATFISH = fish("channel_catfish", 0.6f, 0.3f);
    public static final EntityType<KentuckyFish> RAINBOW_TROUT = fish("rainbow_trout", 0.45f, 0.35f);
    public static final EntityType<KentuckyFish> SMALLMOUTH_BASS = fish("smallmouth_bass", 0.5f, 0.4f);
    public static final EntityType<KentuckyFish> YELLOW_PERCH = fish("yellow_perch", 0.4f, 0.4f);
    public static final EntityType<KentuckyFish> WALLEYE = fish("walleye", 0.5f, 0.35f);
    public static final EntityType<KentuckyFish> NORTHERN_PIKE = fish("northern_pike", 0.7f, 0.3f);
    public static final EntityType<KentuckyFish> MUSKELLUNGE = fish("muskellunge", 0.8f, 0.35f);

    public static final EntityType<Coyote> COYOTE = register("coyote",
            EntityType.Builder.of(Coyote::new, MobCategory.CREATURE).sized(0.6f, 0.8f).clientTrackingRange(10));
    public static final EntityType<Chipmunk> CHIPMUNK = register("chipmunk",
            EntityType.Builder.of(Chipmunk::new, MobCategory.CREATURE).sized(0.35f, 0.35f).clientTrackingRange(10));
    public static final EntityType<Elk> ELK = register("elk",
            EntityType.Builder.of(Elk::new, MobCategory.CREATURE).sized(1.2f, 2.0f).clientTrackingRange(10));
    public static final EntityType<Moose> MOOSE = register("moose",
            EntityType.Builder.of(Moose::new, MobCategory.CREATURE).sized(1.3f, 2.3f).clientTrackingRange(10));
    public static final EntityType<Pronghorn> PRONGHORN = register("pronghorn",
            EntityType.Builder.of(Pronghorn::new, MobCategory.CREATURE).sized(0.8f, 1.6f).clientTrackingRange(10));
    public static final EntityType<GrizzlyBear> GRIZZLY_BEAR = register("grizzly_bear",
            EntityType.Builder.of(GrizzlyBear::new, MobCategory.CREATURE).sized(1.4f, 1.5f).clientTrackingRange(10));
    public static final EntityType<Bobcat> BOBCAT = register("bobcat",
            EntityType.Builder.of(Bobcat::new, MobCategory.CREATURE).sized(0.6f, 0.8f).clientTrackingRange(10));
    public static final EntityType<Crow> CROW = register("crow",
            EntityType.Builder.of(Crow::new, MobCategory.CREATURE).sized(0.4f, 0.6f).clientTrackingRange(10));
    public static final EntityType<Zebra> ZEBRA = register("zebra",
            EntityType.Builder.of(Zebra::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.44375f).clientTrackingRange(10));
    public static final EntityType<BlackTailedDeer> BLACK_TAILED_DEER = register("black_tailed_deer",
            EntityType.Builder.of(BlackTailedDeer::new, MobCategory.CREATURE).sized(0.8f, 1.5f).clientTrackingRange(10));
    public static final EntityType<Bison> BISON = register("bison",
            EntityType.Builder.of(Bison::new, MobCategory.CREATURE).sized(1.4f, 1.9f).clientTrackingRange(10));
    public static final EntityType<Cougar> COUGAR = register("cougar",
            EntityType.Builder.of(Cougar::new, MobCategory.CREATURE).sized(0.8f, 0.9f).clientTrackingRange(10));
    public static final EntityType<RiverOtter> RIVER_OTTER = register("river_otter",
            EntityType.Builder.of(RiverOtter::new, MobCategory.CREATURE).sized(0.5f, 0.4f).clientTrackingRange(10));
    public static final EntityType<Groundhog> GROUNDHOG = register("groundhog",
            EntityType.Builder.of(Groundhog::new, MobCategory.CREATURE).sized(0.6f, 0.55f).clientTrackingRange(10));
    public static final EntityType<Porcupine> PORCUPINE = register("porcupine",
            EntityType.Builder.of(Porcupine::new, MobCategory.CREATURE).sized(0.6f, 0.6f).clientTrackingRange(10));
    public static final EntityType<BaldEagle> BALD_EAGLE = register("bald_eagle",
            EntityType.Builder.of(BaldEagle::new, MobCategory.CREATURE).sized(0.7f, 0.9f).clientTrackingRange(10));
    /** The Skylands' great gliding ray. */
    public static final EntityType<SkyRay> SKY_RAY = register("sky_ray",
            EntityType.Builder.of(SkyRay::new, MobCategory.CREATURE).sized(1.6f, 0.5f).clientTrackingRange(12));
    /** The Skylands' winged horse and its puffy sheep. */
    public static final EntityType<Pegasus> PEGASUS = register("pegasus",
            EntityType.Builder.of(Pegasus::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f).eyeHeight(1.52f).clientTrackingRange(10));
    public static final EntityType<CloudSheep> CLOUD_SHEEP = register("cloud_sheep",
            EntityType.Builder.of(CloudSheep::new, MobCategory.CREATURE).sized(0.9f, 1.3f).eyeHeight(1.235f).clientTrackingRange(10));
    public static final EntityType<SkyRoc> SKY_ROC = register("sky_roc",
            EntityType.Builder.of(SkyRoc::new, MobCategory.CREATURE).sized(1.6f, 1.2f).clientTrackingRange(14));
    /** The moon's own rabbit. */
    public static final EntityType<MoonRabbit> MOON_RABBIT = register("moon_rabbit",
            EntityType.Builder.of(MoonRabbit::new, MobCategory.CREATURE).sized(0.49f, 0.6f).eyeHeight(0.59f).clientTrackingRange(8));
    public static final EntityType<GoldenEagle> GOLDEN_EAGLE = register("golden_eagle",
            EntityType.Builder.of(GoldenEagle::new, MobCategory.CREATURE).sized(0.7f, 0.9f).clientTrackingRange(10));
    public static final EntityType<RedTailedHawk> RED_TAILED_HAWK = register("red_tailed_hawk",
            EntityType.Builder.of(RedTailedHawk::new, MobCategory.CREATURE).sized(0.6f, 0.7f).clientTrackingRange(10));
    public static final EntityType<TurkeyVulture> TURKEY_VULTURE = register("turkey_vulture",
            EntityType.Builder.of(TurkeyVulture::new, MobCategory.CREATURE).sized(0.7f, 0.8f).clientTrackingRange(10));
    public static final EntityType<GreatHornedOwl> GREAT_HORNED_OWL = register("great_horned_owl",
            EntityType.Builder.of(GreatHornedOwl::new, MobCategory.CREATURE).sized(0.6f, 0.9f).clientTrackingRange(10));
    public static final EntityType<GreatBlueHeron> GREAT_BLUE_HERON = register("great_blue_heron",
            EntityType.Builder.of(GreatBlueHeron::new, MobCategory.CREATURE).sized(0.6f, 1.4f).clientTrackingRange(10));
    public static final EntityType<CanadaGoose> CANADA_GOOSE = register("canada_goose",
            EntityType.Builder.of(CanadaGoose::new, MobCategory.CREATURE).sized(0.6f, 0.9f).clientTrackingRange(10));
    public static final EntityType<Orca> ORCA = register("orca",
            EntityType.Builder.of(Orca::new, MobCategory.WATER_CREATURE).sized(2.2f, 1.4f).clientTrackingRange(10));
    public static final EntityType<BlueWhale> BLUE_WHALE = register("blue_whale",
            EntityType.Builder.of(BlueWhale::new, MobCategory.WATER_CREATURE).sized(4.5f, 2.8f).clientTrackingRange(10));
    public static final EntityType<Shark> SHARK = register("shark",
            EntityType.Builder.of(Shark::new, MobCategory.WATER_CREATURE).sized(1.4f, 0.9f).clientTrackingRange(10));
    public static final EntityType<MantaRay> MANTA_RAY = register("manta_ray",
            EntityType.Builder.of(MantaRay::new, MobCategory.WATER_CREATURE).sized(2.2f, 0.4f).clientTrackingRange(10));
    public static final EntityType<Jellyfish> JELLYFISH = register("jellyfish",
            EntityType.Builder.of(Jellyfish::new, MobCategory.WATER_CREATURE).sized(0.7f, 1.0f).clientTrackingRange(10));
    public static final EntityType<Butterfly> BUTTERFLY = register("butterfly",
            EntityType.Builder.of(Butterfly::new, MobCategory.CREATURE).sized(0.4f, 0.3f).clientTrackingRange(10));
    public static final EntityType<Hummingbird> HUMMINGBIRD = register("hummingbird",
            EntityType.Builder.of(Hummingbird::new, MobCategory.CREATURE).sized(0.3f, 0.3f).clientTrackingRange(10));
    public static final EntityType<PileatedWoodpecker> PILEATED_WOODPECKER = register("pileated_woodpecker",
            EntityType.Builder.of(PileatedWoodpecker::new, MobCategory.CREATURE).sized(0.4f, 0.8f).clientTrackingRange(10));
    public static final EntityType<Bobwhite> BOBWHITE = register("bobwhite",
            EntityType.Builder.of(Bobwhite::new, MobCategory.CREATURE).sized(0.4f, 0.45f).clientTrackingRange(10));
    public static final EntityType<BoxTurtle> BOX_TURTLE = register("box_turtle",
            EntityType.Builder.of(BoxTurtle::new, MobCategory.CREATURE).sized(0.5f, 0.35f).clientTrackingRange(10));
    public static final EntityType<Rattlesnake> RATTLESNAKE = register("rattlesnake",
            EntityType.Builder.of(Rattlesnake::new, MobCategory.CREATURE).sized(0.6f, 0.3f).clientTrackingRange(10));
    public static final EntityType<Bullfrog> BULLFROG = register("bullfrog",
            EntityType.Builder.of(Bullfrog::new, MobCategory.CREATURE).sized(0.4f, 0.3f).clientTrackingRange(10));
    public static final EntityType<Alligator> ALLIGATOR = register("alligator",
            EntityType.Builder.of(Alligator::new, MobCategory.CREATURE).sized(1.0f, 0.5f).clientTrackingRange(10));
    public static final EntityType<Swan> SWAN = register("swan",
            EntityType.Builder.of(Swan::new, MobCategory.CREATURE).sized(0.7f, 1.1f).clientTrackingRange(10));
    public static final EntityType<Moobloom> MOOBLOOM = register("moobloom",
            EntityType.Builder.of(Moobloom::new, MobCategory.CREATURE).sized(0.9f, 1.4f).eyeHeight(1.3f).passengerAttachments(1.36875f).clientTrackingRange(10));
    /** Little biome soldiers; they live at camps (MudlingCampFeature), never spawn freely. */
    public static final EntityType<Mudling> MUDLING = register("mudling",
            EntityType.Builder.of(Mudling::new, MobCategory.MISC).sized(0.4f, 0.55f).eyeHeight(0.45f).clientTrackingRange(8));
    public static final EntityType<Reindeer> REINDEER = register("reindeer",
            EntityType.Builder.of(Reindeer::new, MobCategory.CREATURE).sized(0.9f, 1.6f).clientTrackingRange(10));
    public static final EntityType<DeerMount> DEER_MOUNT = register("deer_skull_mount",
            EntityType.Builder.of(DeerMount::new, MobCategory.MISC).sized(0.5f, 0.9f).clientTrackingRange(10));

    private static EntityType<KentuckyFish> fish(String name, float w, float h) {
        return register(name, EntityType.Builder.of(KentuckyFish::new, MobCategory.WATER_AMBIENT).sized(w, h).clientTrackingRange(4));
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        RyanSpawning.init();
        FabricDefaultAttributeRegistry.register(BOAR, Boar.createAttributes());
        FabricDefaultAttributeRegistry.register(DEER, Deer.createAttributes());
        FabricDefaultAttributeRegistry.register(DUCK, Duck.createAttributes());
        FabricDefaultAttributeRegistry.register(TURKEY, Turkey.createAttributes());
        FabricDefaultAttributeRegistry.register(BLACK_BEAR, BlackBear.createAttributes());
        FabricDefaultAttributeRegistry.register(RACCOON, Raccoon.createAttributes());
        FabricDefaultAttributeRegistry.register(SKUNK, Skunk.createAttributes());
        FabricDefaultAttributeRegistry.register(MINK, Mink.createAttributes());
        FabricDefaultAttributeRegistry.register(OPOSSUM, Opossum.createAttributes());
        FabricDefaultAttributeRegistry.register(BEAVER, Beaver.createAttributes());
        FabricDefaultAttributeRegistry.register(SQUIRREL, Squirrel.createAttributes());
        FabricDefaultAttributeRegistry.register(SONGBIRD, Songbird.createAttributes());
        FabricDefaultAttributeRegistry.register(COYOTE, Coyote.createAttributes());
        FabricDefaultAttributeRegistry.register(CHIPMUNK, Chipmunk.createAttributes());
        FabricDefaultAttributeRegistry.register(ELK, Elk.createAttributes());
        FabricDefaultAttributeRegistry.register(MOOSE, Moose.createAttributes());
        FabricDefaultAttributeRegistry.register(PRONGHORN, Pronghorn.createAttributes());
        FabricDefaultAttributeRegistry.register(GRIZZLY_BEAR, GrizzlyBear.createAttributes());
        FabricDefaultAttributeRegistry.register(BOBCAT, Bobcat.createAttributes());
        FabricDefaultAttributeRegistry.register(CROW, Crow.createAttributes());
        FabricDefaultAttributeRegistry.register(ZEBRA, net.minecraft.world.entity.animal.equine.AbstractHorse.createBaseHorseAttributes());
        FabricDefaultAttributeRegistry.register(BLACK_TAILED_DEER, BlackTailedDeer.createAttributes());
        FabricDefaultAttributeRegistry.register(BISON, Bison.createAttributes());
        FabricDefaultAttributeRegistry.register(COUGAR, Cougar.createAttributes());
        FabricDefaultAttributeRegistry.register(RIVER_OTTER, RiverOtter.createAttributes());
        FabricDefaultAttributeRegistry.register(GROUNDHOG, Groundhog.createAttributes());
        FabricDefaultAttributeRegistry.register(PORCUPINE, Porcupine.createAttributes());
        FabricDefaultAttributeRegistry.register(BALD_EAGLE, BaldEagle.createAttributes());
        FabricDefaultAttributeRegistry.register(SKY_RAY, SkyRay.createAttributes());
        FabricDefaultAttributeRegistry.register(MOON_RABBIT, net.minecraft.world.entity.animal.rabbit.Rabbit.createAttributes());
        FabricDefaultAttributeRegistry.register(SKY_ROC, SkyRoc.createAttributes());
        FabricDefaultAttributeRegistry.register(PEGASUS, net.minecraft.world.entity.animal.equine.AbstractHorse.createBaseHorseAttributes());
        FabricDefaultAttributeRegistry.register(CLOUD_SHEEP, net.minecraft.world.entity.animal.sheep.Sheep.createAttributes());
        FabricDefaultAttributeRegistry.register(GOLDEN_EAGLE, GoldenEagle.createAttributes());
        FabricDefaultAttributeRegistry.register(RED_TAILED_HAWK, RedTailedHawk.createAttributes());
        FabricDefaultAttributeRegistry.register(TURKEY_VULTURE, TurkeyVulture.createAttributes());
        FabricDefaultAttributeRegistry.register(GREAT_HORNED_OWL, GreatHornedOwl.createAttributes());
        FabricDefaultAttributeRegistry.register(GREAT_BLUE_HERON, GreatBlueHeron.createAttributes());
        FabricDefaultAttributeRegistry.register(CANADA_GOOSE, CanadaGoose.createAttributes());
        FabricDefaultAttributeRegistry.register(ORCA, Orca.createAttributes());
        FabricDefaultAttributeRegistry.register(BLUE_WHALE, BlueWhale.createAttributes());
        FabricDefaultAttributeRegistry.register(SHARK, Shark.createAttributes());
        FabricDefaultAttributeRegistry.register(MANTA_RAY, MantaRay.createAttributes());
        FabricDefaultAttributeRegistry.register(JELLYFISH, Jellyfish.createAttributes());
        FabricDefaultAttributeRegistry.register(BUTTERFLY, Butterfly.createAttributes());
        FabricDefaultAttributeRegistry.register(HUMMINGBIRD, Hummingbird.createAttributes());
        FabricDefaultAttributeRegistry.register(PILEATED_WOODPECKER, PileatedWoodpecker.createAttributes());
        FabricDefaultAttributeRegistry.register(BOBWHITE, Bobwhite.createAttributes());
        FabricDefaultAttributeRegistry.register(BOX_TURTLE, BoxTurtle.createAttributes());
        FabricDefaultAttributeRegistry.register(RATTLESNAKE, Rattlesnake.createAttributes());
        FabricDefaultAttributeRegistry.register(BULLFROG, Bullfrog.createAttributes());
        FabricDefaultAttributeRegistry.register(ALLIGATOR, Alligator.createAttributes());
        FabricDefaultAttributeRegistry.register(REINDEER, Deer.createAttributes());
        FabricDefaultAttributeRegistry.register(SWAN, Swan.createAttributes());
        FabricDefaultAttributeRegistry.register(MOOBLOOM, net.minecraft.world.entity.animal.cow.Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(MUDLING, Mudling.createAttributes());
        FabricDefaultAttributeRegistry.register(DEER_MOUNT, net.minecraft.world.entity.Mob.createMobAttributes());
        for (var f : java.util.List.of(BLUEGILL, CRAPPIE, LARGEMOUTH_BASS, CHANNEL_CATFISH, RAINBOW_TROUT, SMALLMOUTH_BASS, YELLOW_PERCH, WALLEYE, NORTHERN_PIKE, MUSKELLUNGE)) {
            FabricDefaultAttributeRegistry.register(f, net.minecraft.world.entity.animal.fish.AbstractFish.createAttributes());
        }
    }

    private RyanEntities() {}
}

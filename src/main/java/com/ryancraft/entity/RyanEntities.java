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

    public static final EntityType<Coyote> COYOTE = register("coyote",
            EntityType.Builder.of(Coyote::new, MobCategory.CREATURE).sized(0.6f, 0.8f).clientTrackingRange(10));
    public static final EntityType<Chipmunk> CHIPMUNK = register("chipmunk",
            EntityType.Builder.of(Chipmunk::new, MobCategory.CREATURE).sized(0.35f, 0.35f).clientTrackingRange(10));
    public static final EntityType<Elk> ELK = register("elk",
            EntityType.Builder.of(Elk::new, MobCategory.CREATURE).sized(1.2f, 2.0f).clientTrackingRange(10));
    public static final EntityType<Moose> MOOSE = register("moose",
            EntityType.Builder.of(Moose::new, MobCategory.CREATURE).sized(1.3f, 2.3f).clientTrackingRange(10));
    public static final EntityType<Pronghorn> PRONGHORN = register("pronghorn",
            EntityType.Builder.of(Pronghorn::new, MobCategory.CREATURE).sized(0.8f, 1.4f).clientTrackingRange(10));
    public static final EntityType<GrizzlyBear> GRIZZLY_BEAR = register("grizzly_bear",
            EntityType.Builder.of(GrizzlyBear::new, MobCategory.CREATURE).sized(1.4f, 1.5f).clientTrackingRange(10));
    public static final EntityType<Bobcat> BOBCAT = register("bobcat",
            EntityType.Builder.of(Bobcat::new, MobCategory.CREATURE).sized(0.6f, 0.65f).clientTrackingRange(10));
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
        FabricDefaultAttributeRegistry.register(DEER_MOUNT, net.minecraft.world.entity.Mob.createMobAttributes());
        for (var f : java.util.List.of(BLUEGILL, CRAPPIE, LARGEMOUTH_BASS, CHANNEL_CATFISH)) {
            FabricDefaultAttributeRegistry.register(f, net.minecraft.world.entity.animal.fish.AbstractFish.createAttributes());
        }
    }

    private RyanEntities() {}
}

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

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(BOAR, Boar.createAttributes());
        FabricDefaultAttributeRegistry.register(DEER, Deer.createAttributes());
        FabricDefaultAttributeRegistry.register(DUCK, Duck.createAttributes());
    }

    private RyanEntities() {}
}

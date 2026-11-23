package com.ryancraft.combat;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Cannons and their shot (see {@link ShotType}), and the lead musket ball. Assets: tools/cannons.py.
 * From the gunner's seat, using powder, shot or a light on nothing loads or fires the cannon you sit on.
 */
public final class Cannons {
    public static final EntityType<CannonEntity> CANNON_ENTITY = entity("cannon",
            EntityType.Builder.<CannonEntity>of(CannonEntity::new, MobCategory.MISC).sized(1.2f, 1.0f).clientTrackingRange(10));
    public static final EntityType<CannonShot> SHOT = entity("cannon_shot",
            EntityType.Builder.<CannonShot>of(CannonShot::new, MobCategory.MISC).sized(0.3f, 0.3f).clientTrackingRange(8).updateInterval(2));
    private static final Map<ShotType, Item> AMMO = new EnumMap<>(ShotType.class);
    public static final Item CANNON = item("cannon", p -> new Item(p) {
        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            Level level = ctx.getLevel();
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Vec3 at = Vec3.atBottomCenterOf(ctx.getClickedPos().relative(ctx.getClickedFace()));
            CannonEntity c = CANNON_ENTITY.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (c == null) return InteractionResult.FAIL;
            c.snapTo(at.x, at.y, at.z, ctx.getPlayer() == null ? 0 : ctx.getPlayer().getYRot(), 0);
            if (!level.noCollision(c, c.getBoundingBox())) return InteractionResult.FAIL;
            level.addFreshEntity(c);
            ctx.getItemInHand().consume(1, ctx.getPlayer());
            return InteractionResult.SUCCESS;
        }
    }, new Item.Properties().stacksTo(1));

    static {
        for (ShotType t : ShotType.values()) AMMO.put(t, item(t.item, Item::new, new Item.Properties().stacksTo(t == ShotType.PELLET ? 64 : 16)));
    }

    public static Item item(ShotType t) { return AMMO.get(t); }

    public static @Nullable ShotType shotOf(ItemStack s) {
        for (var e : AMMO.entrySet()) if (e.getKey() != ShotType.PELLET && s.is(e.getValue())) return e.getKey();
        return null;
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String name, EntityType.Builder<T> b) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, b.build(key));
    }

    private static Item item(String name, java.util.function.Function<Item.Properties, Item> f, Item.Properties p) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(p.setId(key)));
    }

    public static void init() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (player.getVehicle() instanceof CannonEntity c) return c.handle(player, player.getItemInHand(hand));
            return InteractionResult.PASS;
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(e -> {
            e.accept(CANNON);
            for (ShotType t : ShotType.values()) e.accept(AMMO.get(t));
        });
    }

    private Cannons() {}
}

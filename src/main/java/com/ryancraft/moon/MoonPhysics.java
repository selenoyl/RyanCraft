package com.ryancraft.moon;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

/**
 * The moon's physics and air (the mixins MoonGravityMixin / MoonAirMixin call in here):
 * <ul><li>Low gravity: living things fall at {@link #LIVING_GRAVITY} of Earth's (big floaty jumps, gentle landings, and
 * fall damage scaled the same), and arrows, thrown things, dropped items and falling blocks at {@link #THROWN_GRAVITY},
 * so arrows fly nearly flat.</li>
 * <li>No air: on the moon your air runs down as if you were underwater, and then you suffocate. A Potion of Air (an
 * awkward potion brewed with a wind charge: 3 minutes, 8 with redstone) lets you breathe; water breathing and
 * Respiration don't help, as there's no water to breathe from. (Force fields and the jade crown will too, later.)</li></ul>
 */
public final class MoonPhysics {
    public static final double LIVING_GRAVITY = 0.3, THROWN_GRAVITY = 0.17;

    public static final Holder<MobEffect> AIR = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, RyanCraft.id("air"),
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xBFE4FF) {});
    public static final Holder<Potion> AIR_POTION = Registry.registerForHolder(BuiltInRegistries.POTION, RyanCraft.id("air"),
            new Potion("air", new MobEffectInstance(AIR, 3600)));
    public static final Holder<Potion> LONG_AIR_POTION = Registry.registerForHolder(BuiltInRegistries.POTION, RyanCraft.id("long_air"),
            new Potion("air", new MobEffectInstance(AIR, 9600)));

    public static boolean onMoon(Entity e) { return e.level().dimension().equals(MoonPortalBlock.MOON); }

    /** Gravity scale for this entity where it is now (1 off the moon). */
    public static double gravityScale(Entity e) {
        if (!onMoon(e)) return 1.0;
        return e instanceof LivingEntity ? LIVING_GRAVITY : THROWN_GRAVITY;
    }

    /** Whether this entity has to hold its breath here. */
    public static boolean breathless(LivingEntity e) {
        if (!(e instanceof Player p) || !onMoon(e) || p.isCreative() || p.isSpectator()) return false;
        return !e.hasEffect(AIR) && !MoonRelics.wearingCrown(e);
    }

    public static void init() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(b -> {
            b.addMix(Potions.AWKWARD, Items.WIND_CHARGE, AIR_POTION);
            b.addMix(AIR_POTION, Items.REDSTONE, LONG_AIR_POTION);
        });
    }

    private MoonPhysics() {}
}

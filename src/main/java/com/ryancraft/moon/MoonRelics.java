package com.ryancraft.moon;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

/**
 * The Great Moon Rabbit's relics, dropped every time it falls:
 * <ul><li>the <b>jade crown</b> (helmet slot): breathe underwater and on the airless moon, mine underwater at full speed
 * and see far better underwater (conduit power while you're in water), for as long as you wear it.</li>
 * <li>the <b>Moon Mortar</b>, a mace-like heavy weapon: it hits harder the farther you fall (Mooncharge, as the mace);
 * a falling slam throws nearby mobs up into a few seconds of moon gravity (Pound); and hits on anything floating land
 * harder (Grind).</li></ul>
 */
public final class MoonRelics {
    public static final TagKey<Item> CROWN_REPAIR = TagKey.create(Registries.ITEM, RyanCraft.id("jade_crown_repair"));
    private static final ArmorMaterial CROWN = new ArmorMaterial(37, Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 3, ArmorType.LEGGINGS, 3,
            ArmorType.BOOTS, 3, ArmorType.BODY, 3), 25, SoundEvents.ARMOR_EQUIP_GOLD, 1f, 0f, CROWN_REPAIR,
            ResourceKey.create(EquipmentAssets.ROOT_ID, RyanCraft.id("jade_crown")));

    public static final Item JADE_CROWN = register("jade_crown", p -> new Item(p.humanoidArmor(CROWN, ArmorType.HELMET).rarity(Rarity.EPIC).fireResistant()));
    public static final Item MOON_MORTAR = register("moon_mortar", p -> new MoonMortar(p.rarity(Rarity.EPIC).durability(600)
            .component(DataComponents.TOOL, MaceItem.createToolProperties()).attributes(mortarAttributes()).enchantable(15).fireResistant()));

    /** Heavier than the mace: 14 attack damage, at the mace's slow swing. */
    private static net.minecraft.world.item.component.ItemAttributeModifiers mortarAttributes() {
        var main = net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND;
        var add = net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE;
        return net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 13.0, add), main)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.4, add), main)
                .build();
    }

    private static Item register(String name, java.util.function.Function<Item.Properties, Item> f) {
        ResourceKey<Item> k = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, k, f.apply(new Item.Properties().setId(k)));
    }

    public static boolean wearingCrown(LivingEntity e) { return e.getItemBySlot(EquipmentSlot.HEAD).is(JADE_CROWN); }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(e -> { e.accept(JADE_CROWN); e.accept(MOON_MORTAR); });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer p : server.getPlayerList().getPlayers())
                if (wearingCrown(p) && p.isEyeInFluid(FluidTags.WATER))
                    p.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, false, true));
        });
    }

    /** The rabbit's own mortar staff, shrunk to fit your hands. */
    public static class MoonMortar extends MaceItem {
        public MoonMortar(Properties p) { super(p); }

        @Override
        public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
            float bonus = super.getAttackDamageBonus(target, damage, source);
            return target.onGround() || target.isInWater() ? bonus : bonus + 3f;            // Grind
        }

        @Override
        public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean slam = canSmashAttack(attacker);
            super.postHurtEnemy(stack, target, attacker);
            if (!slam) return;                                                          // Pound: float them up, helpless
            for (LivingEntity e : attacker.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5),
                    e -> e != attacker && e.isAlive() && !e.isSpectator())) {
                e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 1), attacker);
                e.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0), attacker);
            }
        }
    }

    private MoonRelics() {}
}

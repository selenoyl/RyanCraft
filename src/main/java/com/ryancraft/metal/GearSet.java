package com.ryancraft.metal;

import com.ryancraft.RyanCraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A full set of tools (sword, pickaxe, axe, shovel, hoe) and armour (helmet, chestplate, leggings, boots) for one
 * material, at a vanilla tier's stats. Repaired with the material's own item (tag ryancraft:<name>_tool_materials).
 */
public final class GearSet {
    public final String name;
    public final Item sword, pickaxe, axe, shovel, hoe, helmet, chestplate, leggings, boots;
    public final List<Item> tools = new ArrayList<>(), armour = new ArrayList<>();

    /** Iron's tier: durability 250, speed 6, +2 damage; armour 2/6/5/2. */
    public static GearSet ironTier(String name, int enchantability) {
        return new GearSet(name, BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0f, 2.0f, enchantability, 15, new int[]{2, 5, 6, 2}, 0f);
    }

    public GearSet(String name, TagKey<net.minecraft.world.level.block.Block> incorrect, int durability, float speed, float damage, int ench,
                   int armourDurability, int[] defence /* boots, legs, chest, helmet */, float toughness) {
        this.name = name;
        TagKey<Item> repair = TagKey.create(Registries.ITEM, RyanCraft.id(name + "_tool_materials"));
        ToolMaterial tm = new ToolMaterial(incorrect, durability, speed, damage, ench, repair);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, RyanCraft.id(name));
        ArmorMaterial am = new ArmorMaterial(armourDurability, Map.of(ArmorType.BOOTS, defence[0], ArmorType.LEGGINGS, defence[1],
                ArmorType.CHESTPLATE, defence[2], ArmorType.HELMET, defence[3], ArmorType.BODY, defence[2]), ench, SoundEvents.ARMOR_EQUIP_IRON,
                toughness, 0f, repair, asset);
        sword = tool("sword", p -> new Item(p.sword(tm, 3.0f, -2.4f)));
        pickaxe = tool("pickaxe", p -> new Item(p.pickaxe(tm, 1.0f, -2.8f)));
        axe = tool("axe", p -> new AxeItem(tm, 6.0f, -3.1f, p));
        shovel = tool("shovel", p -> new ShovelItem(tm, 1.5f, -3.0f, p));
        hoe = tool("hoe", p -> new HoeItem(tm, -2.0f, -1.0f, p));
        helmet = armour("helmet", p -> new Item(p.humanoidArmor(am, ArmorType.HELMET)));
        chestplate = armour("chestplate", p -> new Item(p.humanoidArmor(am, ArmorType.CHESTPLATE)));
        leggings = armour("leggings", p -> new Item(p.humanoidArmor(am, ArmorType.LEGGINGS)));
        boots = armour("boots", p -> new Item(p.humanoidArmor(am, ArmorType.BOOTS)));
    }

    private Item tool(String kind, Function<Item.Properties, Item> f) { Item i = register(name + "_" + kind, f); tools.add(i); return i; }
    private Item armour(String kind, Function<Item.Properties, Item> f) { Item i = register(name + "_" + kind, f); armour.add(i); return i; }

    private static Item register(String id, Function<Item.Properties, Item> f) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(id));
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(new Item.Properties().setId(key)));
    }

    /** Add the tools and armour to the creative tabs. */
    public void addToTabs() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(e -> { e.accept(shovel); e.accept(pickaxe); e.accept(axe); e.accept(hoe); });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.COMBAT)
                .register(e -> { e.accept(sword); e.accept(axe); armour.forEach(e::accept); });
    }
}

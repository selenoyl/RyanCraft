package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.List;

/**
 * Hunting camouflage: leather armor dyed to match where you're standing makes skittish animals
 * notice you later. White in the snow, dark green or brown in the woods, lime in open grassland,
 * sandy yellow or brown in dry country, gray up in the rocks.
 */
public final class Camouflage {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    /** How close a dyed colour has to be to a matching dye (RGB distance), so close mixes count too. */
    private static final double TOLERANCE = 55.0;

    /** Dye colours that blend in at this spot. */
    public static List<DyeColor> palette(Player player) {
        BlockPos pos = player.blockPosition();
        Holder<Biome> biome = player.level().getBiome(pos);
        if (biome.value().coldEnoughToSnow(pos, player.level().getSeaLevel())) return List.of(DyeColor.WHITE, DyeColor.LIGHT_GRAY);
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_JUNGLE)
                || biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP) || isNamed(biome, "seasonal_forest", "rainforest")) {
            return List.of(DyeColor.GREEN, DyeColor.BROWN);
        }
        if (biome.is(BiomeTags.IS_SAVANNA) || biome.is(BiomeTags.IS_BADLANDS) || biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BEACH)
                || isNamed(biome, "shrubland")) {
            return List.of(DyeColor.YELLOW, DyeColor.BROWN);
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(Biomes.STONY_SHORE)) return List.of(DyeColor.GRAY, DyeColor.LIGHT_GRAY);
        return List.of(DyeColor.LIME, DyeColor.GREEN);   // plains, meadows and the like
    }

    /** Number of worn leather pieces (0-4) dyed to blend in with the player's surroundings. */
    public static int matchingPieces(Player player) {
        List<DyeColor> palette = palette(player);
        int count = 0;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isLeather(stack)) continue;
            DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
            if (dyed != null && palette.stream().anyMatch(d -> close(dyed.rgb(), d.getTextureDiffuseColor()))) count++;
        }
        return count;
    }

    private static boolean isLeather(ItemStack s) {
        return s.is(Items.LEATHER_HELMET) || s.is(Items.LEATHER_CHESTPLATE) || s.is(Items.LEATHER_LEGGINGS) || s.is(Items.LEATHER_BOOTS);
    }

    private static boolean close(int a, int b) {
        int dr = ((a >> 16) & 255) - ((b >> 16) & 255), dg = ((a >> 8) & 255) - ((b >> 8) & 255), db = (a & 255) - (b & 255);
        return Math.sqrt(dr * dr + dg * dg + db * db) <= TOLERANCE;
    }

    private static boolean isNamed(Holder<Biome> biome, String... paths) {
        return biome.unwrapKey().map(k -> List.of(paths).contains(k.identifier().getPath())).orElse(false);
    }

    private Camouflage() {}
}

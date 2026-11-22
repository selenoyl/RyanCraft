package com.ryancraft.entity;

import com.ryancraft.RyanCraft;
import com.ryancraft.mixin.SpawnPlacementsInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Where our mobs may spawn in the wild. Without this they could appear in mid-air, underwater or in dark caves (and many
 * spawn attempts would be wasted): land animals and birds need natural ground and daylight, fish need water with water
 * above them (lakes above sea level count, unlike vanilla's surface-water rule), and ocean animals need water.
 */
public final class RyanSpawning {
    /** Grass, dirt, snow, sand, mud, podzol, gravel: the ground our animals live on, from tundra to swamp to prairie. */
    static boolean naturalGround(BlockState s) {
        return s.is(BlockTags.ANIMALS_SPAWNABLE_ON) || s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.SNOW)
                || s.is(Blocks.POWDER_SNOW) || s.is(Blocks.MUD) || s.is(Blocks.GRAVEL) || s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.CLAY);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            if (!BuiltInRegistries.ENTITY_TYPE.getKey(t).getNamespace().equals(RyanCraft.MOD_ID)) continue;
            MobCategory c = t.getCategory();
            EntityType type = t;
            if (c == MobCategory.CREATURE) {
                SpawnPlacementsInvoker.ryancraft$register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (et, level, reason, pos, rnd) -> {
                            BlockPos below = pos.below();
                            BlockState under = level.getBlockState(below);
                            boolean snowLayer = level.getBlockState(pos).is(Blocks.SNOW);
                            return (naturalGround(under) || snowLayer) && level.getRawBrightness(pos, 0) > 8;
                        });
            } else if (c == MobCategory.WATER_AMBIENT || c == MobCategory.WATER_CREATURE) {
                SpawnPlacementsInvoker.ryancraft$register(type, SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR,
                        (et, level, reason, pos, rnd) -> level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER));
            }
        }
    }

    private RyanSpawning() {}
}

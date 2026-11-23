package com.ryancraft.client.ambience;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/** A snapshot of what's around the player, sampled about once a second. */
final class Surroundings {
    boolean overworld, skylands, underground, submerged, surface;
    int y, seaLevel;
    boolean dawn, day, dusk, night;
    boolean raining, thundering, rainHere, snowy;
    boolean forest, jungle, taiga, swamp, ocean, beach, river, desert, mountain, savanna, open;
    float temperature;
    int leaves, logs, stone, grass, flowingWater, stillWater, fallingWater;
    Vec3 flowingCentre, waterCentre, fallingCentre;
    float skyLight;

    static Surroundings sample(Minecraft mc) {
        Surroundings s = new Surroundings();
        Level level = mc.level;
        BlockPos eye = BlockPos.containing(mc.player.getEyePosition());
        s.overworld = level.dimension() == Level.OVERWORLD;
        s.skylands = level.dimension() == com.ryancraft.sky.Skylands.LEVEL;
        s.y = eye.getY();
        s.seaLevel = level.getSeaLevel();
        s.submerged = mc.player.isEyeInFluid(FluidTags.WATER);
        s.skyLight = level.getBrightness(LightLayer.SKY, eye) / 15.0f;

        long t = level.getDayTime() % 24000L;
        s.dawn = t >= 22800 || t < 1200;
        s.day = t >= 0 && t < 12000;
        s.dusk = t >= 11800 && t < 13500;
        s.night = t >= 13000 && t < 23000;

        s.raining = level.isRaining();
        s.thundering = level.isThundering();
        s.rainHere = s.raining && level.isRainingAt(level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, eye));

        Holder<Biome> biome = level.getBiome(eye);
        s.temperature = biome.value().getBaseTemperature();
        s.snowy = biome.value().coldEnoughToSnow(eye, s.seaLevel);
        s.forest = biome.is(BiomeTags.IS_FOREST);
        s.jungle = biome.is(BiomeTags.IS_JUNGLE);
        s.taiga = biome.is(BiomeTags.IS_TAIGA);
        s.ocean = biome.is(BiomeTags.IS_OCEAN);
        s.beach = biome.is(BiomeTags.IS_BEACH);
        s.river = biome.is(BiomeTags.IS_RIVER);
        s.mountain = biome.is(BiomeTags.IS_MOUNTAIN);
        s.savanna = biome.is(BiomeTags.IS_SAVANNA);
        s.swamp = biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
        s.desert = biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BADLANDS);
        s.open = biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS) || biome.is(Biomes.MEADOW)
                || biome.unwrapKey().map(k -> k.identifier().getPath().equals("shrubland")).orElse(false);

        // Block scan: 21x17x21 area, every other block
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        double fx = 0, fy = 0, fz = 0, wx = 0, wy = 0, wz = 0, ax = 0, ay = 0, az = 0;
        for (int dx = -10; dx <= 10; dx += 2) {
            for (int dz = -10; dz <= 10; dz += 2) {
                for (int dy = -6; dy <= 10; dy += 2) {
                    p.set(eye.getX() + dx, eye.getY() + dy, eye.getZ() + dz);
                    BlockState state = level.getBlockState(p);
                    if (state.isAir()) continue;
                    if (state.is(BlockTags.LEAVES)) s.leaves++;
                    else if (state.is(BlockTags.LOGS)) s.logs++;
                    else if (state.is(BlockTags.BASE_STONE_OVERWORLD)) s.stone++;
                    else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) s.grass++;
                    FluidState fluid = state.getFluidState();
                    if (fluid.is(FluidTags.WATER)) {
                        if (fluid.isSource()) { s.stillWater++; wx += p.getX(); wy += p.getY(); wz += p.getZ(); }
                        else {
                            s.flowingWater++; fx += p.getX(); fy += p.getY(); fz += p.getZ();
                            if (fluid.hasProperty(net.minecraft.world.level.material.FlowingFluid.FALLING)
                                    && fluid.getValue(net.minecraft.world.level.material.FlowingFluid.FALLING)) {
                                s.fallingWater++; ax += p.getX(); ay += p.getY(); az += p.getZ();
                            }
                        }
                    }
                }
            }
        }
        if (s.flowingWater > 0) s.flowingCentre = new Vec3(fx / s.flowingWater + .5, fy / s.flowingWater + .5, fz / s.flowingWater + .5);
        if (s.fallingWater > 0) s.fallingCentre = new Vec3(ax / s.fallingWater + .5, ay / s.fallingWater + .5, az / s.fallingWater + .5);
        if (s.stillWater > 0) s.waterCentre = new Vec3(wx / s.stillWater + .5, wy / s.stillWater + .5, wz / s.stillWater + .5);

        s.underground = s.overworld && s.skyLight < 0.2f && !level.canSeeSky(eye) && s.stone > 60 && s.y < s.seaLevel + 10;
        s.surface = (s.overworld && !s.underground || s.skylands) && s.skyLight > 0.4f;     // the Skylands is all surface
        return s;
    }
}

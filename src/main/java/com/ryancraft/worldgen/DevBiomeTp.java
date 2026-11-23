package com.ryancraft.worldgen;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

/** Development builds only: /rcbiometp <biome> teleports to the nearest one, for screenshot tests. */
public final class DevBiomeTp {
    public static void init() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        CommandRegistrationCallback.EVENT.register((d, ctx, env) -> d.register(Commands.literal("rcbiometp")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("biome", StringArgumentType.greedyString()).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException(); ServerLevel level = c.getSource().getLevel();
                    var key = ResourceKey.create(Registries.BIOME, Identifier.parse(StringArgumentType.getString(c, "biome")));
                    var found = level.findClosestBiome3d(h -> h.is(key), p.blockPosition(), 6400, 32, 64);
                    if (found == null) return 0;
                    BlockPos at = found.getFirst();
                    level.getChunk(at);
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at.getX(), at.getZ());
                    p.teleportTo(level, at.getX() + 0.5, y + 26, at.getZ() + 0.5, java.util.Set.of(), p.getYRot(), 38f, false);
                    return 1;
                }))));
    }
    private DevBiomeTp() {}
}

package com.ryancraft.pipes;

import com.mojang.serialization.Codec;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The price of an Ender Terminal: its end portal frame has to come out of a stronghold. Breaking any frame clears the
 * portal it was part of, and that stronghold is spent: Eyes of Ender lead to the next nearest one instead.
 */
public final class Strongholds {
    /** Start chunks of the strongholds whose portals have been broken up, per dimension. */
    public static final AttachmentType<List<Long>> SACRIFICED = AttachmentRegistry.create(RyanCraft.id("sacrificed_strongholds"),
            b -> b.persistent(Codec.LONG.listOf()).initializer(List::of));

    public static void init() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((d, ctx, env) -> d.register(
                net.minecraft.commands.Commands.literal("stronghold").requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                        .executes(c -> {
                            ServerLevel l = c.getSource().getLevel(); BlockPos from = BlockPos.containing(c.getSource().getPosition());
                            BlockPos p = nearestLiving(l, from);
                            if (p == null) p = l.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, from, 100, false);
                            BlockPos fp = p;
                            RyanCraft.LOGGER.info("STRONGHOLD eyes lead to {} ({} sacrificed)", fp, l.getAttachedOrCreate(SACRIFICED).size());
                            c.getSource().sendSuccess(() -> Component.literal("Eyes of Ender lead to " + (fp == null ? "nowhere" : fp.getX() + ", " + fp.getZ())
                                    + " (" + l.getAttachedOrCreate(SACRIFICED).size() + " sacrificed)"), false);
                            return 1; })
                        .then(net.minecraft.commands.Commands.literal("sacrifice").executes(c -> {     // spend the stronghold the eyes lead to now
                            ServerLevel l = c.getSource().getLevel(); BlockPos from = BlockPos.containing(c.getSource().getPosition());
                            BlockPos p = nearestLiving(l, from);
                            if (p == null) p = l.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, from, 100, false);
                            if (p == null) return 0;
                            List<Long> done = new ArrayList<>(l.getAttachedOrCreate(SACRIFICED)); done.add(new ChunkPos(p).toLong()); l.setAttached(SACRIFICED, done);
                            return 1; }))));
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, be) -> {
            if (!state.is(Blocks.END_PORTAL_FRAME) || !(level instanceof ServerLevel sl)) return;
            // vanilla gives the frame no loot table at all, so its drops are handled here: the frame itself with a
            // Silk Touch diamond (or netherite) pickaxe, and the eye back whenever there was one in it
            if (!player.isCreative()) {
                net.minecraft.world.item.ItemStack tool = player.getMainHandItem();
                boolean silk = (tool.is(net.minecraft.world.item.Items.DIAMOND_PICKAXE) || tool.is(net.minecraft.world.item.Items.NETHERITE_PICKAXE))
                        && net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                            sl.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), tool) > 0;
                if (silk) net.minecraft.world.level.block.Block.popResource(sl, pos, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.END_PORTAL_FRAME));
                if (state.getValue(net.minecraft.world.level.block.EndPortalFrameBlock.HAS_EYE))
                    net.minecraft.world.level.block.Block.popResource(sl, pos, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_EYE));
            }
            frameBroken(sl, pos, player instanceof ServerPlayer sp ? sp : null);
        });
    }

    static void frameBroken(ServerLevel level, BlockPos pos, @Nullable ServerPlayer player) {
        // the portal goes dark: clear the end portal blocks inside the frame
        ArrayDeque<BlockPos> q = new ArrayDeque<>(); Set<BlockPos> seen = new HashSet<>();
        for (Direction d : Direction.Plane.HORIZONTAL) q.add(pos.relative(d));
        boolean cleared = false;
        while (!q.isEmpty() && seen.size() < 64) {
            BlockPos p = q.poll();
            if (!seen.add(p) || !level.getBlockState(p).is(Blocks.END_PORTAL)) continue;
            level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState()); cleared = true;
            for (Direction d : Direction.Plane.HORIZONTAL) q.add(p.relative(d));
        }
        // and the stronghold is spent
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, StructureTags.EYE_OF_ENDER_LOCATED);
        if (start == null || !start.isValid()) return;
        long key = start.getChunkPos().toLong();
        List<Long> done = new ArrayList<>(level.getAttachedOrCreate(SACRIFICED));
        if (!done.contains(key)) {
            done.add(key); level.setAttached(SACRIFICED, done);
            level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.6f);
            if (player != null) player.displayClientMessage(Component.literal("The stronghold falls silent. Eyes of Ender will look elsewhere.")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
        } else if (cleared) level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6f, 0.8f);
    }

    /** Where an Eye of Ender should fly: the nearest stronghold that hasn't been sacrificed. Null means "ask vanilla". */
    public static @Nullable BlockPos nearestLiving(ServerLevel level, BlockPos from) {
        List<Long> done = level.getAttachedOrCreate(SACRIFICED);
        if (done.isEmpty()) return null;
        BlockPos best = null; double bestD = Double.MAX_VALUE; boolean any = false;
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (Holder<Structure> h : registry.getTagOrEmpty(StructureTags.EYE_OF_ENDER_LOCATED)) {
            for (StructurePlacement placement : level.getChunkSource().getGeneratorState().getPlacementsForStructure(h)) {
                if (!(placement instanceof ConcentricRingsStructurePlacement rings)) continue;
                any = true;
                for (ChunkPos cp : level.getChunkSource().getGeneratorState().getRingPositionsFor(rings)) {
                    if (done.contains(cp.toLong())) continue;
                    BlockPos p = placement.getLocatePos(cp);
                    double d = (p.getX() - from.getX()) * (double) (p.getX() - from.getX()) + (p.getZ() - from.getZ()) * (double) (p.getZ() - from.getZ());
                    if (d < bestD) { bestD = d; best = p; }
                }
            }
        }
        return any ? best : null;
    }

    private Strongholds() {}
}

package com.ryancraft.fish;

import com.mojang.serialization.Codec;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** The angler's side of the loop: every rod catch is called out, and each player's biggest fish per species is kept. */
public final class FishingLog {
    public static final AttachmentType<Map<String, Float>> BESTS = AttachmentRegistry.create(RyanCraft.id("fish_bests"),
            b -> b.persistent(Codec.unboundedMap(Codec.STRING, Codec.FLOAT)).copyOnDeath().initializer(HashMap::new));

    public static String inches(float l) { return String.format(java.util.Locale.ROOT, "%.1f in", l); }

    /** A fish just came out of the water on this player's line. */
    public static void caught(ServerPlayer player, ItemStack fish) {
        Float len = fish.get(FishSize.LENGTH);
        if (len == null) return;
        String id = BuiltInRegistries.ITEM.getKey(fish.getItem()).toString();
        Map<String, Float> bests = new HashMap<>(player.getAttachedOrCreate(BESTS));
        Float old = bests.get(id);
        boolean best = old == null || len > old, trophy = FishSize.isTrophy(fish);
        if (best) { bests.put(id, len); player.setAttached(BESTS, bests); }

        MutableComponent msg = fish.getHoverName().copy().withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" - " + inches(len)).withStyle(ChatFormatting.AQUA));
        if (FishSize.isLegendary(fish)) msg.append(Component.literal("  Legendary!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        else if (trophy) msg.append(Component.literal("  Trophy!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.displayClientMessage(msg, true);
        if (best && old != null)
            player.displayClientMessage(Component.literal("New personal best: ").withStyle(ChatFormatting.GREEN).append(fish.getHoverName())
                    .append(Component.literal(" " + inches(len) + " (was " + inches(old) + ")").withStyle(ChatFormatting.GRAY)), false);
        if (trophy) player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.3f);
        else if (best) player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    /** The Angler's Logbook: this player's biggest fish of each species. */
    public static void show(Player player) {
        Map<String, Float> bests = new TreeMap<>(player.getAttachedOrCreate(BESTS));
        player.displayClientMessage(Component.literal("Angler's Logbook").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE), false);
        if (bests.isEmpty()) { player.displayClientMessage(Component.literal("No fish yet. Go wet a line.").withStyle(ChatFormatting.GRAY), false); return; }
        bests.forEach((id, len) -> {
            Item item = BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(id));
            ItemStack s = new ItemStack(item); s.set(FishSize.LENGTH, len);
            MutableComponent line = Component.literal(" ").append(s.getHoverName()).append(Component.literal(" " + inches(len)).withStyle(ChatFormatting.AQUA));
            if (FishSize.isLegendary(s)) line.append(Component.literal(" **").withStyle(ChatFormatting.LIGHT_PURPLE));
            else if (FishSize.isTrophy(s)) line.append(Component.literal(" *").withStyle(ChatFormatting.GOLD));
            player.displayClientMessage(line, false);
        });
    }

    private FishingLog() {}
}

package com.ryancraft.season;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Seasons, Serene Seasons style. The year runs from world time: four seasons of three parts (early, mid, late),
 * {@link #SUB_DAYS} days each, starting in early spring. Temperate biomes get colder in winter (snow, frozen ponds,
 * crops stall outside greenhouses), their grass and leaves change colour, and our trees turn in autumn.
 */
public final class Seasons {
    public static final int SUB_DAYS = 8, YEAR = 12;
    public enum Season { SPRING, SUMMER, AUTUMN, WINTER;
        public String title() { return name().charAt(0) + name().substring(1).toLowerCase(); } }
    private static final String[] PHASE = {"Early", "Mid", "Late"};

    /** The part of the year (0 = early spring ... 11 = late winter), as last seen by a tick on either side. */
    public static volatile int current = 0;

    public static int sub(long dayTime) { return (int) Math.floorMod(Math.floorDiv(dayTime, 24000L) / SUB_DAYS, (long) YEAR); }
    public static int sub(Level level) { return sub(level.getDayTime()); }
    public static Season season(int sub) { return Season.values()[sub / 3]; }
    public static String name(int sub) { return PHASE[sub % 3] + " " + season(sub).title(); }
    public static int dayOf(long dayTime) { return (int) Math.floorMod(Math.floorDiv(dayTime, 24000L), (long) SUB_DAYS) + 1; }

    /** Temperate biomes feel the seasons; deserts, jungles and frozen places keep their climate. */
    public static boolean temperate(Biome b) { float t = b.getBaseTemperature(); return t > 0.15f && t <= 0.95f; }

    private static final float[] TEMP = {-0.25f, -0.15f, -0.05f, 0f, 0.1f, 0.05f, -0.05f, -0.2f, -0.4f, -0.7f, -0.8f, -0.6f};
    public static float temperatureOffset(Biome b) { return temperate(b) ? TEMP[current] : 0f; }

    // colour shifts (target colour, blend strength per part of the year)
    private static final float[] GRASS_K = {.1f, .15f, .05f, 0, 0, 0, .15f, .3f, .45f, .5f, .55f, .4f};
    private static final float[] LEAF_K = {.1f, .1f, 0, 0, 0, 0, .25f, .5f, .65f, .55f, .6f, .45f};
    private static int target(int sub, boolean grass) {
        return switch (season(sub)) { case SPRING -> 0x7acc48; case AUTUMN -> grass ? 0xb89a4a : 0xd0782a; case WINTER -> grass ? 0x8e8c6e : 0x8a7656; default -> 0; };
    }
    public static int grass(Biome b, int c) { return temperate(b) ? blend(c, target(current, true), GRASS_K[current]) : c; }
    public static int foliage(Biome b, int c) { return temperate(b) ? blend(c, target(current, false), LEAF_K[current]) : c; }
    private static int blend(int a, int b, float k) {
        if (k <= 0) return a;
        int r = (int) (((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k), g = (int) (((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k),
            bl = (int) ((a & 255) * (1 - k) + (b & 255) * k);
        return (a & 0xff000000) | (r << 16) | (g << 8) | bl;
    }

    /** How far our trees have turned: chance per random tick that a leaf block takes on this season's colour. */
    public static boolean autumnLeaves(int sub) { return sub >= 6; }
    public static float turnChance(int sub) {
        return switch (sub) { case 6 -> 0.06f; case 7 -> 0.3f; case 8 -> 0.8f; case 0 -> 0.25f; case 1 -> 0.7f; default -> 1f; };
    }

    /** Crops stall in winter unless they're under a roof (a greenhouse). */
    public static boolean cropsSleep(Level level, Biome b, boolean underRoof) {
        return season(current) == Season.WINTER && temperate(b) && !underRoof;
    }

    public static Item CALENDAR;

    public static void init() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id("season_calendar"));
        CALENDAR = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().stacksTo(1).setId(key)) {
            @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide()) player.displayClientMessage(describe(level.getDayTime()), true);
                return InteractionResult.SUCCESS;
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> e.accept(CALENDAR));

        ServerLifecycleEvents.SERVER_STARTED.register(s -> current = sub(s.overworld()));
        ServerTickEvents.END_SERVER_TICK.register(Seasons::tick);
        CommandRegistrationCallback.EVENT.register((d, ctx, env) -> d.register(Commands.literal("season")
                .executes(c -> { c.getSource().sendSuccess(() -> describe(c.getSource().getServer().overworld().getDayTime()), false); return 1; })
                .then(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("season", StringArgumentType.word())
                                .suggests((c, b) -> { for (Season s : Season.values()) b.suggest(s.name().toLowerCase()); return b.buildFuture(); })
                                .executes(c -> set(c.getSource().getServer(), StringArgumentType.getString(c, "season"), 1))
                                .then(Commands.argument("phase", StringArgumentType.word())
                                        .suggests((c, b) -> { b.suggest("early"); b.suggest("mid"); b.suggest("late"); return b.buildFuture(); })
                                        .executes(c -> set(c.getSource().getServer(), StringArgumentType.getString(c, "season"),
                                                switch (StringArgumentType.getString(c, "phase")) { case "early" -> 0; case "late" -> 2; default -> 1; })))))));
    }

    public static Component describe(long dayTime) {
        int s = sub(dayTime);
        ChatFormatting col = switch (season(s)) { case SPRING -> ChatFormatting.GREEN; case SUMMER -> ChatFormatting.YELLOW; case AUTUMN -> ChatFormatting.GOLD; case WINTER -> ChatFormatting.AQUA; };
        return Component.literal(name(s)).withStyle(col).append(Component.literal(" - day " + dayOf(dayTime) + " of " + SUB_DAYS).withStyle(ChatFormatting.GRAY));
    }

    private static int set(MinecraftServer server, String season, int phase) {
        Season s;
        try { s = Season.valueOf(season.toUpperCase()); } catch (IllegalArgumentException e) { return 0; }
        ServerLevel o = server.overworld();
        long t = o.getDayTime(), day = Math.floorDiv(t, 24000L), year = Math.floorDiv(day, (long) SUB_DAYS * YEAR);
        long target = (year * YEAR + s.ordinal() * 3L + phase) * SUB_DAYS;
        for (ServerLevel l : server.getAllLevels()) l.setDayTime(target * 24000L + Math.floorMod(t, 24000L));
        tick(server);
        return 1;
    }

    private static int announced = -1;
    private static void tick(MinecraftServer server) {
        int s = sub(server.overworld());
        current = s;
        if (s != announced) {
            if (announced != -1 && s % 3 == 0)
                server.getPlayerList().broadcastSystemMessage(Component.literal(season(s).title() + " has arrived.").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY), false);
            announced = s;
        }
    }

    private Seasons() {}
}

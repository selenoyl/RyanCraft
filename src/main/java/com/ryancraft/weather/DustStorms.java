package com.ryancraft.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Dust storms: a weather of their own, only over deserts and badlands. Rarer than thunderstorms: usually 5-10 days
 * clear, then a storm of 5-12 minutes. Thick sandy fog (rusty over the badlands), blowing dust and howling wind on the
 * client; thirst drains faster for anyone caught out in one. Kept with the overworld and sent to players.
 */
public final class DustStorms {
    public record State(boolean active, int timer) {
        static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.BOOL.fieldOf("active").forGetter(State::active), Codec.INT.fieldOf("timer").forGetter(State::timer)).apply(i, State::new));
    }
    public static final AttachmentType<State> STATE = AttachmentRegistry.create(RyanCraft.id("dust_storm"),
            b -> b.persistent(State.CODEC).initializer(() -> new State(false, 120000)));     // a new world starts with days of clear sky

    /** Server -> client: whether a dust storm is blowing. */
    public record Sync(boolean active) implements CustomPacketPayload {
        public static final Type<Sync> TYPE = new Type<>(RyanCraft.id("dust_storm"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Sync> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, Sync::active, Sync::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static boolean dusty(Holder<Biome> b) { return b.is(Biomes.DESERT) || b.is(BiomeTags.IS_BADLANDS); }
    public static boolean red(Holder<Biome> b) { return b.is(BiomeTags.IS_BADLANDS); }

    public static boolean active(MinecraftServer server) { return server.overworld().getAttachedOrCreate(STATE).active(); }

    /** Server: is this spot out in a dust storm right now? */
    public static boolean caughtIn(Level level, BlockPos pos) {
        return level instanceof ServerLevel sl && level.dimension() == Level.OVERWORLD && active(sl.getServer())
                && level.canSeeSky(pos) && dusty(level.getBiome(pos));
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(Sync.TYPE, Sync.CODEC);
        ServerPlayConnectionEvents.JOIN.register((h, sender, server) -> sender.sendPacket(new Sync(active(server))));
        ServerTickEvents.END_SERVER_TICK.register(DustStorms::tick);
        CommandRegistrationCallback.EVENT.register((d, ctx, env) -> d.register(Commands.literal("duststorm")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(c -> { c.getSource().sendSuccess(() -> Component.literal(active(c.getSource().getServer()) ? "A dust storm is blowing" : "No dust storm"), false); return 1; })
                .then(Commands.literal("start").executes(c -> { set(c.getSource().getServer(), true, 9000); return 1; }))
                .then(Commands.literal("stop").executes(c -> { set(c.getSource().getServer(), false, 0); return 1; }))));
        // /weather duststorm [duration]: joins vanilla's /weather (and /weather clear ends a storm: WeatherClearMixin)
        CommandRegistrationCallback.EVENT.register((d, ctx, env) -> d.register(Commands.literal("weather")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("duststorm")
                        .executes(c -> startCommand(c.getSource(), 9000))
                        .then(Commands.argument("duration", net.minecraft.commands.arguments.TimeArgument.time(1))
                                .executes(c -> startCommand(c.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "duration")))))));
    }

    private static void tick(MinecraftServer server) {
        ServerLevel o = server.overworld();
        State s = o.getAttachedOrCreate(STATE);
        int t = s.timer();
        if (t <= 0) {                                              // first run, or the weather changes
            boolean next = !s.active();
            set(server, next, next ? 6000 + o.getRandom().nextInt(8400) : 120000 + o.getRandom().nextInt(120000));
            return;
        }
        if (o.getGameTime() % 20 == 0) o.setAttached(STATE, new State(s.active(), t - 20));
    }

    private static int startCommand(net.minecraft.commands.CommandSourceStack src, int ticks) {
        set(src.getServer(), true, ticks);
        src.sendSuccess(() -> Component.literal("Set the weather to a dust storm (deserts and badlands)"), true);
        return ticks;
    }

    /** /weather clear also ends a dust storm. */
    public static void clearCommand(MinecraftServer server) { if (active(server)) set(server, false, 0); }

    private static void set(MinecraftServer server, boolean active, int timer) {
        ServerLevel o = server.overworld();
        boolean was = o.getAttachedOrCreate(STATE).active();
        o.setAttached(STATE, new State(active, timer <= 0 ? 120000 + o.getRandom().nextInt(120000) : timer));
        if (was != active) for (ServerPlayer p : server.getPlayerList().getPlayers()) ServerPlayNetworking.send(p, new Sync(active));
    }

    private DustStorms() {}
}

package com.ryancraft.thirst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ryancraft.RyanConfig;
import com.ryancraft.RyanCraft;
import com.ryancraft.RyanItems;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A thirst bar beside hunger. It drains a little faster than food, quicker when you sprint and in hot places (deserts,
 * badlands, the Nether, a hot summer). Drink from water with an empty hand, or drink a water bottle; milk, stews,
 * melons and berries give a little too. Run dry and you can't sprint, then you start taking damage, like starving.
 */
public final class Thirst {
    public static final int MAX = 20;

    public record Data(int level, float hydration, float exhaustion) {
        public static final Data FULL = new Data(MAX, 5f, 0f);
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("level").forGetter(Data::level), Codec.FLOAT.fieldOf("hydration").forGetter(Data::hydration),
                Codec.FLOAT.fieldOf("exhaustion").forGetter(Data::exhaustion)).apply(i, Data::new));
        public static final StreamCodec<ByteBuf, Data> STREAM = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::level, ByteBufCodecs.FLOAT, Data::hydration, ByteBufCodecs.FLOAT, Data::exhaustion, Data::new);
    }

    public static final AttachmentType<Data> DATA = AttachmentRegistry.create(RyanCraft.id("thirst"),
            b -> b.persistent(Data.CODEC).initializer(() -> Data.FULL).syncWith(Data.STREAM, AttachmentSyncPredicate.targetOnly()));
    public static final ResourceKey<DamageType> DEHYDRATION = ResourceKey.create(Registries.DAMAGE_TYPE, RyanCraft.id("dehydration"));

    /** Sent when a player with an empty hand uses water in front of them. */
    public record Drink() implements CustomPacketPayload {
        public static final Type<Drink> TYPE = new Type<>(RyanCraft.id("drink"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Drink> CODEC = StreamCodec.unit(new Drink());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** What drinking or eating something gives: thirst points and hydration (a buffer used up before the bar drops). */
    private record Quench(int points, float hydration) {}
    private static final Map<Item, Quench> FOODS = new HashMap<>();

    public static boolean enabled() { return RyanConfig.get().thirst; }

    public static Data get(net.minecraft.world.entity.player.Player p) { return p.getAttachedOrCreate(DATA); }

    public static void quench(ServerPlayer p, int points, float hydration) {
        Data d = get(p);
        int level = Math.min(MAX, d.level() + points);
        p.setAttached(DATA, new Data(level, Math.min(level, d.hydration() + hydration), d.exhaustion()));
    }

    /** Sips in a full water bottle; each use takes one, so you needn't drain a whole bottle at once. */
    public static final int SIPS = 3;
    private static final int[] SIP_POINTS = {0, 2, 2, 3};          // the first sip is the biggest; 7 points a bottle

    public static boolean isWater(ItemStack stack) {
        return stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }
    /** Sips left in a water bottle (kept in custom model data, which also picks the bottle's fill texture). */
    public static int sipsLeft(ItemStack stack) {
        net.minecraft.world.item.component.CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return cmd == null || cmd.floats().isEmpty() ? SIPS : Math.max(1, Math.min(SIPS, Math.round(cmd.floats().get(0))));
    }
    public static void setSipsLeft(ItemStack stack, int sips) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(java.util.List.of((float) sips), java.util.List.of(), java.util.List.of(), java.util.List.of()));
    }

    /** One sip from a water bottle (called on both sides; only the server quenches). */
    public static void sip(Level level, net.minecraft.world.entity.player.Player player, int sipsBefore) {
        if (player instanceof ServerPlayer sp && enabled()) quench(sp, SIP_POINTS[Math.max(1, Math.min(SIPS, sipsBefore))], 1.5f);
    }

    /** Server: something was just eaten or drunk (called before the stack is used up). */
    public static void consumed(ServerPlayer p, ItemStack stack) {
        if (!enabled()) return;
        if (stack.is(Items.POTION)) {
            if (isWater(stack)) quench(p, SIP_POINTS[sipsLeft(stack)], 1.5f); else quench(p, 3, 1f);
            return;
        }
        Quench q = FOODS.get(stack.getItem());
        if (q != null) quench(p, q.points(), q.hydration());
    }

    public static void init() {
        // drinks and juicy foods: thirst points, plus hydration (like hunger's saturation: it's used up before the bar drops)
        FOODS.put(Items.MILK_BUCKET, new Quench(6, 4f));
        FOODS.put(Items.HONEY_BOTTLE, new Quench(3, 1.5f));
        for (Item stew : new Item[]{Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW}) FOODS.put(stew, new Quench(5, 3f));
        FOODS.put(Items.MELON_SLICE, new Quench(2, 1.5f));
        FOODS.put(Items.GLISTERING_MELON_SLICE, new Quench(2, 1.5f));
        FOODS.put(Items.APPLE, new Quench(1, 1f));
        FOODS.put(Items.BEETROOT, new Quench(1, 0.5f));
        FOODS.put(Items.PUMPKIN_PIE, new Quench(1, 0.5f));
        FOODS.put(RyanItems.CHERRY_PIE, new Quench(1, 1f));
        FOODS.put(Items.GOLDEN_APPLE, new Quench(2, 1f));
        FOODS.put(Items.ENCHANTED_GOLDEN_APPLE, new Quench(6, 4f));
        FOODS.put(Items.SWEET_BERRIES, new Quench(1, 0f));
        FOODS.put(Items.GLOW_BERRIES, new Quench(1, 0f));
        FOODS.put(Items.CARROT, new Quench(1, 0f));
        FOODS.put(Items.CHORUS_FRUIT, new Quench(1, 0f));
        FOODS.put(RyanItems.CHERRIES, new Quench(1, 0.5f));

        PayloadTypeRegistry.playC2S().register(Drink.TYPE, Drink.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Drink.TYPE, (payload, ctx) -> drinkFromWater(ctx.player()));
        ServerTickEvents.END_SERVER_TICK.register(Thirst::tick);
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((d, ctx, env) -> d.register(
                net.minecraft.commands.Commands.literal("thirst")
                        .executes(c -> { ServerPlayer p = c.getSource().getPlayerOrException(); Data t = get(p);
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Thirst " + t.level() + "/" + MAX + ", hydration " + t.hydration()), false); return t.level(); })
                        .then(net.minecraft.commands.Commands.literal("set").requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                                .then(net.minecraft.commands.Commands.argument("level", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, MAX))
                                        .executes(c -> { ServerPlayer p = c.getSource().getPlayerOrException();
                                            p.setAttached(DATA, new Data(com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "level"), 0f, 0f)); return 1; })))));
    }

    private static final Map<UUID, Long> LAST_SIP = new HashMap<>();

    private static void drinkFromWater(ServerPlayer p) {
        if (!enabled() || !p.getMainHandItem().isEmpty() || get(p).level() >= MAX) return;
        long now = p.level().getGameTime();
        if (now - LAST_SIP.getOrDefault(p.getUUID(), -100L) < 10) return;
        HitResult hit = p.pick(p.blockInteractionRange(), 0f, true);
        if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = bh.getBlockPos();
        if (!p.level().getFluidState(pos).is(FluidTags.WATER)) return;
        LAST_SIP.put(p.getUUID(), now);
        quench(p, 2, 0.5f);
        ServerLevel level = p.level();
        level.playSound(null, p.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 0.95f + level.getRandom().nextFloat() * 0.1f);
        level.sendParticles(ParticleTypes.SPLASH, bh.getLocation().x, pos.getY() + 0.9, bh.getLocation().z, 6, 0.15, 0.05, 0.15, 0.1);
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
    }

    /** Exhaustion toward the next point lost; kept on the server only, so the bar syncs just when it changes. */
    private static final Map<UUID, Float> EXHAUSTION = new HashMap<>();

    private static void tick(MinecraftServer server) {
        if (!enabled()) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            Data d = get(p);
            int level = d.level(); float hyd = d.hydration();
            ServerLevel world = p.level();
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
                if (level < MAX && world.getGameTime() % 20 == 0) p.setAttached(DATA, new Data(level + 1, hyd, 0f));
                continue;
            }
            float rate = 0.0017f;                                   // about two minutes per point at rest: there, but not a chore
            if (p.isSprinting()) rate *= 2.5f;
            if (world.dimension() == Level.NETHER) rate *= 2f;
            else if (world.isBrightOutside() && world.canSeeSky(p.blockPosition())
                    && hot(world.getBiome(p.blockPosition()).value())) rate *= 1.5f;
            float ex = EXHAUSTION.getOrDefault(p.getUUID(), 0f) + rate;
            if (ex >= 4f) {
                ex -= 4f;
                if (hyd > 0) hyd = Math.max(0, hyd - 1); else level = Math.max(0, level - 1);
                p.setAttached(DATA, new Data(level, hyd, 0f));
            }
            EXHAUSTION.put(p.getUUID(), ex);
            if (level == 0 && world.getGameTime() % 80 == 0) {                // parched: hurts like starving
                Difficulty diff = world.getDifficulty();
                if (p.getHealth() > 10f || diff == Difficulty.HARD || (p.getHealth() > 1f && diff == Difficulty.NORMAL))
                    p.hurtServer(world, new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DEHYDRATION)), 1f);
            }
        }
    }

    /** Deserts, badlands, savanna, or a temperate biome in the height of summer. */
    private static boolean hot(net.minecraft.world.level.biome.Biome b) {
        return b.getBaseTemperature() + com.ryancraft.season.Seasons.temperatureOffset(b) > 0.95f;
    }

    private Thirst() {}
}

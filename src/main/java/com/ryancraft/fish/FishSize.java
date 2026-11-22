package com.ryancraft.fish;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ryancraft.RyanCraft;
import com.ryancraft.RyanItems;
import com.ryancraft.entity.KentuckyFish;
import com.ryancraft.entity.RyanEntities;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Fish come in lengths, like bucks come in racks. Every fish in the water has a length (it's drawn to size); catch it by rod,
 * bucket or spear and the fish keeps it, shown in inches on the item. The top few percent of each species are trophies.
 */
public final class FishSize {
    /**
     * Lengths in inches, true to life: most fish fall between {@code min} and {@code typical}; about 1 in 50 is a
     * trophy (typical up to the real-world record); about 1 in 1000 is a legend, up to 15% past the record.
     */
    public record Spec(float min, float typical, float record) {
        public float roll(RandomSource r) {
            float u = r.nextFloat(), l;
            if (u < 0.979f) l = min + (typical - min) * (float) Math.pow(r.nextFloat(), 1.3);
            else if (u < 0.999f) l = typical + (record - typical) * (float) Math.pow(r.nextFloat(), 1.5);
            else l = record * (1 + 0.15f * r.nextFloat());
            return Math.round(l * 4) / 4f;
        }
        public float average() { return min + (typical - min) * 0.43f; }
        public float trophy() { return typical; }
    }

    public static final DataComponentType<Float> LENGTH = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, RyanCraft.id("fish_length"),
            DataComponentType.<Float>builder().persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT).build());

    private static final Map<EntityType<?>, Spec> BY_TYPE = new HashMap<>();
    private static final Map<Item, Spec> BY_ITEM = new HashMap<>();

    private static void add(EntityType<?> type, Item raw, Item bucket, float min, float typical, float record) {
        Spec s = new Spec(min, typical, record); BY_TYPE.put(type, s); BY_ITEM.put(raw, s); BY_ITEM.put(bucket, s);
    }

    public static Spec spec(EntityType<?> type) { return BY_TYPE.getOrDefault(type, new Spec(6, 12, 16)); }
    public static Spec spec(Item item) { return BY_ITEM.get(item); }
    public static boolean isTrophy(ItemStack stack) {
        Float l = stack.get(LENGTH); Spec s = BY_ITEM.get(stack.getItem());
        return l != null && s != null && l > s.trophy();
    }
    /** Bigger than any fish of its kind ever really caught. */
    public static boolean isLegendary(ItemStack stack) {
        Float l = stack.get(LENGTH); Spec s = BY_ITEM.get(stack.getItem());
        return l != null && s != null && l > s.record();
    }

    /** Loot function "ryancraft:fish_length": the fish's own length when it drops from one, otherwise a fresh roll (rod catches). */
    public record LengthFunction() implements LootItemFunction {
        public static final MapCodec<LengthFunction> CODEC = MapCodec.unit(new LengthFunction());
        @Override public ItemStack apply(ItemStack stack, LootContext ctx) {
            Entity e = ctx.getOptionalParameter(LootContextParams.THIS_ENTITY);
            if (e instanceof KentuckyFish f) stack.set(LENGTH, f.length());
            else { Spec s = BY_ITEM.get(stack.getItem()); if (s != null) stack.set(LENGTH, s.roll(ctx.getRandom())); }
            // a rod catch: tell the angler what they landed, and keep their personal best
            if (e instanceof net.minecraft.world.entity.projectile.FishingHook hook && hook.getPlayerOwner() instanceof net.minecraft.server.level.ServerPlayer p)
                FishingLog.caught(p, stack);
            return stack;
        }
        @Override public LootItemFunctionType<LengthFunction> getType() { return TYPE; }
    }
    public static final LootItemFunctionType<LengthFunction> TYPE = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
            RyanCraft.id("fish_length"), new LootItemFunctionType<>(LengthFunction.CODEC));

    public static void init() {
        add(RyanEntities.BLUEGILL, RyanItems.BLUEGILL, RyanItems.BLUEGILL_BUCKET, 4, 9, 12);
        add(RyanEntities.CRAPPIE, RyanItems.CRAPPIE, RyanItems.CRAPPIE_BUCKET, 6, 13, 18);
        add(RyanEntities.LARGEMOUTH_BASS, RyanItems.LARGEMOUTH_BASS, RyanItems.LARGEMOUTH_BASS_BUCKET, 10, 20, 26);
        add(RyanEntities.CHANNEL_CATFISH, RyanItems.CHANNEL_CATFISH, RyanItems.CHANNEL_CATFISH_BUCKET, 12, 28, 40);
        add(RyanEntities.RAINBOW_TROUT, RyanItems.RAINBOW_TROUT, RyanItems.RAINBOW_TROUT_BUCKET, 8, 20, 30);
        add(RyanEntities.SMALLMOUTH_BASS, RyanItems.SMALLMOUTH_BASS, RyanItems.SMALLMOUTH_BASS_BUCKET, 8, 17, 23);
        add(RyanEntities.YELLOW_PERCH, RyanItems.YELLOW_PERCH, RyanItems.YELLOW_PERCH_BUCKET, 5, 11, 14);
        add(RyanEntities.WALLEYE, RyanItems.WALLEYE, RyanItems.WALLEYE_BUCKET, 12, 24, 31);
        add(RyanEntities.NORTHERN_PIKE, RyanItems.NORTHERN_PIKE, RyanItems.NORTHERN_PIKE_BUCKET, 16, 32, 46);
        add(RyanEntities.MUSKELLUNGE, RyanItems.MUSKELLUNGE, RyanItems.MUSKELLUNGE_BUCKET, 28, 44, 54);
        RyanFishing.init();
    }

    private FishSize() {}
}

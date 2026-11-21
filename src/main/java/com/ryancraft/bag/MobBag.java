package com.ryancraft.bag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/** The mobs riding in a bundle: their full saved data, their names (for the tooltip) and the bag's capacity. */
public record MobBag(List<CompoundTag> mobs, List<Component> names, int capacity) {
    public static final Codec<MobBag> CODEC = RecordCodecBuilder.create(i -> i.group(
            CompoundTag.CODEC.listOf().fieldOf("mobs").forGetter(MobBag::mobs),
            ComponentSerialization.CODEC.listOf().fieldOf("names").forGetter(MobBag::names),
            Codec.INT.fieldOf("capacity").forGetter(MobBag::capacity)).apply(i, MobBag::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobBag> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), MobBag::mobs,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), MobBag::names,
            ByteBufCodecs.VAR_INT, MobBag::capacity,
            MobBag::new);

    public int count() { return mobs.size(); }
    public boolean isFull() { return mobs.size() >= capacity; }
    public String typeId() { return mobs.isEmpty() ? "" : mobs.get(0).getStringOr("id", ""); }

    public MobBag with(CompoundTag mob, Component name, int cap) {
        List<CompoundTag> m = new ArrayList<>(mobs); m.add(mob);
        List<Component> n = new ArrayList<>(names); n.add(name);
        return new MobBag(List.copyOf(m), List.copyOf(n), cap);
    }

    /** Without the last mob (they come out in reverse order). */
    public MobBag withoutLast() {
        return new MobBag(List.copyOf(mobs.subList(0, mobs.size() - 1)), List.copyOf(names.subList(0, names.size() - 1)), capacity);
    }
}

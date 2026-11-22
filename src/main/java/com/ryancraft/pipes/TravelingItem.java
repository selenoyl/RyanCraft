package com.ryancraft.pipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * An item on its way through one pipe block: it comes in at the {@code from} face, reaches the centre at progress 0.5,
 * leaves through {@code to} at 1. Routed items (to and from an Ender Terminal) carry their remaining turns.
 */
public final class TravelingItem {
    public ItemStack stack;
    public float progress, speed;
    public Direction from;
    public Direction to;            // decided at the centre (null before)
    public List<Direction> route;   // remaining turns for routed items, else empty
    public boolean routed;

    public TravelingItem(ItemStack stack, Direction from, float speed) {
        this(stack, 0f, speed, from, null, new ArrayList<>(), false);
    }

    public TravelingItem(ItemStack stack, float progress, float speed, Direction from, Direction to, List<Direction> route, boolean routed) {
        this.stack = stack; this.progress = progress; this.speed = speed; this.from = from; this.to = to;
        this.route = new ArrayList<>(route); this.routed = routed;
    }

    public static final Codec<TravelingItem> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(t -> t.stack),
            Codec.FLOAT.fieldOf("progress").forGetter(t -> t.progress),
            Codec.FLOAT.fieldOf("speed").forGetter(t -> t.speed),
            Direction.CODEC.fieldOf("from").forGetter(t -> t.from),
            Direction.CODEC.optionalFieldOf("to").forGetter(t -> java.util.Optional.ofNullable(t.to)),
            Direction.CODEC.listOf().optionalFieldOf("route", List.of()).forGetter(t -> t.route),
            Codec.BOOL.optionalFieldOf("routed", false).forGetter(t -> t.routed)
    ).apply(i, (s, p, sp, f, to, r, ro) -> new TravelingItem(s, p, sp, f, to.orElse(null), r, ro)));

    /** Where it is inside the block (0..1 cube coordinates), for drawing. */
    public double[] position(float extra) {
        float p = Math.min(1f, progress + extra);
        double x = 0.5, y = 0.5, z = 0.5;
        if (p < 0.5f) { double d = 0.5 - p; x += from.getStepX() * d; y += from.getStepY() * d; z += from.getStepZ() * d; }
        else if (to != null) { double d = p - 0.5; x += to.getStepX() * d; y += to.getStepY() * d; z += to.getStepZ() * d; }
        return new double[]{x, y, z};
    }
}

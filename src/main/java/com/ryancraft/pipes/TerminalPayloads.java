package com.ryancraft.pipes;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** What a terminal's screen and the server say to each other. */
public final class TerminalPayloads {
    /** Server -> client: everything on the network (one stack per kind, with the true count alongside). */
    public record Contents(List<ItemStack> items, List<Long> counts) implements CustomPacketPayload {
        public static final Type<Contents> TYPE = new Type<>(RyanCraft.id("terminal_contents"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Contents> CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), Contents::items,
                ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), Contents::counts, Contents::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Client -> server: send me this many of this item. */
    public record Request(ItemStack item, int count) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(RyanCraft.id("terminal_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Request::item, ByteBufCodecs.VAR_INT, Request::count, Request::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Client -> server: put away what I'm holding on the cursor. */
    public record DepositCarried() implements CustomPacketPayload {
        public static final Type<DepositCarried> TYPE = new Type<>(RyanCraft.id("terminal_deposit"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DepositCarried> CODEC = StreamCodec.unit(new DepositCarried());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(Contents.TYPE, Contents.CODEC);
        PayloadTypeRegistry.playC2S().register(Request.TYPE, Request.CODEC);
        PayloadTypeRegistry.playC2S().register(DepositCarried.TYPE, DepositCarried.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Request.TYPE, (p, ctx) -> {
            if (ctx.player().containerMenu instanceof EnderTerminalMenu m && m.terminal != null && !p.item().isEmpty())
                m.terminal.request(ctx.player().level(), ItemVariant.of(p.item()), Math.max(1, Math.min(p.count(), 64)));
        });
        ServerPlayNetworking.registerGlobalReceiver(DepositCarried.TYPE, (p, ctx) -> {
            if (ctx.player().containerMenu instanceof EnderTerminalMenu m && m.terminal != null)
                m.setCarried(m.terminal.deposit(ctx.player().level(), m.getCarried()));
        });
    }

    private TerminalPayloads() {}
}

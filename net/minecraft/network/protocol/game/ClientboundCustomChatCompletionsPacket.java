/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.util.ByIdMap;

public record ClientboundCustomChatCompletionsPacket(Action action, List<String> entries) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<ByteBuf, ClientboundCustomChatCompletionsPacket> STREAM_CODEC = StreamCodec.composite(Action.STREAM_CODEC, ClientboundCustomChatCompletionsPacket::action, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ClientboundCustomChatCompletionsPacket::entries, ClientboundCustomChatCompletionsPacket::new);

    @Override
    public PacketType<ClientboundCustomChatCompletionsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleCustomChatCompletions(this);
    }

    public static enum Action {
        ADD(0),
        REMOVE(1),
        SET(2);

        private static final IntFunction<Action> BY_ID;
        public static final StreamCodec<ByteBuf, Action> STREAM_CODEC;
        private final int id;

        private Action(int id) {
            this.id = id;
        }

        static {
            BY_ID = ByIdMap.continuous(a -> a.id, Action.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, a -> a.id);
        }
    }
}


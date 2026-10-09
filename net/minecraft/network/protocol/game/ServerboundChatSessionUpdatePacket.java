/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<ByteBuf, ServerboundChatSessionUpdatePacket> STREAM_CODEC = RemoteChatSession.Data.STREAM_CODEC.map(ServerboundChatSessionUpdatePacket::new, ServerboundChatSessionUpdatePacket::chatSession);

    @Override
    public PacketType<ServerboundChatSessionUpdatePacket> type() {
        return GamePacketTypes.SERVERBOUND_CHAT_SESSION_UPDATE;
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        listener.handleChatSessionUpdate(this);
    }
}


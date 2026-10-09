/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<ByteBuf, ClientboundDeleteChatPacket> STREAM_CODEC = StreamCodec.composite(MessageSignature.Packed.STREAM_CODEC, ClientboundDeleteChatPacket::messageSignature, ClientboundDeleteChatPacket::new);

    @Override
    public PacketType<ClientboundDeleteChatPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DELETE_CHAT;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleDeleteChat(this);
    }
}


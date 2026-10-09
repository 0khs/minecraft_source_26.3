/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundChatCommandSignedPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<ByteBuf, ServerboundChatCommandSignedPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ServerboundChatCommandSignedPacket::command, ByteBufCodecs.INSTANT, ServerboundChatCommandSignedPacket::timeStamp, ByteBufCodecs.LONG, ServerboundChatCommandSignedPacket::salt, ArgumentSignatures.STREAM_CODEC, ServerboundChatCommandSignedPacket::argumentSignatures, LastSeenMessages.Update.STREAM_CODEC, ServerboundChatCommandSignedPacket::lastSeenMessages, ServerboundChatCommandSignedPacket::new);

    @Override
    public PacketType<ServerboundChatCommandSignedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHAT_COMMAND_SIGNED;
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        listener.handleSignedChatCommand(this);
    }
}


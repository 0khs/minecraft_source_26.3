/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundPunchPacket() implements Packet<ServerGamePacketListener>
{
    public static final ServerboundPunchPacket INSTANCE = new ServerboundPunchPacket();
    public static final StreamCodec<ByteBuf, ServerboundPunchPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public PacketType<ServerboundPunchPacket> type() {
        return GamePacketTypes.SERVERBOUND_PUNCH;
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        listener.handlePunch(this);
    }
}


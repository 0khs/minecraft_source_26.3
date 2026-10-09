/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundAcceptTeleportationPacket(int id, double x, double y, double z, float yRot, float xRot) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundAcceptTeleportationPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundAcceptTeleportationPacket::id, ByteBufCodecs.DOUBLE, ServerboundAcceptTeleportationPacket::x, ByteBufCodecs.DOUBLE, ServerboundAcceptTeleportationPacket::y, ByteBufCodecs.DOUBLE, ServerboundAcceptTeleportationPacket::z, ByteBufCodecs.FLOAT, ServerboundAcceptTeleportationPacket::yRot, ByteBufCodecs.FLOAT, ServerboundAcceptTeleportationPacket::xRot, ServerboundAcceptTeleportationPacket::new);

    @Override
    public PacketType<ServerboundAcceptTeleportationPacket> type() {
        return GamePacketTypes.SERVERBOUND_ACCEPT_TELEPORTATION;
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        listener.handleAcceptTeleportPacket(this);
    }
}


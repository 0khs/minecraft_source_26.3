/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundRespawnPacket(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRespawnPacket> STREAM_CODEC = StreamCodec.composite(CommonPlayerSpawnInfo.STREAM_CODEC, ClientboundRespawnPacket::commonPlayerSpawnInfo, ByteBufCodecs.BYTE, ClientboundRespawnPacket::dataToKeep, ClientboundRespawnPacket::new);
    public static final byte KEEP_ATTRIBUTE_MODIFIERS = 1;
    public static final byte KEEP_ENTITY_DATA = 2;
    public static final byte KEEP_ALL_DATA = 3;

    @Override
    public PacketType<ClientboundRespawnPacket> type() {
        return GamePacketTypes.CLIENTBOUND_RESPAWN;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleRespawn(this);
    }

    public boolean shouldKeep(byte mask) {
        return (this.dataToKeep & mask) != 0;
    }
}


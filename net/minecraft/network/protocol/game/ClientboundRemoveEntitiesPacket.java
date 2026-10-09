/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundRemoveEntitiesPacket(IntList entityIds) implements Packet<ClientGamePacketListener>
{
    private static final StreamCodec<ByteBuf, IntList> ID_LIST_STREAM_CODEC = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.collection(IntArrayList::new));
    public static final StreamCodec<ByteBuf, ClientboundRemoveEntitiesPacket> STREAM_CODEC = StreamCodec.composite(ID_LIST_STREAM_CODEC, ClientboundRemoveEntitiesPacket::entityIds, ClientboundRemoveEntitiesPacket::new);

    public ClientboundRemoveEntitiesPacket(int ... ids) {
        this(IntList.of((int[])ids));
    }

    @Override
    public PacketType<ClientboundRemoveEntitiesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleRemoveEntities(this);
    }
}


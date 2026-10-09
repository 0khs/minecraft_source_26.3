/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.entity.Entity;

public class ClientboundAnimatePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundAnimatePacket> STREAM_CODEC = Packet.codec(ClientboundAnimatePacket::write, ClientboundAnimatePacket::new);
    public static final int WAKE_UP = 0;
    public static final int CRITICAL_HIT = 1;
    public static final int MAGIC_CRITICAL_HIT = 2;
    private final int id;
    private final int action;

    public ClientboundAnimatePacket(Entity entity, int action) {
        this.id = entity.getId();
        this.action = action;
    }

    private ClientboundAnimatePacket(FriendlyByteBuf input) {
        this.id = input.readVarInt();
        this.action = input.readUnsignedByte();
    }

    private void write(FriendlyByteBuf output) {
        output.writeVarInt(this.id);
        output.writeByte(this.action);
    }

    @Override
    public PacketType<ClientboundAnimatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_ANIMATE;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleAnimate(this);
    }

    public int getId() {
        return this.id;
    }

    public int getAction() {
        return this.action;
    }
}


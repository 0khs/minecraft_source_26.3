/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.common;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.resources.Identifier;

public record ClientboundPostEffectsPacket(List<Identifier> postEffects) implements Packet<ClientCommonPacketListener>
{
    public static final StreamCodec<ByteBuf, ClientboundPostEffectsPacket> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundPostEffectsPacket::postEffects, ClientboundPostEffectsPacket::new);

    @Override
    public PacketType<ClientboundPostEffectsPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_POST_EFFECTS;
    }

    @Override
    public void handle(ClientCommonPacketListener listener) {
        listener.handlePostEffects(this);
    }
}


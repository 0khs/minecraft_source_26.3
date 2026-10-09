/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.SwingAnimation;

public record ClientboundSwingAnimationPacket(int entityId, InteractionHand hand, SwingAnimation animation) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<ByteBuf, ClientboundSwingAnimationPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundSwingAnimationPacket::entityId, InteractionHand.STREAM_CODEC, ClientboundSwingAnimationPacket::hand, SwingAnimation.STREAM_CODEC, ClientboundSwingAnimationPacket::animation, ClientboundSwingAnimationPacket::new);

    public ClientboundSwingAnimationPacket(Entity entity, InteractionHand hand, SwingAnimation animation) {
        this(entity.getId(), hand, animation);
    }

    @Override
    public PacketType<ClientboundSwingAnimationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SWING_ANIMATION;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleSwingAnimation(this);
    }
}


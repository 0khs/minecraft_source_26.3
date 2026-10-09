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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public record ServerboundUseItemOnPacket(InteractionHand hand, BlockHitResult hitResult, int sequence) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundUseItemOnPacket> STREAM_CODEC = StreamCodec.composite(InteractionHand.STREAM_CODEC, ServerboundUseItemOnPacket::hand, BlockHitResult.STREAM_CODEC, ServerboundUseItemOnPacket::hitResult, ByteBufCodecs.VAR_INT, ServerboundUseItemOnPacket::sequence, ServerboundUseItemOnPacket::new);

    @Override
    public PacketType<ServerboundUseItemOnPacket> type() {
        return GamePacketTypes.SERVERBOUND_USE_ITEM_ON;
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        listener.handleUseItemOn(this);
    }
}


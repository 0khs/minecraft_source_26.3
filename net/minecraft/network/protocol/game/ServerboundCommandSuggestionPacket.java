/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.SkipPacketDecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundCommandSuggestionPacket(int id, String command) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<ByteBuf, ServerboundCommandSuggestionPacket> STREAM_CODEC = ServerboundCommandSuggestionPacket.createStreamCodec(32500);
    public static final StreamCodec<ByteBuf, ServerboundCommandSuggestionPacket> CHAT_ONLY_STREAM_CODEC = new StreamCodec<ByteBuf, ServerboundCommandSuggestionPacket>(){
        private final StreamCodec<ByteBuf, ServerboundCommandSuggestionPacket> downstream = ServerboundCommandSuggestionPacket.createStreamCodec(256);

        @Override
        public ServerboundCommandSuggestionPacket decode(ByteBuf input) {
            try {
                return (ServerboundCommandSuggestionPacket)this.downstream.decode(input);
            }
            catch (DecoderException e) {
                throw new SkipPacketDecoderException(e);
            }
        }

        @Override
        public void encode(ByteBuf output, ServerboundCommandSuggestionPacket packet) {
            STREAM_CODEC.encode(output, packet);
        }
    };

    private static StreamCodec<ByteBuf, ServerboundCommandSuggestionPacket> createStreamCodec(int maxLength) {
        return StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundCommandSuggestionPacket::id, ByteBufCodecs.stringUtf8(maxLength), ServerboundCommandSuggestionPacket::command, ServerboundCommandSuggestionPacket::new);
    }

    @Override
    public PacketType<ServerboundCommandSuggestionPacket> type() {
        return GamePacketTypes.SERVERBOUND_COMMAND_SUGGESTION;
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        listener.handleCustomCommandSuggestions(this);
    }
}


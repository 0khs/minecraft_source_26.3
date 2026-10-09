/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {
    public static final Codec<LastSeenMessages> CODEC = MessageSignature.CODEC.listOf().xmap(LastSeenMessages::new, LastSeenMessages::entries);
    public static final LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

    public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
        output.update(Ints.toByteArray((int)this.entries.size()));
        for (MessageSignature entry : this.entries) {
            output.update(entry.bytes());
        }
    }

    public Packed pack(MessageSignatureCache cache) {
        return new Packed(this.entries.stream().map(entry -> entry.pack(cache)).toList());
    }

    public byte computeChecksum() {
        int checksum = 1;
        for (MessageSignature entry : this.entries) {
            checksum = 31 * checksum + entry.checksum();
        }
        byte checksumByte = (byte)checksum;
        return checksumByte == 0 ? (byte)1 : checksumByte;
    }

    public record Packed(List<MessageSignature.Packed> entries) {
        public static final Packed EMPTY = new Packed(List.of());
        public static final StreamCodec<ByteBuf, Packed> STREAM_CODEC = StreamCodec.composite(MessageSignature.Packed.STREAM_CODEC.apply(ByteBufCodecs.list(20)), Packed::entries, Packed::new);

        public Optional<LastSeenMessages> unpack(MessageSignatureCache cache) {
            ArrayList<MessageSignature> unpacked = new ArrayList<MessageSignature>(this.entries.size());
            for (MessageSignature.Packed packed : this.entries) {
                Optional<MessageSignature> entry = packed.unpack(cache);
                if (entry.isEmpty()) {
                    return Optional.empty();
                }
                unpacked.add(entry.get());
            }
            return Optional.of(new LastSeenMessages(unpacked));
        }
    }

    public record Update(int offset, BitSet acknowledged, byte checksum) {
        public static final byte IGNORE_CHECKSUM = 0;
        public static final StreamCodec<ByteBuf, Update> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Update::offset, ByteBufCodecs.fixedBitSet(20), Update::acknowledged, ByteBufCodecs.BYTE, Update::checksum, Update::new);

        public boolean verifyChecksum(LastSeenMessages lastSeen) {
            return this.checksum == 0 || this.checksum == lastSeen.computeChecksum();
        }
    }
}


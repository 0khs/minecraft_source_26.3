/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public record ArgumentSignatures(List<Entry> entries) {
    public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;
    public static final StreamCodec<ByteBuf, ArgumentSignatures> STREAM_CODEC = StreamCodec.composite(Entry.STREAM_CODEC.apply(ByteBufCodecs.list(8)), ArgumentSignatures::entries, ArgumentSignatures::new);

    public static ArgumentSignatures signCommand(SignableCommand<?> command, Signer signer) {
        List<Entry> entries = command.arguments().stream().map(argument -> {
            MessageSignature signature = signer.sign(argument.value());
            if (signature != null) {
                return new Entry(argument.name(), signature);
            }
            return null;
        }).filter(Objects::nonNull).toList();
        return new ArgumentSignatures(entries);
    }

    @FunctionalInterface
    public static interface Signer {
        public @Nullable MessageSignature sign(String var1);
    }

    public record Entry(String name, MessageSignature signature) {
        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(16), Entry::name, MessageSignature.STREAM_CODEC, Entry::signature, Entry::new);
    }
}


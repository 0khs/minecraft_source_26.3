/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.PositionStep;
import net.minecraft.world.phys.Vec3;

public sealed interface PositionPath {
    public static final StreamCodec<ByteBuf, PositionPath> STREAM_CODEC = Type.STREAM_CODEC.dispatch(PositionPath::type, Type::streamCodec);

    public Vec3 endPosition();

    public Type type();

    public static PositionPath of(Vec3 position) {
        return new Linear(position);
    }

    public static PositionPath stepped(List<PositionStep> steps) {
        return new Stepped(steps);
    }

    public record Linear(Vec3 endPosition) implements PositionPath
    {
        public static final StreamCodec<ByteBuf, Linear> STREAM_CODEC = Vec3.STREAM_CODEC.map(Linear::new, Linear::endPosition);

        @Override
        public Type type() {
            return Type.LINEAR;
        }
    }

    public record Stepped(Vec3 endPosition, List<PositionStep> steps) implements PositionPath
    {
        public static final StreamCodec<ByteBuf, Stepped> STREAM_CODEC = PositionStep.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Stepped::new, Stepped::steps);

        public Stepped(List<PositionStep> steps) {
            this(steps.getLast().position(), steps);
        }

        @Override
        public Type type() {
            return Type.STEPPED;
        }
    }

    public static enum Type {
        LINEAR(Linear.STREAM_CODEC),
        STEPPED(Stepped.STREAM_CODEC);

        public static final IntFunction<Type> BY_ID;
        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC;
        private final StreamCodec<ByteBuf, ? extends PositionPath> streamCodec;

        private Type(StreamCodec<ByteBuf, ? extends PositionPath> streamCodec) {
            this.streamCodec = streamCodec;
        }

        public StreamCodec<ByteBuf, ? extends PositionPath> streamCodec() {
            return this.streamCodec;
        }

        static {
            BY_ID = ByIdMap.continuous(Enum::ordinal, Type.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
        }
    }
}


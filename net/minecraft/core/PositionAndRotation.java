/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public interface PositionAndRotation {
    public static final StreamCodec<ByteBuf, PositionAndRotation> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, PositionAndRotation::position, ByteBufCodecs.FLOAT, PositionAndRotation::yRot, ByteBufCodecs.FLOAT, PositionAndRotation::xRot, PositionAndRotation::of);

    public Vec3 position();

    public float yRot();

    public float xRot();

    public PositionAndRotation immutable();

    default public boolean is(Vec3 position, float yRot, float xRot) {
        return this.yRot() == yRot && this.xRot() == xRot && Objects.equals(this.position(), position);
    }

    public static PositionAndRotation of(Vec3 position, float yRot, float xRot) {
        return new Immutable(position, yRot, xRot);
    }

    public record Immutable(Vec3 position, float yRot, float xRot) implements PositionAndRotation
    {
        @Override
        public PositionAndRotation immutable() {
            return this;
        }
    }

    public static class Mutable
    implements PositionAndRotation {
        private Vec3 position = Vec3.ZERO;
        private float yRot;
        private float xRot;

        @Override
        public Vec3 position() {
            return this.position;
        }

        @Override
        public float yRot() {
            return this.yRot;
        }

        @Override
        public float xRot() {
            return this.xRot;
        }

        @Override
        public PositionAndRotation immutable() {
            return PositionAndRotation.of(this.position, this.yRot, this.xRot);
        }

        public void set(Vec3 position, float yRot, float xRot) {
            this.position = position;
            this.yRot = yRot;
            this.xRot = xRot;
        }

        public void addDelta(Vec3 delta) {
            this.position = this.position.add(delta);
        }

        public void addRotation(float yRot, float xRot) {
            this.yRot += yRot;
            this.xRot += xRot;
        }
    }
}


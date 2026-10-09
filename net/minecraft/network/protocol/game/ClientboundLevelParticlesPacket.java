/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.util.ByIdMap;

public record ClientboundLevelParticlesPacket(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, float xDist, float yDist, float zDist, float xMaxSpeed, float yMaxSpeed, float zMaxSpeed, int count, RandomizationType randomizationType) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelParticlesPacket> STREAM_CODEC = StreamCodec.composite(ParticleTypes.STREAM_CODEC, ClientboundLevelParticlesPacket::particle, ByteBufCodecs.BOOL, ClientboundLevelParticlesPacket::overrideLimiter, ByteBufCodecs.BOOL, ClientboundLevelParticlesPacket::alwaysShow, ByteBufCodecs.DOUBLE, ClientboundLevelParticlesPacket::x, ByteBufCodecs.DOUBLE, ClientboundLevelParticlesPacket::y, ByteBufCodecs.DOUBLE, ClientboundLevelParticlesPacket::z, ByteBufCodecs.FLOAT, ClientboundLevelParticlesPacket::xDist, ByteBufCodecs.FLOAT, ClientboundLevelParticlesPacket::yDist, ByteBufCodecs.FLOAT, ClientboundLevelParticlesPacket::zDist, ByteBufCodecs.FLOAT, ClientboundLevelParticlesPacket::xMaxSpeed, ByteBufCodecs.FLOAT, ClientboundLevelParticlesPacket::yMaxSpeed, ByteBufCodecs.FLOAT, ClientboundLevelParticlesPacket::zMaxSpeed, ByteBufCodecs.VAR_INT, ClientboundLevelParticlesPacket::count, RandomizationType.STREAM_CODEC, ClientboundLevelParticlesPacket::randomizationType, ClientboundLevelParticlesPacket::new);

    public ClientboundLevelParticlesPacket(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, float xDist, float yDist, float zDist, float maxSpeed, int count) {
        this(particle, overrideLimiter, alwaysShow, x, y, z, xDist, yDist, zDist, maxSpeed, maxSpeed, maxSpeed, count, RandomizationType.DEFAULT);
    }

    @Override
    public PacketType<ClientboundLevelParticlesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleParticleEvent(this);
    }

    public static enum RandomizationType {
        DEFAULT(0),
        ALTERNATIVE(1),
        ALTERNATIVE_WITH_SPEED(2);

        private static final IntFunction<RandomizationType> BY_ID;
        public static final StreamCodec<ByteBuf, RandomizationType> STREAM_CODEC;
        private final int id;

        private RandomizationType(int id) {
            this.id = id;
        }

        public boolean isAlternative() {
            return this != DEFAULT;
        }

        static {
            BY_ID = ByIdMap.continuous(h -> h.id, RandomizationType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, h -> h.id);
        }
    }
}


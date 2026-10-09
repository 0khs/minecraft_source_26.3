/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.BitSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

public final class ClientboundLevelChunkWithLightPacket
extends Record
implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;
    private final ClientboundLevelChunkPacketData chunkData;
    private final ClientboundLightUpdatePacketData lightData;
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelChunkWithLightPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, ClientboundLevelChunkWithLightPacket::x, ByteBufCodecs.INT, ClientboundLevelChunkWithLightPacket::z, ClientboundLevelChunkPacketData.STREAM_CODEC, ClientboundLevelChunkWithLightPacket::chunkData, ClientboundLightUpdatePacketData.STREAM_CODEC, ClientboundLevelChunkWithLightPacket::lightData, ClientboundLevelChunkWithLightPacket::new);

    public ClientboundLevelChunkWithLightPacket(LevelChunk levelChunk, LevelLightEngine lightEngine, @Nullable BitSet skyChangedLightSectionFilter, @Nullable BitSet blockChangedLightSectionFilter) {
        ChunkPos chunkPos = levelChunk.getPos();
        this(chunkPos.x(), chunkPos.z(), new ClientboundLevelChunkPacketData(levelChunk), new ClientboundLightUpdatePacketData(chunkPos, lightEngine, skyChangedLightSectionFilter, blockChangedLightSectionFilter));
    }

    public ClientboundLevelChunkWithLightPacket(int x, int z, ClientboundLevelChunkPacketData chunkData, ClientboundLightUpdatePacketData lightData) {
        this.x = x;
        this.z = z;
        this.chunkData = chunkData;
        this.lightData = lightData;
    }

    @Override
    public PacketType<ClientboundLevelChunkWithLightPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleLevelChunkWithLight(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ClientboundLevelChunkWithLightPacket.class, "x;z;chunkData;lightData", "x", "z", "chunkData", "lightData"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ClientboundLevelChunkWithLightPacket.class, "x;z;chunkData;lightData", "x", "z", "chunkData", "lightData"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ClientboundLevelChunkWithLightPacket.class, "x;z;chunkData;lightData", "x", "z", "chunkData", "lightData"}, this, o);
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

    public ClientboundLevelChunkPacketData chunkData() {
        return this.chunkData;
    }

    public ClientboundLightUpdatePacketData lightData() {
        return this.lightData;
    }
}


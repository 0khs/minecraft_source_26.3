/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

public record ClientboundLightUpdatePacketData(BitSet skyYMask, BitSet blockYMask, BitSet emptySkyYMask, BitSet emptyBlockYMask, List<byte[]> skyUpdates, List<byte[]> blockUpdates) {
    private static final StreamCodec<ByteBuf, byte[]> DATA_LAYER_STREAM_CODEC = ByteBufCodecs.byteArray(2048);
    public static final StreamCodec<ByteBuf, ClientboundLightUpdatePacketData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BIT_SET, ClientboundLightUpdatePacketData::skyYMask, ByteBufCodecs.BIT_SET, ClientboundLightUpdatePacketData::blockYMask, ByteBufCodecs.BIT_SET, ClientboundLightUpdatePacketData::emptySkyYMask, ByteBufCodecs.BIT_SET, ClientboundLightUpdatePacketData::emptyBlockYMask, DATA_LAYER_STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundLightUpdatePacketData::skyUpdates, DATA_LAYER_STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundLightUpdatePacketData::blockUpdates, ClientboundLightUpdatePacketData::new);

    public ClientboundLightUpdatePacketData(ChunkPos chunkPos, LevelLightEngine lightEngine, @Nullable BitSet skyChangedLightSectionFilter, @Nullable BitSet blockChangedLightSectionFilter) {
        this(new BitSet(), new BitSet(), new BitSet(), new BitSet(), new ArrayList<byte[]>(), new ArrayList<byte[]>());
        for (int sectionIndex = 0; sectionIndex < lightEngine.getLightSectionCount(); ++sectionIndex) {
            if (skyChangedLightSectionFilter == null || skyChangedLightSectionFilter.get(sectionIndex)) {
                this.prepareSectionData(chunkPos, lightEngine, LightLayer.SKY, sectionIndex, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }
            if (blockChangedLightSectionFilter != null && !blockChangedLightSectionFilter.get(sectionIndex)) continue;
            this.prepareSectionData(chunkPos, lightEngine, LightLayer.BLOCK, sectionIndex, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
        }
    }

    private void prepareSectionData(ChunkPos pos, LevelLightEngine lightEngine, LightLayer layer, int sectionIndex, BitSet mask, BitSet emptyMask, List<byte[]> updates) {
        DataLayer data = lightEngine.getLayerListener(layer).getDataLayerData(SectionPos.of(pos, lightEngine.getMinLightSection() + sectionIndex));
        if (data != null) {
            if (data.isEmpty()) {
                emptyMask.set(sectionIndex);
            } else {
                mask.set(sectionIndex);
                updates.add(data.copy().getData());
            }
        }
    }
}


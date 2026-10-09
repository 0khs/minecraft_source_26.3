/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

public class ClientboundLevelChunkPacketData {
    private static final StreamCodec<ByteBuf, Map<Heightmap.Types, long[]>> HEIGHTMAPS_STREAM_CODEC = ByteBufCodecs.map(size -> new EnumMap(Heightmap.Types.class), Heightmap.Types.STREAM_CODEC, ByteBufCodecs.LONG_ARRAY);
    private static final int TWO_MEGABYTES = 0x200000;
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelChunkPacketData> STREAM_CODEC = StreamCodec.composite(HEIGHTMAPS_STREAM_CODEC, p -> p.heightmaps, ByteBufCodecs.byteArray(0x200000), p -> p.buffer, BlockEntityInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), p -> p.blockEntitiesData, ClientboundLevelChunkPacketData::new);
    private final Map<Heightmap.Types, long[]> heightmaps;
    private final byte[] buffer;
    private final List<BlockEntityInfo> blockEntitiesData;

    public ClientboundLevelChunkPacketData(LevelChunk levelChunk) {
        this.heightmaps = levelChunk.getHeightmaps().stream().filter(entry -> ((Heightmap.Types)entry.getKey()).sendToClient()).collect(Collectors.toMap(Map.Entry::getKey, entry -> (long[])((Heightmap)entry.getValue()).getRawData().clone()));
        this.buffer = new byte[ClientboundLevelChunkPacketData.calculateChunkSize(levelChunk)];
        ClientboundLevelChunkPacketData.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
        this.blockEntitiesData = Lists.newArrayList();
        for (Map.Entry<BlockPos, BlockEntity> entry2 : levelChunk.getBlockEntities().entrySet()) {
            this.blockEntitiesData.add(BlockEntityInfo.create(entry2.getValue()));
        }
    }

    private ClientboundLevelChunkPacketData(Map<Heightmap.Types, long[]> heightmaps, byte[] buffer, List<BlockEntityInfo> blockEntitiesData) {
        this.heightmaps = heightmaps;
        this.buffer = buffer;
        this.blockEntitiesData = blockEntitiesData;
    }

    private static int calculateChunkSize(LevelChunk chunk) {
        int total = 0;
        for (LevelChunkSection section : chunk.getSections()) {
            total += section.getSerializedSize();
        }
        return total;
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf buffer = Unpooled.wrappedBuffer((byte[])this.buffer);
        buffer.writerIndex(0);
        return buffer;
    }

    public static void extractChunkData(FriendlyByteBuf buffer, LevelChunk chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            section.write(buffer);
        }
        if (buffer.writerIndex() != buffer.capacity()) {
            throw new IllegalStateException("Didn't fill chunk buffer: expected " + buffer.capacity() + " bytes, got " + buffer.writerIndex());
        }
    }

    public void forEachBlockEntityTag(int chunkX, int chunkZ, BlockEntityTagOutput output) {
        int baseX = 16 * chunkX;
        int baseZ = 16 * chunkZ;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (BlockEntityInfo data : this.blockEntitiesData) {
            int unpackedX = baseX + SectionPos.sectionRelative(data.packedXZ >> 4);
            int unpackedZ = baseZ + SectionPos.sectionRelative(data.packedXZ);
            pos.set(unpackedX, data.y, unpackedZ);
            output.accept(pos, data.type, data.tag.orElse(null));
        }
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.buffer));
    }

    public Map<Heightmap.Types, long[]> getHeightmaps() {
        return this.heightmaps;
    }

    private record BlockEntityInfo(byte packedXZ, short y, BlockEntityType<?> type, Optional<CompoundTag> tag) {
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityInfo> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE, BlockEntityInfo::packedXZ, ByteBufCodecs.SHORT, BlockEntityInfo::y, ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE), BlockEntityInfo::type, ByteBufCodecs.OPTIONAL_COMPOUND_TAG, BlockEntityInfo::tag, BlockEntityInfo::new);

        private static BlockEntityInfo create(BlockEntity blockEntity) {
            CompoundTag tag = blockEntity.getUpdateTag(blockEntity.getLevel().registryAccess());
            BlockPos pos = blockEntity.getBlockPos();
            int xz = SectionPos.sectionRelative(pos.getX()) << 4 | SectionPos.sectionRelative(pos.getZ());
            return new BlockEntityInfo((byte)xz, (short)pos.getY(), blockEntity.getType(), tag.isEmpty() ? Optional.empty() : Optional.of(tag));
        }
    }

    @FunctionalInterface
    public static interface BlockEntityTagOutput {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
    }
}


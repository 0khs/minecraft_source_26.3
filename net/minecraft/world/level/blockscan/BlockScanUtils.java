/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.blockscan;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.blockscan.BlockStateConsumer;
import net.minecraft.world.level.blockscan.FilteredSectionCache;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class BlockScanUtils {
    static boolean findBlocks(LevelReader level, BlockPos from, BlockPos to, Predicate<BlockState> predicate, BlockStateConsumer consumer) {
        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY());
        int maxZ = Math.max(from.getZ(), to.getZ());
        return BlockScanUtils.sectionBasedScan(level, minX, minY, minZ, maxX, maxY, maxZ, predicate, consumer);
    }

    private static boolean sectionBasedScan(LevelReader level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Predicate<BlockState> predicate, BlockStateConsumer consumer) {
        boolean aboveWorld;
        int minSectionX = SectionPos.blockToSectionCoord(minX);
        int minSectionY = Math.max(level.getMinSectionY(), SectionPos.blockToSectionCoord(minY));
        int minSectionZ = SectionPos.blockToSectionCoord(minZ);
        int maxSectionX = SectionPos.blockToSectionCoord(maxX);
        int maxSectionY = Math.min(level.getMaxSectionY(), SectionPos.blockToSectionCoord(maxY));
        int maxSectionZ = SectionPos.blockToSectionCoord(maxZ);
        for (int x = minSectionX; x <= maxSectionX; ++x) {
            for (int z = minSectionZ; z <= maxSectionZ; ++z) {
                ChunkAccess chunk = level.getChunk(x, z);
                for (int y = minSectionY; y <= maxSectionY; ++y) {
                    int toZ;
                    int toY;
                    int toX;
                    int fromZ;
                    int fromY;
                    int fromX;
                    int sectionOriginZ;
                    int sectionOriginY;
                    int sectionOriginX;
                    BlockPos origin;
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(y));
                    if (!section.maybeHas(predicate) || !BlockScanUtils.findBlocksInSection(section, origin = new BlockPos(sectionOriginX = SectionPos.sectionToBlockCoord(x), sectionOriginY = SectionPos.sectionToBlockCoord(y), sectionOriginZ = SectionPos.sectionToBlockCoord(z)), fromX = SectionPos.sectionRelative(Math.max(sectionOriginX, minX)), fromY = SectionPos.sectionRelative(Math.max(sectionOriginY, minY)), fromZ = SectionPos.sectionRelative(Math.max(sectionOriginZ, minZ)), toX = SectionPos.sectionRelative(Math.min(SectionPos.sectionToBlockCoord(x, 15), maxX)), toY = SectionPos.sectionRelative(Math.min(SectionPos.sectionToBlockCoord(y, 15), maxY)), toZ = SectionPos.sectionRelative(Math.min(SectionPos.sectionToBlockCoord(z, 15), maxZ)), predicate, consumer)) continue;
                    return true;
                }
            }
        }
        boolean belowWorld = minY < level.getMinY();
        boolean bl = aboveWorld = maxY > level.getMaxY();
        if ((belowWorld || aboveWorld) && predicate.test(Blocks.AIR.defaultBlockState())) {
            if (belowWorld) {
                for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, level.getMinY() - 1, maxZ)) {
                    if (!consumer.apply(pos, Blocks.AIR.defaultBlockState()).shouldAbort()) continue;
                    return true;
                }
            }
            if (aboveWorld) {
                for (BlockPos pos : BlockPos.betweenClosed(minX, level.getMaxY() + 1, minZ, maxX, maxY, maxZ)) {
                    if (!consumer.apply(pos, Blocks.AIR.defaultBlockState()).shouldAbort()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean findBlocksInSection(LevelChunkSection section, BlockPos origin, int fromX, int fromY, int fromZ, int toX, int toY, int toZ, Predicate<BlockState> predicate, BlockStateConsumer consumer) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int y = fromY; y <= toY; ++y) {
            for (int z = fromZ; z <= toZ; ++z) {
                for (int x = fromX; x <= toX; ++x) {
                    BlockState blockState = section.getBlockState(x, y, z);
                    if (!predicate.test(blockState)) continue;
                    mutablePos.setWithOffset(origin, x, y, z);
                    if (!consumer.apply(mutablePos, blockState).shouldAbort()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    static boolean findBlocksWithCache(LevelReader level, Iterable<BlockPos> positions, Predicate<BlockState> predicate, BlockStateConsumer consumer) {
        FilteredSectionCache sectionCache = new FilteredSectionCache(level, predicate);
        for (BlockPos pos : positions) {
            BlockState state = sectionCache.getBlockState(pos);
            if (state == null || !consumer.apply(pos, state).shouldAbort()) continue;
            return true;
        }
        return false;
    }
}


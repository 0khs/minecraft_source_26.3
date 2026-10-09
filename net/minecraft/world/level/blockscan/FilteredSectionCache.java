/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.blockscan;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jspecify.annotations.Nullable;

public class FilteredSectionCache {
    private final LevelReader level;
    private final @Nullable CacheEntry[] sectionCache;
    private final Predicate<BlockState> statePredicate;

    public FilteredSectionCache(LevelReader level, Predicate<BlockState> statePredicate) {
        this.level = level;
        this.statePredicate = statePredicate;
        this.sectionCache = new CacheEntry[8];
    }

    public @Nullable BlockState getBlockState(BlockPos pos) {
        int relativeZ;
        int relativeY;
        int sectionX = SectionPos.blockToSectionCoord(pos.getX());
        int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        int sectionZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (sectionY < this.level.getMinSectionY() || sectionY > this.level.getMaxSectionY()) {
            BlockState air = Blocks.AIR.defaultBlockState();
            return this.statePredicate.test(air) ? air : null;
        }
        LevelChunkSection section = this.getOrLoad(sectionX, sectionY, sectionZ);
        if (section == null) {
            return null;
        }
        int relativeX = SectionPos.sectionRelative(pos.getX());
        BlockState blockState = section.getBlockState(relativeX, relativeY = SectionPos.sectionRelative(pos.getY()), relativeZ = SectionPos.sectionRelative(pos.getZ()));
        return this.statePredicate.test(blockState) ? blockState : null;
    }

    private @Nullable LevelChunkSection getOrLoad(int sectionX, int sectionY, int sectionZ) {
        int hash = this.getHash(sectionX, sectionY, sectionZ);
        CacheEntry cacheEntry = this.sectionCache[hash];
        if (cacheEntry == null || cacheEntry.sectionX != sectionX || cacheEntry.sectionY != sectionY || cacheEntry.sectionZ != sectionZ) {
            ChunkAccess chunk = this.level.getChunk(sectionX, sectionZ);
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
            this.sectionCache[hash] = cacheEntry = new CacheEntry(section.maybeHas(this.statePredicate) ? section : null, sectionX, sectionY, sectionZ);
        }
        return cacheEntry.chunkSection;
    }

    private int getHash(int sectionX, int sectionY, int sectionZ) {
        int relativeX = sectionX & 1;
        int relativeY = sectionY & 1;
        int relativeZ = sectionZ & 1;
        return ((relativeZ << 1) + relativeY << 1) + relativeX;
    }

    private record CacheEntry(@Nullable LevelChunkSection chunkSection, int sectionX, int sectionY, int sectionZ) {
    }
}


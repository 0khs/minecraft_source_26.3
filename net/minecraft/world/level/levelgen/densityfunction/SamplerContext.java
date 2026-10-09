/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.densityfunction;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferArena;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import org.jspecify.annotations.Nullable;

public class SamplerContext {
    public static final SamplerContext EMPTY_UNCACHED = new SamplerContext(ContextMap.EMPTY, DensityBufferArena.GLOBAL, false);
    private static final int CACHE_SIZE_STEP = 16;
    private final ContextMap userFields;
    private final DensityBufferArena bufferArena;
    private CacheCell @Nullable [] cacheCells;

    private SamplerContext(ContextMap userFields, DensityBufferArena bufferArena, boolean enableCaches) {
        this.userFields = userFields;
        this.bufferArena = bufferArena;
        this.cacheCells = enableCaches ? new CacheCell[]{} : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> @Nullable T getField(ContextKey<T> key) {
        return this.userFields.get(key);
    }

    public <T> T getFieldOrDefault(ContextKey<? extends T> key, T defaultValue) {
        return Objects.requireNonNullElse(this.getField(key), defaultValue);
    }

    public ScopedDensityBuffer acquireBuffer(DensityVolume volume) {
        return this.bufferArena.acquire(volume.size());
    }

    private @Nullable CacheCell getCacheCell(int cacheId) {
        if (this.cacheCells == null) {
            return null;
        }
        int oldSize = this.cacheCells.length;
        if (cacheId >= oldSize) {
            int newSize = Mth.roundToward(cacheId + 1, 16);
            this.cacheCells = Arrays.copyOf(this.cacheCells, newSize);
            for (int i = oldSize; i < newSize; ++i) {
                this.cacheCells[i] = new CacheCell();
            }
        }
        return this.cacheCells[cacheId];
    }

    void sampleVolumeCached(int cacheId, DensitySampler input, DensityBuffer outputBuffer, DensityVolume volume) {
        CacheCell cell = this.getCacheCell(cacheId);
        if (cell == null) {
            input.sampleVolume(this, outputBuffer, volume);
            return;
        }
        if (cell.buffer == null || !volume.equals(cell.volume)) {
            if (cell.buffer != null) {
                cell.buffer.close();
            }
            cell.volume = volume;
            cell.buffer = this.acquireBuffer(volume);
            input.sampleVolume(this, cell.buffer, volume);
        }
        outputBuffer.copyFrom(cell.buffer);
    }

    float sampleValueCached(int cacheId, DensitySampler input, int blockX, int blockY, int blockZ) {
        int index;
        CacheCell cell = this.getCacheCell(cacheId);
        if (cell == null) {
            return input.sampleValue(this, blockX, blockY, blockZ);
        }
        long cacheKey = BlockPos.asLong(blockX, blockY, blockZ);
        if (cell.valueKey == cacheKey && !Float.isNaN(cell.value)) {
            return cell.value;
        }
        if (cell.buffer != null && cell.volume != null && (index = cell.volume.indexOfBlock(blockX, blockY, blockZ)) != -1) {
            return cell.buffer.get(index);
        }
        float value = input.sampleValue(this, blockX, blockY, blockZ);
        cell.valueKey = cacheKey;
        cell.value = value;
        return value;
    }

    private static class CacheCell {
        private @Nullable DensityVolume volume;
        private @Nullable ScopedDensityBuffer buffer;
        private long valueKey;
        private float value = Float.NaN;

        private CacheCell() {
        }
    }

    public static class Builder {
        private ContextMap userFields = ContextMap.EMPTY;
        private DensityBufferArena bufferArena = DensityBufferArena.GLOBAL;
        private boolean enableCaches;

        private Builder() {
        }

        public Builder setUserFields(ContextMap userFields) {
            this.userFields = userFields;
            return this;
        }

        public Builder useBufferArena(DensityBufferArena bufferArena) {
            this.bufferArena = bufferArena;
            return this;
        }

        public Builder enableCaches() {
            this.enableCaches = true;
            return this;
        }

        public SamplerContext build() {
            return new SamplerContext(this.userFields, this.bufferArena, this.enableCaches);
        }
    }
}


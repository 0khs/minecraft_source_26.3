/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.densityfunction;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferArena;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import org.jspecify.annotations.Nullable;

public class DensityBufferPool
implements DensityBufferArena {
    private static final int BUFFER_SIZE_INCREMENT = 16;
    private static final int MAX_REUSE_SIZE_FACTOR = 2;
    private final int maxAge;
    private final List<ScopedDensityBuffer> buffers = new ArrayList<ScopedDensityBuffer>();

    public DensityBufferPool(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public ScopedDensityBuffer acquire(int size) {
        int roundedMinSize = Mth.roundToward(size, 16);
        ScopedDensityBuffer buffer = this.tryTakeBest(roundedMinSize, roundedMinSize * 2);
        if (buffer == null) {
            return new ScopedDensityBuffer(this, roundedMinSize, size);
        }
        buffer.restore(size);
        return buffer;
    }

    private @Nullable ScopedDensityBuffer tryTakeBest(int minCapacity, int maxCapacity) {
        int bestIndex = -1;
        int bestCapacity = maxCapacity + 1;
        for (int i = this.buffers.size() - 1; i >= 0; --i) {
            int capacity = this.buffers.get(i).capacity();
            if (capacity == minCapacity) {
                return this.buffers.remove(i);
            }
            if (capacity <= minCapacity || capacity >= bestCapacity) continue;
            bestIndex = i;
            bestCapacity = capacity;
        }
        if (bestIndex == -1) {
            return null;
        }
        return this.buffers.remove(bestIndex);
    }

    @Override
    public void release(ScopedDensityBuffer buffer) {
        this.buffers.add(buffer);
    }

    public void garbageCollect() {
        this.buffers.removeIf(buffer -> buffer.incrementAge() > this.maxAge);
    }

    public void clear() {
        this.buffers.clear();
    }

    @VisibleForTesting
    int size() {
        return this.buffers.size();
    }

    public boolean isEmpty() {
        return this.buffers.isEmpty();
    }
}


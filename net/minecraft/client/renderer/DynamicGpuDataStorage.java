/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;
import java.util.List;

public interface DynamicGpuDataStorage<T extends DynamicGpuData>
extends AutoCloseable {
    public void endFrame();

    public GpuBufferSlice writeData(T var1);

    public GpuBufferSlice[] writeData(T[] var1);

    public GpuBufferSlice writeDataBatched(List<T> var1);

    public GpuBufferSlice[] writeDataBatchedMultiple(List<List<T>> var1);

    public @GpuBuffer.Usage int usage();

    @Override
    public void close();

    public static interface DynamicGpuData {
        public void write(ByteBuffer var1);
    }
}


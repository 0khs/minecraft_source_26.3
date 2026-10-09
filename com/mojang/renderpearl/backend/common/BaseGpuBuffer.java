/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.common;

import com.mojang.renderpearl.api.buffers.GpuBuffer;

public abstract class BaseGpuBuffer
implements GpuBuffer {
    private final @GpuBuffer.Usage int usage;
    private final long size;

    public BaseGpuBuffer(@GpuBuffer.Usage int usage, long size) {
        this.size = size;
        this.usage = usage;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public @GpuBuffer.Usage int usage() {
        return this.usage;
    }

    public void checkCanBeUsed() {
    }
}


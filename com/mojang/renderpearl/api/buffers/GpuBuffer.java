/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.buffers;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface GpuBuffer
extends UncheckedAutoCloseable {
    public static final int USAGE_MAP_READ = 1;
    public static final int USAGE_MAP_WRITE = 2;
    public static final int USAGE_HINT_CLIENT_STORAGE = 4;
    public static final int USAGE_COPY_DST = 8;
    public static final int USAGE_COPY_SRC = 16;
    public static final int USAGE_VERTEX = 32;
    public static final int USAGE_INDEX = 64;
    public static final int USAGE_UNIFORM = 128;
    public static final int USAGE_UNIFORM_TEXEL_BUFFER = 256;
    public static final int USAGE_INDIRECT_PARAMETERS = 512;

    public long size();

    public @Usage int usage();

    public boolean isClosed();

    default public GpuBufferSlice slice(long offset, long length) {
        if (offset < 0L || length < 0L || offset + length > this.size()) {
            throw new IllegalArgumentException("Offset of " + offset + " and length " + length + " would put new slice outside buffer's range (of 0," + this.size() + ")");
        }
        return new GpuBufferSlice(this, offset, length);
    }

    default public GpuBufferSlice slice() {
        return new GpuBufferSlice(this, 0L, this.size());
    }

    default public GpuBufferSlice.MappedView map(boolean read, boolean write) {
        return this.map(0L, this.size(), read, write);
    }

    public GpuBufferSlice.MappedView map(long var1, long var3, boolean var5, boolean var6);

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Usage {
    }
}


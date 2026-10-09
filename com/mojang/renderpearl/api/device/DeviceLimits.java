/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.device;

import com.mojang.renderpearl.api.GpuFormat;

public record DeviceLimits(int maxAnisotropy, int minUniformOffsetAlignment, int maxTextureSize, long maxMemoryAllocationSize, int maxMultiDrawDirectInterleavedDrawCount, int maxColorAttachments, int maxDrawIndirectDrawCount) {
    public int maxTextureSizeForFormat(GpuFormat format) {
        return Integer.highestOneBit(Math.min(this.maxTextureSize, (int)Math.sqrt((double)this.maxMemoryAllocationSize / (double)format.blockSize())));
    }
}


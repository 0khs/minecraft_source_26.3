/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.common;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;

public abstract class BaseGpuTexture
implements GpuTexture {
    private final GpuFormat format;
    private final int width;
    private final int height;
    private final int depthOrLayers;
    private final int mipLevels;
    private final @GpuTexture.Usage int usage;
    private final String label;

    public BaseGpuTexture(@GpuTexture.Usage int usage, String label, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        this.usage = usage;
        this.label = label;
        this.format = format;
        this.width = width;
        this.height = height;
        this.depthOrLayers = depthOrLayers;
        this.mipLevels = mipLevels;
    }

    @Override
    public int getWidth(int mipLevel) {
        return this.width >> mipLevel;
    }

    @Override
    public int getHeight(int mipLevel) {
        return this.height >> mipLevel;
    }

    @Override
    public int getDepthOrLayers() {
        return this.depthOrLayers;
    }

    @Override
    public int getMipLevels() {
        return this.mipLevels;
    }

    @Override
    public GpuFormat getFormat() {
        return this.format;
    }

    @Override
    public @GpuTexture.Usage int usage() {
        return this.usage;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}


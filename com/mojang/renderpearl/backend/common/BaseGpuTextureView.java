/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.common;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;

public abstract class BaseGpuTextureView
implements GpuTextureView {
    private final GpuTexture texture;
    private final int baseMipLevel;
    private final int mipLevels;

    protected BaseGpuTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
        this.texture = texture;
        this.baseMipLevel = baseMipLevel;
        this.mipLevels = mipLevels;
    }

    @Override
    public GpuTexture texture() {
        return this.texture;
    }

    @Override
    public int baseMipLevel() {
        return this.baseMipLevel;
    }

    @Override
    public int mipLevels() {
        return this.mipLevels;
    }

    @Override
    public int getWidth(int mipLevel) {
        return this.texture.getWidth(mipLevel + this.baseMipLevel);
    }

    @Override
    public int getHeight(int mipLevel) {
        return this.texture.getHeight(mipLevel + this.baseMipLevel);
    }
}


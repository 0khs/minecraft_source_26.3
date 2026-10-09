/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.textures;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;

public interface GpuTextureView
extends UncheckedAutoCloseable {
    public boolean isClosed();

    public GpuTexture texture();

    public int baseMipLevel();

    public int mipLevels();

    public int getWidth(int var1);

    public int getHeight(int var1);
}


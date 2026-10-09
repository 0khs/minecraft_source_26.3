/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.backend.opengl.FrameBufferCache;

public interface FrameBufferAttachment {
    public int glId();

    public int fboMipLevel();

    public void addAssociatedFbo(FrameBufferCache.CacheKey var1);

    public void removeAssociatedFbo(FrameBufferCache.CacheKey var1);
}


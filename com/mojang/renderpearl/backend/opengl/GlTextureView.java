/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.backend.common.BaseGpuTextureView;
import com.mojang.renderpearl.backend.opengl.FrameBufferAttachment;
import com.mojang.renderpearl.backend.opengl.FrameBufferCache;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import java.util.ArrayList;
import java.util.List;

public class GlTextureView
extends BaseGpuTextureView
implements FrameBufferAttachment {
    private static final int EMPTY = -1;
    private boolean closed;
    private final FrameBufferCache frameBufferCache;
    private final List<FrameBufferCache.CacheKey> fboKeys = new ArrayList<FrameBufferCache.CacheKey>();

    protected GlTextureView(GlTexture texture, int baseMipLevel, int mipLevels, FrameBufferCache frameBufferCache) {
        super(texture, baseMipLevel, mipLevels);
        texture.addViews();
        this.frameBufferCache = frameBufferCache;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.texture().removeViews();
            while (!this.fboKeys.isEmpty()) {
                this.frameBufferCache.destroyFbo(this.fboKeys.getLast());
            }
        }
    }

    @Override
    public GlTexture texture() {
        return (GlTexture)super.texture();
    }

    @Override
    public int glId() {
        return this.texture().id;
    }

    @Override
    public int fboMipLevel() {
        return this.baseMipLevel();
    }

    @Override
    public void addAssociatedFbo(FrameBufferCache.CacheKey fboKey) {
        this.fboKeys.add(fboKey);
    }

    @Override
    public void removeAssociatedFbo(FrameBufferCache.CacheKey fboKey) {
        this.fboKeys.remove(fboKey);
    }
}


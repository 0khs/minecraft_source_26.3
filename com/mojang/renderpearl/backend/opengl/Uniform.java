/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;

public sealed interface Uniform
extends UncheckedAutoCloseable {
    @Override
    default public void close() {
    }

    public record Sampler(int samplerIndex) implements Uniform
    {
    }

    public record Utb(int samplerIndex, GpuFormat format, int texture) implements Uniform
    {
        public Utb(int samplerIndex, GpuFormat format) {
            this(samplerIndex, format, GlStateManager._genTexture());
        }

        @Override
        public void close() {
            GlStateManager._deleteTexture(this.texture);
        }
    }

    public record Ubo(int blockBinding) implements Uniform
    {
    }
}


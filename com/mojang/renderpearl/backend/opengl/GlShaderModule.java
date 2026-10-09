/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;

public class GlShaderModule
implements UncheckedAutoCloseable {
    private static final int NOT_ALLOCATED = -1;
    public static final GlShaderModule INVALID_SHADER = new GlShaderModule(-1, "invalid", ShaderType.VERTEX);
    private final String label;
    private int shaderId;
    private final ShaderType type;

    public GlShaderModule(int shaderId, String label, ShaderType type) {
        this.label = label;
        this.shaderId = shaderId;
        this.type = type;
    }

    @Override
    public void close() {
        if (this.shaderId == -1) {
            throw new IllegalStateException("Already closed");
        }
        RenderSystem.assertOnRenderThread();
        GlStateManager.glDeleteShader(this.shaderId);
        this.shaderId = -1;
    }

    public String getLabel() {
        return this.label;
    }

    public int getShaderId() {
        return this.shaderId;
    }

    public ShaderType getType() {
        return this.type;
    }
}


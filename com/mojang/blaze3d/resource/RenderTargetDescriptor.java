/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.resource;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import java.util.Objects;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public record RenderTargetDescriptor(int width, int height, @Nullable TextureProperties color, @Nullable TextureProperties depth) implements ResourceDescriptor<RenderTarget>
{
    @Override
    public RenderTarget allocate() {
        return new TextureTarget(null, this.width, this.height, this.color != null ? this.color.format : null, this.depth != null ? this.depth.format : null);
    }

    @Override
    public void prepare(RenderTarget resource) {
        if (this.color != null && this.depth != null && this.color.clearColor != null && this.depth.clearColor != null) {
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(resource.getColorTexture(), this.color.clearColor, resource.getDepthTexture(), this.depth.clearColor.x());
        } else if (this.color != null && this.color.clearColor != null) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(resource.getColorTexture(), this.color.clearColor);
        } else if (this.depth != null && this.depth.clearColor != null) {
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(resource.getDepthTexture(), this.depth.clearColor.x());
        }
    }

    @Override
    public void free(RenderTarget resource) {
        resource.destroyBuffers();
    }

    @Override
    public boolean canUsePhysicalResource(ResourceDescriptor<?> other) {
        if (other instanceof RenderTargetDescriptor) {
            RenderTargetDescriptor descriptor = (RenderTargetDescriptor)other;
            return this.width == descriptor.width && this.height == descriptor.height && Objects.equals(this.color, descriptor.color) && Objects.equals(this.depth, descriptor.depth);
        }
        return false;
    }

    public record TextureProperties(@Nullable Vector4fc clearColor, GpuFormat format) {
        public static final TextureProperties DEFAULT_DEPTH = new TextureProperties((Vector4fc)new Vector4f(0.0f, 0.0f, 0.0f, 0.0f), GpuFormat.D32_FLOAT);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.api.commands;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.buffers.TransientMemory;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public interface CommandEncoder {
    public void submit();

    public TransientMemory transientMemory();

    default public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTexture, Optional<Vector4fc> clearColor) {
        return this.createRenderPass(label, colorTexture, clearColor, null, OptionalDouble.empty());
    }

    default public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTexture, Optional<Vector4fc> clearColor, @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth) {
        return this.createRenderPass(label, colorTexture, clearColor, depthTexture, clearDepth, new RenderPass.RenderArea(0, 0, colorTexture.getWidth(0), colorTexture.getHeight(0)));
    }

    default public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTexture, Optional<Vector4fc> clearColor, @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth, RenderPass.RenderArea renderArea) {
        RenderPassDescriptor.Builder descriptor = RenderPassDescriptor.builder(label).withColorAttachment(colorTexture, clearColor);
        if (depthTexture != null) {
            descriptor.withDepthAttachment(depthTexture, clearDepth);
        }
        descriptor.withRenderArea(renderArea);
        return this.createRenderPass(descriptor.build());
    }

    public RenderPass createRenderPass(RenderPassDescriptor var1);

    public void clearColorTexture(GpuTexture var1, Vector4fc var2);

    public void clearColorAndDepthTextures(GpuTexture var1, Vector4fc var2, GpuTexture var3, double var4);

    public void clearColorAndDepthTextures(GpuTexture var1, Vector4fc var2, GpuTexture var3, double var4, int var6, int var7, int var8, int var9, int var10);

    public void clearDepthTexture(GpuTexture var1, double var2);

    public void writeToBuffer(GpuBufferSlice var1, ByteBuffer var2);

    public void copyToBuffer(GpuBufferSlice var1, GpuBufferSlice var2);

    public void writeToTexture(GpuTexture var1, NativeImage var2);

    public void writeToTexture(GpuTexture var1, NativeImage var2, int var3, int var4, int var5, int var6);

    public void writeToTexture(GpuTexture var1, ByteBuffer var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public void copyBufferToTexture(GpuBufferSlice var1, int var2, int var3, int var4, int var5, GpuTexture var6, int var7, int var8, int var9, int var10, int var11, int var12);

    public void copyTextureToBuffer(GpuTexture var1, GpuBuffer var2, long var3, Runnable var5, int var6);

    public void copyTextureToBuffer(GpuTexture var1, GpuBuffer var2, long var3, Runnable var5, int var6, int var7, int var8, int var9, int var10);

    public void copyTextureToTexture(GpuTexture var1, GpuTexture var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);

    public GpuFence createFence();

    public void writeTimestamp(GpuQueryPool var1, int var2);
}


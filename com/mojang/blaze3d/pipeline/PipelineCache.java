/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class PipelineCache
implements AutoCloseable {
    private final GpuDevice device;
    private final ShaderSource shaderSource;
    private final Map<RenderPipeline, CompiledRenderPipeline> cache = new Reference2ReferenceOpenHashMap();

    public PipelineCache(GpuDevice device, ShaderSource shaderSource) {
        this.device = device;
        this.shaderSource = shaderSource;
    }

    public void insert(RenderPipeline pipeline, CompiledRenderPipeline compiled) {
        this.cache.put(pipeline, compiled);
    }

    public @Nullable CompiledRenderPipeline get(RenderPipeline pipeline) {
        CompiledRenderPipeline cachedPipeline = this.cache.get(pipeline);
        if (cachedPipeline != null) {
            return cachedPipeline;
        }
        CompiledRenderPipeline newPipeline = this.device.compilePipeline(pipeline, this.shaderSource, Util.backgroundExecutor()).join().finishCompile();
        if (newPipeline == null) {
            return null;
        }
        this.cache.put(pipeline, newPipeline);
        return newPipeline;
    }

    public void clear() {
        this.cache.values().forEach(UncheckedAutoCloseable::close);
        this.cache.clear();
    }

    @Override
    public void close() {
        this.clear();
        this.shaderSource.close();
    }
}


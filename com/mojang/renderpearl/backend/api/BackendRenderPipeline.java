/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.backend.api;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.backend.api.SpvModule;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.List;
import org.jspecify.annotations.Nullable;

public interface BackendRenderPipeline
extends UncheckedAutoCloseable {
    public boolean isClosed();

    @FunctionalInterface
    public static interface Pending {
        public static final Pending NULL = () -> null;

        public @Nullable BackendRenderPipeline finishCompile();
    }

    public record CreateInfo(String name, List<Shader> shaders, List<VertexBuffer> vertexBuffers, List<AttribBinding> attribBindings, List<BindGroupLayout.UniformDescription> uniforms, int pushConstantsSize, @Nullable DepthStencilState depthStencilState, PolygonMode polygonMode, boolean cull, List<@Nullable ColorTargetState> colorTargetStates, PrimitiveTopology primitiveTopology) {

        public record AttribBinding(int bufferSlot, int location, int offset, GpuFormat format) {
        }

        public record VertexBuffer(int bufferSlot, int stride, int stepRate) {
        }

        public record Shader(String name, String entryPoint, SpvModule module) {
        }
    }
}


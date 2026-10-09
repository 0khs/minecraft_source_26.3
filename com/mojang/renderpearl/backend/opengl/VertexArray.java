/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBVertexAttribBinding
 *  org.lwjgl.opengl.GL33C
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;

public abstract sealed class VertexArray
implements UncheckedAutoCloseable {
    protected final int vertexArrayId = GL33C.glGenVertexArrays();

    public static BiFunction<GlProgram, BackendRenderPipeline.CreateInfo, VertexArray> createSource(GLCapabilities capabilities, Set<String> enabledExtensions) {
        if (capabilities.GL_ARB_vertex_attrib_binding && GlDevice.USE_GL_ARB_vertex_attrib_binding) {
            enabledExtensions.add("GL_ARB_vertex_attrib_binding");
            return Separate::new;
        }
        return Emulated::new;
    }

    private VertexArray() {
    }

    @Override
    public void close() {
        GL33C.glDeleteVertexArrays((int)this.vertexArrayId);
    }

    public abstract void bind(@Nullable GpuBufferSlice[] var1);

    private static final class Separate
    extends VertexArray {
        private final boolean needsMesaWorkaround;
        private final int[] strides = new int[16];

        private Separate(GlProgram program, BackendRenderPipeline.CreateInfo createInfo) {
            String version;
            this.needsMesaWorkaround = "Mesa".equals(GL33C.glGetString((int)7936)) ? (version = GL33C.glGetString((int)7938)).contains("25.0.0") || version.contains("25.0.1") || version.contains("25.0.2") : false;
            GlStateManager._glBindVertexArray(this.vertexArrayId);
            for (BackendRenderPipeline.CreateInfo.VertexBuffer vertexBuffer : createInfo.vertexBuffers()) {
                this.strides[vertexBuffer.bufferSlot()] = vertexBuffer.stride();
                ARBVertexAttribBinding.glVertexBindingDivisor((int)vertexBuffer.bufferSlot(), (int)vertexBuffer.stepRate());
            }
            for (BackendRenderPipeline.CreateInfo.AttribBinding attribBinding : createInfo.attribBindings()) {
                int attribLocation = attribBinding.location();
                GL33C.glEnableVertexAttribArray((int)attribLocation);
                int glExternalId = GlConst.toGlExternalId(attribBinding.format());
                int glType = GlConst.toGlType(attribBinding.format());
                boolean isIntegerFormat = GlConst.isGlFormatInteger(glExternalId);
                boolean isNormalizedFormat = GlConst.isFormatNormalized(attribBinding.format());
                int channelCount = GlConst.glFormatChannelCount(glExternalId);
                if (isIntegerFormat) {
                    ARBVertexAttribBinding.glVertexAttribIFormat((int)attribLocation, (int)channelCount, (int)glType, (int)attribBinding.offset());
                } else {
                    ARBVertexAttribBinding.glVertexAttribFormat((int)attribLocation, (int)channelCount, (int)glType, (boolean)isNormalizedFormat, (int)attribBinding.offset());
                }
                ARBVertexAttribBinding.glVertexAttribBinding((int)attribLocation, (int)attribBinding.bufferSlot());
            }
            GlStateManager._glBindVertexArray(0);
        }

        @Override
        public void bind(@Nullable GpuBufferSlice[] vertexBuffers) {
            GlStateManager._glBindVertexArray(this.vertexArrayId);
            for (int i = 0; i < vertexBuffers.length; ++i) {
                if (this.strides[i] == 0) continue;
                GpuBufferSlice vertexBufferSlice = vertexBuffers[i];
                if (vertexBufferSlice == null) {
                    throw new IllegalStateException("Vertex buffer slot " + i + " not specified but required");
                }
                GlBuffer vertexBuffer = (GlBuffer)vertexBufferSlice.buffer();
                if (this.needsMesaWorkaround) {
                    ARBVertexAttribBinding.glBindVertexBuffer((int)i, (int)0, (long)0L, (int)0);
                }
                ARBVertexAttribBinding.glBindVertexBuffer((int)i, (int)vertexBuffer.handle(), (long)vertexBufferSlice.offset(), (int)this.strides[i]);
            }
        }
    }

    private static final class Emulated
    extends VertexArray {
        private final int[] bufferStride = new int[16];
        private final int[] bufferDivisor = new int[16];
        private final int[] bufferIndex = new int[16];
        private final int[] channelCount = new int[16];
        private final int[] glType = new int[16];
        private final boolean[] isInteger = new boolean[16];
        private final boolean[] isNormalized = new boolean[16];
        private final int[] elementOffset = new int[16];

        private Emulated(GlProgram program, BackendRenderPipeline.CreateInfo createInfo) {
            GlStateManager._glBindVertexArray(this.vertexArrayId);
            for (BackendRenderPipeline.CreateInfo.VertexBuffer vertexBuffer : createInfo.vertexBuffers()) {
                this.bufferStride[vertexBuffer.bufferSlot()] = vertexBuffer.stride();
                this.bufferDivisor[vertexBuffer.bufferSlot()] = vertexBuffer.stepRate();
            }
            Arrays.fill(this.bufferIndex, -1);
            for (BackendRenderPipeline.CreateInfo.AttribBinding attribBinding : createInfo.attribBindings()) {
                int attribLocation = attribBinding.location();
                GL33C.glEnableVertexAttribArray((int)attribLocation);
                this.bufferIndex[attribLocation] = attribBinding.bufferSlot();
                int glExternalId = GlConst.toGlExternalId(attribBinding.format());
                this.glType[attribLocation] = GlConst.toGlType(attribBinding.format());
                this.isInteger[attribLocation] = GlConst.isGlFormatInteger(glExternalId);
                this.isNormalized[attribLocation] = GlConst.isFormatNormalized(attribBinding.format());
                this.channelCount[attribLocation] = GlConst.glFormatChannelCount(glExternalId);
                this.elementOffset[attribLocation] = attribBinding.offset();
            }
            GlStateManager._glBindVertexArray(0);
        }

        @Override
        public void bind(@Nullable GpuBufferSlice[] vertexBuffers) {
            GlStateManager._glBindVertexArray(this.vertexArrayId);
            for (int attributeIndex = 0; attributeIndex < 16; ++attributeIndex) {
                if (this.bufferIndex[attributeIndex] == -1) continue;
                int vertexBufferIndex = this.bufferIndex[attributeIndex];
                if (vertexBuffers[vertexBufferIndex] == null) {
                    throw new IllegalStateException("Vertex buffer slot " + vertexBufferIndex + " not specified but required");
                }
                GlStateManager._glBindBuffer(34962, ((GlBuffer)vertexBuffers[vertexBufferIndex].buffer()).handle());
                long totalOffset = vertexBuffers[vertexBufferIndex].offset() + (long)this.elementOffset[attributeIndex];
                if (this.isInteger[attributeIndex]) {
                    GL33C.glVertexAttribIPointer((int)attributeIndex, (int)this.channelCount[attributeIndex], (int)this.glType[attributeIndex], (int)this.bufferStride[vertexBufferIndex], (long)totalOffset);
                } else {
                    GL33C.glVertexAttribPointer((int)attributeIndex, (int)this.channelCount[attributeIndex], (int)this.glType[attributeIndex], (boolean)this.isNormalized[attributeIndex], (int)this.bufferStride[vertexBufferIndex], (long)totalOffset);
                }
                GL33C.glVertexAttribDivisor((int)attributeIndex, (int)this.bufferDivisor[vertexBufferIndex]);
            }
        }
    }
}


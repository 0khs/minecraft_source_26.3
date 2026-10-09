/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.booleans.BooleanArrayList
 *  it.unimi.dsi.fastutil.booleans.BooleanList
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlCommandEncoder;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlQueryPool;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;

class GlRenderPass
implements RenderPassBackend {
    private final GlCommandEncoder encoder;
    private final GlDevice device;
    private final ScissorState defaultScissorState;
    protected @Nullable GlRenderPipeline pipeline;
    protected final @Nullable GpuBufferSlice[] vertexBuffers = new GpuBufferSlice[16];
    protected boolean vertexBufferDirty = true;
    protected @Nullable GpuBuffer indexBuffer;
    protected IndexType indexType = IndexType.INT;
    protected boolean indexBufferDirty = false;
    private final ScissorState scissorState = new ScissorState();
    protected boolean scissorStateDirty = true;
    protected final ReferenceList<Object> uniforms = new ReferenceArrayList();
    protected final BooleanList dirtyUniforms = new BooleanArrayList();
    protected boolean anyUniformDirty = false;
    protected @Nullable GpuBufferSlice pushConstants;
    protected boolean pushConstantsDirty = false;
    protected final int colorAttachmentCount;

    public GlRenderPass(GlCommandEncoder encoder, GlDevice device, int colorAttachmentCount, ScissorState defaultScissorState) {
        this.encoder = encoder;
        this.device = device;
        this.colorAttachmentCount = colorAttachmentCount;
        this.defaultScissorState = defaultScissorState;
        this.scissorState.setFrom(defaultScissorState);
    }

    @Override
    public void pushDebugGroup(Supplier<String> label) {
        this.device.debugLabels().pushDebugGroup(label);
    }

    @Override
    public void popDebugGroup() {
        this.device.debugLabels().popDebugGroup();
    }

    @Override
    public void setPipeline(BackendRenderPipeline pipeline) {
        if (!(pipeline instanceof GlRenderPipeline)) {
            throw new IllegalArgumentException("Pipeline must be instance of GlRenderPipeline");
        }
        GlRenderPipeline glRenderPipeline = (GlRenderPipeline)pipeline;
        if (this.pipeline == null || this.pipeline != pipeline) {
            this.uniforms.clear();
            this.uniforms.size(glRenderPipeline.program().uniformCount());
            this.dirtyUniforms.clear();
            this.dirtyUniforms.size(glRenderPipeline.program().uniformCount());
            for (int i = 0; i < this.dirtyUniforms.size(); ++i) {
                this.dirtyUniforms.set(i, true);
            }
            this.anyUniformDirty = true;
        }
        this.pipeline = glRenderPipeline;
        this.vertexBufferDirty = true;
        this.indexBufferDirty = this.indexBuffer != null;
    }

    @Override
    public void setUniform(int index, @Nullable Object value) {
        this.uniforms.set(index, value);
        this.dirtyUniforms.set(index, true);
        this.anyUniformDirty = true;
    }

    @Override
    public void pushConstants(ByteBuffer value) {
        this.pushConstants = this.encoder.transientMemory().uploadGpu(value, (long)this.device.getDeviceInfo().limits().minUniformOffsetAlignment(), 128);
        this.pushConstantsDirty = true;
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        this.scissorState.enable(x, y, width, height);
        this.scissorStateDirty = true;
    }

    @Override
    public void disableScissor() {
        this.scissorState.setFrom(this.defaultScissorState);
        this.scissorStateDirty = true;
    }

    public boolean isScissorEnabled() {
        return this.scissorState.enabled();
    }

    public int getScissorX() {
        return this.scissorState.x();
    }

    public int getScissorY() {
        return this.scissorState.y();
    }

    public int getScissorWidth() {
        return this.scissorState.width();
    }

    public int getScissorHeight() {
        return this.scissorState.height();
    }

    @Override
    public void setVertexBuffer(int slot, @Nullable GpuBufferSlice vertexBuffer) {
        GpuBuffer inputBuffer = vertexBuffer != null ? vertexBuffer.buffer() : null;
        GpuBuffer existingBuffer = this.vertexBuffers[slot] != null ? this.vertexBuffers[slot].buffer() : null;
        long inputOffset = vertexBuffer != null ? vertexBuffer.offset() : 0L;
        long exitingOffset = this.vertexBuffers[slot] != null ? this.vertexBuffers[slot].offset() : 0L;
        this.vertexBufferDirty |= inputBuffer != existingBuffer || inputOffset != exitingOffset;
        this.vertexBuffers[slot] = vertexBuffer;
    }

    @Override
    public void setIndexBuffer(@Nullable GpuBuffer indexBuffer, IndexType indexType) {
        this.indexBuffer = indexBuffer;
        this.indexType = indexType;
        this.indexBufferDirty = true;
    }

    @Override
    public void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
        this.encoder.executeDraw(this, vertexOffset, firstIndex, indexCount, this.indexType, instanceCount, firstInstance);
    }

    @Override
    public void multiDrawIndexed(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
        throw new UnsupportedOperationException("OpenGL does not support the multiDrawDirectInterleaved device feature");
    }

    @Override
    public void multiDrawIndexed(PointerBuffer firstIndexOffsets, IntBuffer indexCounts, IntBuffer vertexOffsets, int drawCount) {
        this.encoder.executeDraws(this, this.indexType, firstIndexOffsets, indexCounts, vertexOffsets, drawCount);
    }

    @Override
    public void drawIndexedIndirect(GpuBufferSlice commands, int drawCount) {
        this.encoder.executeDrawIndirect(this, this.indexType, (GlBuffer)commands.buffer(), commands.offset(), drawCount);
    }

    @Override
    public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        this.encoder.executeDraw(this, firstVertex, 0, vertexCount, null, instanceCount, firstInstance);
    }

    @Override
    public void multiDraw(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
        throw new UnsupportedOperationException("OpenGL does not support the multiDrawDirectInterleaved device feature");
    }

    @Override
    public void multiDraw(IntBuffer firstVertices, IntBuffer vertexCounts, int drawCount) {
        this.encoder.executeDraws(this, null, null, vertexCounts, firstVertices, drawCount);
    }

    @Override
    public void drawIndirect(GpuBufferSlice commands, int drawCount) {
        this.encoder.executeDrawIndirect(this, null, (GlBuffer)commands.buffer(), commands.offset(), drawCount);
    }

    @Override
    public void writeTimestamp(GpuQueryPool pool, int index) {
        ((GlQueryPool)pool).writeTimestamp(index);
    }
}


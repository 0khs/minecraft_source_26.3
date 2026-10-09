/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.vulkan.VkDrawIndexedIndirectCommand
 *  org.lwjgl.vulkan.VkDrawIndirectCommand
 */
package com.mojang.renderpearl.frontend;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceLimits;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.common.BaseGpuBuffer;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.lwjgl.vulkan.VkDrawIndirectCommand;

public class FrontendRenderPass
implements RenderPass,
RenderPass.UniformUploader {
    private final RenderPassBackend backend;
    private final GpuDeviceBackend device;
    private final DeviceFeatures deviceFeatures;
    private final DeviceLimits deviceLimits;
    private final Runnable onFinish;
    private final @Nullable RenderPass.RenderArea renderArea;
    private final List<@Nullable RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colorAttachments;
    private final boolean hasDepthAttachment;
    private boolean isClosed;
    private int pushedDebugGroups;
    private @Nullable FrontendRenderPipeline boundPipeline;
    private final @Nullable GpuBufferSlice[] vertexBuffers = new GpuBufferSlice[16];
    protected @Nullable GpuBuffer indexBuffer;
    protected final HashMap<String, Object> uniforms = new HashMap();
    private boolean constantsPushed = false;

    public FrontendRenderPass(RenderPassBackend backend, GpuDeviceBackend device, List<@Nullable RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colorAttachments, boolean hasDepthAttachment, Runnable onFinish, @Nullable RenderPass.RenderArea renderArea) {
        this.backend = backend;
        this.device = device;
        this.deviceFeatures = device.getDeviceInfo().features();
        this.deviceLimits = device.getDeviceInfo().limits();
        this.colorAttachments = colorAttachments;
        this.hasDepthAttachment = hasDepthAttachment;
        this.onFinish = onFinish;
        this.renderArea = renderArea;
    }

    @Override
    public void pushDebugGroup(Supplier<String> label) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        ++this.pushedDebugGroups;
        this.backend.pushDebugGroup(label);
    }

    @Override
    public void popDebugGroup() {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (this.pushedDebugGroups == 0) {
            throw new IllegalStateException("Can't pop more debug groups than was pushed!");
        }
        --this.pushedDebugGroups;
        this.backend.popDebugGroup();
    }

    @Override
    public void writeTimestamp(GpuQueryPool pool, int index) {
        if (index < 0 || index > pool.size()) {
            throw new IllegalStateException("Index " + index + " is out of range for query pool of size " + pool.size());
        }
        this.backend.writeTimestamp(pool, index);
    }

    @Override
    public void setPipeline(CompiledRenderPipeline pipeline) {
        if (!(pipeline instanceof FrontendRenderPipeline)) {
            throw new IllegalArgumentException("Pipeline must be instance of FrontendCompiledRenderPipeline");
        }
        FrontendRenderPipeline frontendPipeline = (FrontendRenderPipeline)pipeline;
        List<@Nullable ColorTargetState> colorTargetStates = frontendPipeline.colorTargetStates();
        if (colorTargetStates.size() != this.colorAttachments.size()) {
            throw new IllegalStateException("Render pass color attachment count must match pipeline color target state count.");
        }
        for (int i = 0; i < this.colorAttachments.size(); ++i) {
            ColorTargetState colorTargetState;
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment = this.colorAttachments.get(i);
            if (attachment == null || (colorTargetState = colorTargetStates.get(i)) != null && colorTargetState.format() == attachment.textureView().texture().getFormat()) continue;
            throw new IllegalStateException("Render pass color attachment " + i + " format doesn't match pipeline format.");
        }
        this.boundPipeline = frontendPipeline;
        this.backend.setPipeline(frontendPipeline.backendRenderPipeline());
        this.uniforms.forEach(this::setUniform);
        this.constantsPushed = false;
    }

    @Override
    public void setUniform(String name, @Nullable GpuTextureView textureView, @Nullable GpuSampler sampler) {
        if (textureView != null && sampler != null) {
            TextureViewAndSampler pair = new TextureViewAndSampler(textureView, sampler);
            this.setUniform(name, pair);
        } else {
            if (textureView != null || sampler != null) {
                throw new IllegalArgumentException("textureView and sampler must both or neither be null");
            }
            this.setUniform(name, (Object)null);
        }
    }

    @Override
    public void setUniform(String name, GpuBuffer value) {
        this.setUniform(name, value.slice());
    }

    @Override
    public void setUniform(String name, GpuBufferSlice value) {
        int alignment = this.device.getDeviceInfo().limits().minUniformOffsetAlignment();
        if (value.offset() % (long)alignment > 0L) {
            throw new IllegalArgumentException("Uniform buffer offset must be aligned to " + alignment);
        }
        this.setUniform(name, (Object)value);
    }

    private void setUniform(String name, @Nullable Object value) {
        if (value == null) {
            this.uniforms.remove(name);
        } else {
            this.uniforms.put(name, value);
        }
        if (this.boundPipeline == null) {
            return;
        }
        int uniformIndex = this.boundPipeline.uniformIndices().getOrDefault((Object)name, -1);
        if (uniformIndex == -1) {
            return;
        }
        this.backend.setUniform(uniformIndex, value);
    }

    @Override
    public void pushConstants(ByteBuffer value) {
        if (this.boundPipeline == null) {
            throw new IllegalStateException("Must bind pipeline before pushing constants");
        }
        if (value.remaining() < this.boundPipeline.pushConstantSize()) {
            throw new IllegalArgumentException("Not enough values for push constant range");
        }
        this.constantsPushed = true;
        this.backend.pushConstants(value);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Scissor size must be >0, was " + width + "x" + height);
        }
        if (x < this.renderArea.x() || y < this.renderArea.y() || x + width > this.renderArea.x() + this.renderArea.width() || y + height > this.renderArea.y() + this.renderArea.height()) {
            throw new IllegalArgumentException("Scissor at " + x + ", " + y + " with size " + width + "x" + height + " is out of bounds for render area " + String.valueOf(this.renderArea));
        }
        this.backend.enableScissor(x, y, width, height);
    }

    @Override
    public void disableScissor() {
        this.backend.disableScissor();
    }

    @Override
    public void setVertexBuffer(int slot, @Nullable GpuBufferSlice vertexBuffer) {
        if (slot < 0 || slot >= 16) {
            throw new IllegalArgumentException("Vertex buffer slot is out of range: " + slot);
        }
        if (vertexBuffer != null && vertexBuffer.buffer().isClosed()) {
            throw new IllegalStateException("Vertex buffer at slot " + slot + " has been closed!");
        }
        if (vertexBuffer != null && (vertexBuffer.buffer().usage() & 0x20) == 0) {
            throw new IllegalStateException("Vertex buffer at slot " + slot + " doesn't have GpuBuffer.USAGE_VERTEX flag!");
        }
        this.vertexBuffers[slot] = vertexBuffer;
        this.backend.setVertexBuffer(slot, vertexBuffer);
    }

    @Override
    public void setIndexBuffer(GpuBuffer indexBuffer, IndexType indexType) {
        this.indexBuffer = indexBuffer;
        this.backend.setIndexBuffer(indexBuffer, indexType);
    }

    @Override
    public void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (firstInstance != 0 && !this.deviceFeatures.nonZeroFirstInstance()) {
            throw new UnsupportedOperationException("firstInstance must be zero on when device does not support nonZeroFirstInstance");
        }
        this.validateDraw(List.of(), false, true);
        this.backend.drawIndexed(indexCount, instanceCount, firstIndex, vertexOffset, firstInstance);
    }

    @Override
    public void multiDrawIndexed(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (!this.deviceFeatures.multiDrawDirectInterleaved()) {
            throw new UnsupportedOperationException("device does not support multiDrawDirectInterleaved");
        }
        if (firstInstance != 0 && !this.deviceFeatures.nonZeroFirstInstance()) {
            throw new UnsupportedOperationException("firstInstance must be zero on when device does not support nonZeroFirstInstance");
        }
        if (drawCount > this.deviceLimits.maxMultiDrawDirectInterleavedDrawCount()) {
            throw new IllegalArgumentException("May not exceed maxMultiDrawDirectInterleavedDrawCount draws in a single multiDrawDirectInterleaved call");
        }
        if (drawParameters.remaining() < drawCount * 3) {
            throw new IllegalArgumentException("Not enough elements in drawParameters for drawCount draws");
        }
        this.validateDraw(List.of(), false, true);
        this.backend.multiDrawIndexed(drawParameters, instanceCount, firstInstance, drawCount);
    }

    @Override
    public void multiDrawIndexed(PointerBuffer firstIndexOffsets, IntBuffer indexCounts, IntBuffer vertexOffsets, int drawCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (!this.deviceFeatures.multiDrawDirectSeparate()) {
            throw new UnsupportedOperationException("device does not support multiDrawDirectSeparate");
        }
        if (firstIndexOffsets.remaining() < drawCount) {
            throw new IllegalArgumentException("firstIndexOffsets does not contain enough elements for drawCount draws");
        }
        if (indexCounts.remaining() < drawCount) {
            throw new IllegalArgumentException("indexCounts does not contain enough elements for drawCount draws");
        }
        if (vertexOffsets.remaining() < drawCount) {
            throw new IllegalArgumentException("vertexOffsets does not contain enough elements for drawCount draws");
        }
        this.validateDraw(List.of(), false, true);
        this.backend.multiDrawIndexed(firstIndexOffsets, indexCounts, vertexOffsets, drawCount);
    }

    @Override
    public void drawIndexedIndirect(GpuBufferSlice commands, int drawCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (!this.deviceFeatures.drawIndirect()) {
            throw new UnsupportedOperationException("device does not support drawIndirect");
        }
        if (drawCount > 1 && !this.deviceFeatures.multiDrawIndirect()) {
            throw new UnsupportedOperationException("drawCount must be one when device does not support multiDrawIndirect");
        }
        if ((commands.buffer().usage() & 0x200) == 0) {
            throw new IllegalArgumentException("Indirect commands buffer must have GpuBuffer.USAGE_INDIRECT_PARAMETERS flag");
        }
        if (commands.length() < (long)drawCount * (long)VkDrawIndexedIndirectCommand.SIZEOF) {
            throw new IllegalArgumentException("Commands buffer is not large enough to hold requested draw count at the given offset");
        }
        if (commands.offset() % 4L != 0L) {
            throw new IllegalArgumentException("Commands offset must be multiple of 4");
        }
        this.validateDraw(List.of(), false, true);
        this.backend.drawIndexedIndirect(commands, drawCount);
    }

    @Override
    public <T> void drawMultipleIndexed(Collection<RenderPass.Draw<T>> draws, @Nullable GpuBuffer defaultIndexBuffer, @Nullable IndexType defaultIndexType, Collection<String> dynamicUniforms, T uniformArgument) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.validateDraw(dynamicUniforms, true, false);
        GpuBuffer lastIndexBuffer = null;
        IndexType lastIndexType = null;
        for (RenderPass.Draw<T> draw : draws) {
            GpuBuffer indexBuffer;
            BiConsumer<T, RenderPass.UniformUploader> uniformUploaderConsumer = draw.uniformUploaderConsumer();
            if (uniformUploaderConsumer != null) {
                uniformUploaderConsumer.accept(uniformArgument, this);
            }
            IndexType indexType = draw.indexType() == null ? defaultIndexType : draw.indexType();
            GpuBuffer gpuBuffer = indexBuffer = draw.indexBuffer() == null ? defaultIndexBuffer : draw.indexBuffer();
            assert (indexBuffer != null);
            assert (indexType != null);
            this.setVertexBuffer(draw.slot(), draw.vertexBuffer().slice());
            if (FrontendGpuDevice.STRICT_VALIDATION) {
                if (indexBuffer == null) {
                    throw new IllegalStateException("Missing index buffer");
                }
                ((BaseGpuBuffer)indexBuffer).checkCanBeUsed();
                if (indexBuffer.isClosed()) {
                    throw new IllegalStateException("Index buffer has been closed!");
                }
                if (draw.slot() < 0 || draw.slot() >= 16) {
                    throw new IllegalStateException("Vertex buffer slot must be between 0 and 16");
                }
                if (this.vertexBuffers[draw.slot()] != null) {
                    ((BaseGpuBuffer)this.vertexBuffers[draw.slot()].buffer()).checkCanBeUsed();
                }
                if (this.vertexBuffers[draw.slot()] == null) {
                    throw new IllegalStateException("Missing vertex buffer at slot " + draw.slot());
                }
                if (this.vertexBuffers[draw.slot()].buffer().isClosed()) {
                    throw new IllegalStateException("Vertex buffer at slot " + draw.slot() + " has been closed!");
                }
            }
            if (indexBuffer != lastIndexBuffer || indexType != lastIndexType) {
                this.setIndexBuffer(indexBuffer, indexType);
                lastIndexBuffer = indexBuffer;
                lastIndexType = indexType;
            }
            assert (this.boundPipeline != null);
            if (this.boundPipeline.pushConstantSize() > 0 && !this.constantsPushed) {
                throw new IllegalStateException("Missing push constants");
            }
            this.backend.drawIndexed(draw.indexCount(), 1, draw.firstIndex(), draw.baseVertex(), 0);
        }
    }

    @Override
    public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (firstInstance != 0 && !this.deviceFeatures.nonZeroFirstInstance()) {
            throw new UnsupportedOperationException("firstInstance must be zero on when device does not support nonZeroFirstInstance");
        }
        this.validateDraw(List.of(), false, false);
        this.backend.draw(vertexCount, instanceCount, firstVertex, firstInstance);
    }

    @Override
    public void multiDraw(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (!this.deviceFeatures.multiDrawDirectInterleaved()) {
            throw new UnsupportedOperationException("device does not support multiDrawDirectInterleaved");
        }
        if (firstInstance != 0 && !this.deviceFeatures.nonZeroFirstInstance()) {
            throw new UnsupportedOperationException("firstInstance must be zero on when device does not support nonZeroFirstInstance");
        }
        if (drawCount > this.deviceLimits.maxMultiDrawDirectInterleavedDrawCount()) {
            throw new IllegalArgumentException("May not exceed maxMultiDrawDirectInterleavedDrawCount draws in a single multiDrawDirectInterleaved call");
        }
        if (drawParameters.remaining() < drawCount * 2) {
            throw new IllegalArgumentException("Not enough elements in drawParameters for drawCount draws");
        }
        this.validateDraw(List.of(), false, false);
        this.backend.multiDraw(drawParameters, instanceCount, firstInstance, drawCount);
    }

    @Override
    public void multiDraw(IntBuffer firstVertices, IntBuffer vertexCounts, int drawCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (!this.deviceFeatures.multiDrawDirectSeparate()) {
            throw new UnsupportedOperationException("device does not support multiDrawDirectSeparate");
        }
        if (firstVertices.remaining() < drawCount) {
            throw new IllegalArgumentException("firstVertices does not contain enough elements for drawCount draws");
        }
        if (vertexCounts.remaining() < drawCount) {
            throw new IllegalArgumentException("vertexCounts does not contain enough elements for drawCount draws");
        }
        this.validateDraw(List.of(), false, false);
        this.backend.multiDraw(firstVertices, vertexCounts, drawCount);
    }

    @Override
    public void drawIndirect(GpuBufferSlice commands, int drawCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (!this.deviceFeatures.drawIndirect()) {
            throw new UnsupportedOperationException("device does not support drawIndirect");
        }
        if (drawCount > 1 && !this.deviceFeatures.multiDrawIndirect()) {
            throw new UnsupportedOperationException("drawCount must be one when device does not support multiDrawIndirect");
        }
        if ((commands.buffer().usage() & 0x200) == 0) {
            throw new IllegalArgumentException("Indirect commands buffer must have GpuBuffer.USAGE_INDIRECT_PARAMETERS flag");
        }
        if (commands.length() < (long)drawCount * (long)VkDrawIndirectCommand.SIZEOF) {
            throw new IllegalArgumentException("Commands buffer is not large enough to hold requested draw count at the given offset");
        }
        if (commands.offset() % 4L != 0L) {
            throw new IllegalArgumentException("Commands offset must be multiple of 4");
        }
        this.validateDraw(List.of(), false, false);
        this.backend.drawIndirect(commands, drawCount);
    }

    @Override
    public void close() {
        if (!this.isClosed) {
            this.isClosed = true;
            if (this.pushedDebugGroups > 0) {
                throw new IllegalStateException("Render pass had debug groups left open!");
            }
            this.onFinish.run();
        }
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void validateDraw(Collection<String> dynamicUniforms, boolean dynamicVertexBuffer, boolean indexed) {
        if (this.boundPipeline == null) {
            throw new IllegalStateException("Can't draw without a render pipeline");
        }
        if (!FrontendGpuDevice.STRICT_VALIDATION) {
            return;
        }
        if (this.boundPipeline.pushConstantSize() > 0 && !this.constantsPushed) {
            throw new IllegalStateException("Missing push constants");
        }
        if (indexed) {
            if (this.indexBuffer == null) {
                throw new IllegalStateException("Missing index buffer");
            }
            ((BaseGpuBuffer)this.indexBuffer).checkCanBeUsed();
            if (this.indexBuffer.isClosed()) {
                throw new IllegalStateException("Index buffer has been closed!");
            }
            if ((this.indexBuffer.usage() & 0x40) == 0) {
                throw new IllegalStateException("Index buffer must have GpuBuffer.USAGE_INDEX!");
            }
        }
        if (!dynamicVertexBuffer) {
            for (int i = 0; i < 16; ++i) {
                if (this.vertexBuffers[i] == null && this.boundPipeline.vertexFormats().get(i) != null) {
                    throw new IllegalStateException("Vertex format contains elements but vertex buffer at slot " + i + " is null");
                }
                if (this.vertexBuffers[i] == null) continue;
                ((BaseGpuBuffer)this.vertexBuffers[i].buffer()).checkCanBeUsed();
            }
        }
        block9: for (BindGroupLayout.UniformDescription uniform : this.boundPipeline.uniforms()) {
            long offset;
            long l;
            GpuBuffer buffer;
            Object value = this.uniforms.get(uniform.name());
            if (dynamicUniforms.contains(uniform.name())) continue;
            if (value == null) {
                throw new IllegalStateException("Missing uniform " + uniform.name() + " (should be " + String.valueOf((Object)uniform.type()) + ")");
            }
            switch (1.$SwitchMap$com$mojang$renderpearl$api$pipeline$UniformType[uniform.type().ordinal()]) {
                case 1: {
                    if (!(value instanceof GpuBufferSlice)) {
                        throw new IllegalArgumentException("UBO value must be GpuBufferSlice");
                    }
                    GpuBufferSlice valueSlice = (GpuBufferSlice)value;
                    ((BaseGpuBuffer)valueSlice.buffer()).checkCanBeUsed();
                    if (valueSlice.buffer().isClosed()) {
                        throw new IllegalStateException("Uniform buffer " + uniform.name() + " is already closed");
                    }
                    if ((valueSlice.buffer().usage() & 0x80) != 0) continue block9;
                    throw new IllegalStateException("Uniform buffer " + uniform.name() + " must have GpuBuffer.USAGE_UNIFORM");
                }
                case 2: {
                    if (!(value instanceof GpuBufferSlice)) {
                        throw new IllegalArgumentException("UTB value must be GpuBufferSlice");
                    }
                    GpuBufferSlice gpuBufferSlice = (GpuBufferSlice)value;
                    try {
                        GpuBuffer gpuBuffer;
                        buffer = gpuBuffer = gpuBufferSlice.buffer();
                        long l2 = l = gpuBufferSlice.offset();
                        offset = l;
                        l2 = l = gpuBufferSlice.length();
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }
                long length = l;
                if (offset != 0L || length != buffer.size()) {
                    throw new IllegalStateException("Uniform texel buffers do not support a slice of a buffer, must be entire buffer");
                }
                if ((buffer.usage() & 0x100) == 0) {
                    throw new IllegalStateException("Uniform texel buffer " + uniform.name() + " must have GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER");
                }
                if (uniform.gpuFormat() != null) continue block9;
                throw new IllegalStateException("Invalid uniform texel buffer " + uniform.name() + " (missing a texture format)");
                case 3: {
                    UncheckedAutoCloseable sampler;
                    GpuTextureView textureView;
                    if (!(value instanceof TextureViewAndSampler)) {
                        throw new IllegalArgumentException("Sampler value must be TextureViewAndSampler");
                    }
                    TextureViewAndSampler textureViewAndSampler = (TextureViewAndSampler)value;
                    {
                        UncheckedAutoCloseable uncheckedAutoCloseable = textureViewAndSampler.view();
                        textureView = uncheckedAutoCloseable;
                        sampler = uncheckedAutoCloseable = textureViewAndSampler.sampler();
                    }
                    if (textureView.isClosed()) {
                        throw new IllegalStateException("Texture view " + uniform.name() + " (" + textureView.texture().getLabel() + ") has been closed!");
                    }
                    if ((textureView.texture().usage() & 4) == 0) {
                        throw new IllegalStateException("Texture view " + uniform.name() + " (" + textureView.texture().getLabel() + ") must have USAGE_TEXTURE_BINDING!");
                    }
                    if (!sampler.isClosed()) break;
                    throw new IllegalStateException("Sampler for " + uniform.name() + " (" + textureView.texture().getLabel() + ") has been closed!");
                }
            }
        }
        if (this.boundPipeline.wantsDepthTexture() && !this.hasDepthAttachment) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Render pipeline %s wants a depth texture but none was provided", this.boundPipeline.name()));
        }
    }
}


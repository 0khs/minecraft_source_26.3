/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.opengl.ARBBaseInstance
 *  org.lwjgl.opengl.ARBDrawIndirect
 *  org.lwjgl.opengl.ARBMultiDrawIndirect
 *  org.lwjgl.opengl.GL33C
 *  org.lwjgl.system.CustomBuffer
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.buffers.TransientMemory;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.opengl.FrameBufferAttachment;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlFence;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlQueryPool;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlSampler;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.backend.opengl.GlTextureView;
import com.mojang.renderpearl.backend.opengl.GlTransientMemory;
import com.mojang.renderpearl.backend.opengl.Uniform;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.ARBBaseInstance;
import org.lwjgl.opengl.ARBDrawIndirect;
import org.lwjgl.opengl.ARBMultiDrawIndirect;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

class GlCommandEncoder
implements CommandEncoderBackend,
UncheckedAutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_SUBMITS_IN_FLIGHT = 2;
    private static final long NO_FENCE = 0L;
    private final GlDevice device;
    private final GlTransientMemory transientMemory;
    private final long[] fences = new long[2];
    private long currentSubmitIndex = 2L;
    private final int readFbo;
    private final int drawFbo;
    private @Nullable GlRenderPipeline lastPipeline;
    private @Nullable GlProgram lastProgram;
    private final List<@Nullable FrameBufferAttachment> renderPassColorTextures = new ArrayList<FrameBufferAttachment>();

    protected GlCommandEncoder(GlDevice device) {
        this.device = device;
        this.transientMemory = device.getDeviceInfo().features().persistentMapping() ? new GlTransientMemory.PersistentMapping(device, this) : new GlTransientMemory.Fallback(device, this);
        this.readFbo = device.directStateAccess().createFrameBufferObject();
        this.drawFbo = device.directStateAccess().createFrameBufferObject();
    }

    @Override
    public void close() {
        this.transientMemory.close();
    }

    public long currentSubmitIndex() {
        return this.currentSubmitIndex;
    }

    public int currentSubmitSlot() {
        return (int)(this.currentSubmitIndex % 2L);
    }

    @Override
    public void submit() {
        this.fences[this.currentSubmitSlot()] = GL33C.glFenceSync((int)37143, (int)0);
        ++this.currentSubmitIndex;
        if (!this.awaitSubmit(this.currentSubmitIndex - 2L, -1L)) {
            throw new IllegalStateException("Failed to wait for frame completion");
        }
        this.transientMemory.rotate();
    }

    public boolean awaitSubmit(long index, long timeoutNS) {
        if (this.currentSubmitIndex > index + 2L) {
            return true;
        }
        if (index == this.currentSubmitIndex) {
            if (timeoutNS == 0L) {
                return false;
            }
            throw new IllegalStateException("Cannot wait on a fence for the current submit");
        }
        int submitSlot = (int)(index % 2L);
        long fence = this.fences[submitSlot];
        if (fence == 0L) {
            return true;
        }
        int result = GlStateManager._glClientWaitSync(fence, 1, timeoutNS);
        if (result == 37147) {
            return false;
        }
        if (result == 37149) {
            throw new IllegalStateException("Failed to complete GPU fence: " + GlStateManager._getError());
        }
        GL33C.glDeleteSync((long)this.fences[submitSlot]);
        this.fences[submitSlot] = 0L;
        return true;
    }

    @Override
    public TransientMemory transientMemory() {
        return this.transientMemory;
    }

    @Override
    public RenderPassBackend createRenderPass(RenderPassDescriptor descriptor) {
        OptionalDouble clearValue;
        this.device.debugLabels().pushDebugGroup(descriptor.label());
        List<@Nullable RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colorAttachments = descriptor.colorAttachments();
        this.renderPassColorTextures.clear();
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> colorAttachment : colorAttachments) {
            this.renderPassColorTextures.add(colorAttachment != null ? (GlTextureView)colorAttachment.textureView() : null);
        }
        RenderPassDescriptor.Attachment<OptionalDouble> depthAttachment = descriptor.depthAttachment();
        int fbo = this.device.frameBufferCache().getFbo(this.device.directStateAccess(), this.renderPassColorTextures, depthAttachment == null ? null : (GlTextureView)depthAttachment.textureView());
        GlStateManager._glBindFramebuffer(36160, fbo);
        int width = 0;
        int height = 0;
        if (!colorAttachments.isEmpty()) {
            for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> colorAttachment : colorAttachments) {
                if (colorAttachment == null) continue;
                GpuTextureView colorTexture = colorAttachment.textureView();
                width = colorTexture.getWidth(0);
                height = colorTexture.getHeight(0);
            }
        } else if (depthAttachment != null) {
            width = depthAttachment.textureView().getWidth(0);
            height = depthAttachment.textureView().getHeight(0);
        }
        RenderPass.RenderArea renderArea = descriptor.renderArea();
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(renderArea.x(), renderArea.y(), renderArea.width(), renderArea.height());
        for (int i = 0; i < colorAttachments.size(); ++i) {
            Optional<Vector4fc> clearValue2;
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment = colorAttachments.get(i);
            if (attachment == null || !(clearValue2 = attachment.clearValue()).isPresent()) continue;
            GlStateManager._colorMask(i, 15);
            GlStateManager._clearBuffer(i, clearValue2.get());
        }
        if (depthAttachment != null && (clearValue = depthAttachment.clearValue()).isPresent()) {
            GlStateManager._depthMask(true);
            GlStateManager._clearBuffer(clearValue.getAsDouble());
        }
        GlStateManager._viewport(0, 0, width, height);
        int[] drawBuffers = new int[this.renderPassColorTextures.size()];
        for (int i = 0; i < this.renderPassColorTextures.size(); ++i) {
            drawBuffers[i] = colorAttachments.get(i) != null ? 36064 + i : 0;
        }
        GL33C.glDrawBuffers((int[])drawBuffers);
        this.lastPipeline = null;
        ScissorState scissorState = new ScissorState();
        scissorState.enable(renderArea.x(), renderArea.y(), renderArea.width(), renderArea.height());
        return new GlRenderPass(this, this.device, this.renderPassColorTextures.size(), scissorState);
    }

    @Override
    public void clearColorTexture(GpuTexture colorTexture, Vector4fc clearColor) {
        GL33C.glClearColor((float)clearColor.x(), (float)clearColor.y(), (float)clearColor.z(), (float)clearColor.w());
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(15);
        for (int i = 0; i < colorTexture.getMipLevels(); ++i) {
            this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, ((GlTexture)colorTexture).id, 0, i, 36160);
            GlStateManager._clear(16384);
        }
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth) {
        GlStateManager._disableScissorTest();
        GL33C.glClearDepth((double)clearDepth);
        GL33C.glClearColor((float)clearColor.x(), (float)clearColor.y(), (float)clearColor.z(), (float)clearColor.w());
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(15);
        for (int i = 0; i < colorTexture.getMipLevels(); ++i) {
            int fbo = this.device.frameBufferCache().getFbo(this.device.directStateAccess(), Collections.singletonList((GlTexture)colorTexture), (GlTexture)depthTexture, i);
            GlStateManager._glBindFramebuffer(36160, fbo);
            GlStateManager._clear(16640);
        }
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, int regionX, int regionY, int regionWidth, int regionHeight, int mipLevel) {
        GlStateManager._scissorBox(regionX, regionY, regionWidth, regionHeight);
        GlStateManager._enableScissorTest();
        GL33C.glClearDepth((double)clearDepth);
        GL33C.glClearColor((float)clearColor.x(), (float)clearColor.y(), (float)clearColor.z(), (float)clearColor.w());
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(15);
        int fbo = this.device.frameBufferCache().getFbo(this.device.directStateAccess(), Collections.singletonList((GlTexture)colorTexture), (GlTexture)depthTexture, mipLevel);
        GlStateManager._glBindFramebuffer(36160, fbo);
        GlStateManager._clear(16640);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        GL33C.glClearDepth((double)clearDepth);
        GlStateManager._depthMask(true);
        GlStateManager._disableScissorTest();
        for (int i = 0; i < depthTexture.getMipLevels(); ++i) {
            this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, 0, ((GlTexture)depthTexture).id, i, 36160);
            GL33C.glDrawBuffer((int)0);
            GlStateManager._clear(256);
        }
        GL33C.glDrawBuffer((int)36064);
        GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void writeToBuffer(GpuBufferSlice slice, ByteBuffer data) {
        GlBuffer buffer = (GlBuffer)slice.buffer();
        buffer.checkCanBeUsed();
        this.device.directStateAccess().bufferSubData(buffer.handle(), slice.offset(), data, buffer.usage());
    }

    @Override
    public void copyToBuffer(GpuBufferSlice source, GpuBufferSlice target) {
        GlBuffer sourceBuffer = (GlBuffer)source.buffer();
        GlBuffer targetBuffer = (GlBuffer)target.buffer();
        sourceBuffer.checkCanBeUsed();
        targetBuffer.checkCanBeUsed();
        this.device.directStateAccess().copyBufferSubData(sourceBuffer.handle(), targetBuffer.handle(), source.offset(), target.offset(), source.length());
    }

    @Override
    public void writeToTexture(GpuTexture destination, ByteBuffer source, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height) {
        int target;
        if ((destination.usage() & 0x10) != 0) {
            target = GlConst.CUBEMAP_TARGETS[depthOrLayer % 6];
            GL33C.glBindTexture((int)34067, (int)((GlTexture)destination).id);
        } else {
            target = 3553;
            GlStateManager._bindTexture(((GlTexture)destination).id);
        }
        GlStateManager._pixelStore(3314, width);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        GlStateManager._pixelStore(3317, destination.getFormat().byteAlignment());
        GlStateManager._texSubImage2D(target, mipLevel, destX, destY, width, height, GlConst.toGlExternalId(destination.getFormat()), GlConst.toGlType(destination.getFormat()), source);
    }

    @Override
    public void copyBufferToTexture(GpuBufferSlice source, int sourceX, int sourceY, int sourceWidth, int sourceHeight, GpuTexture destination, int destinationX, int destinationY, int copyWidth, int copyHeight, int mipLevel, int arrayLayer) {
        int target;
        if ((destination.usage() & 0x10) != 0) {
            target = GlConst.CUBEMAP_TARGETS[arrayLayer % 6];
            GL33C.glBindTexture((int)34067, (int)((GlTexture)destination).id);
        } else {
            target = 3553;
            GlStateManager._bindTexture(((GlTexture)destination).id);
        }
        int texelSize = destination.getFormat().blockSize();
        long skipTexels = (long)sourceX + (long)sourceY * (long)sourceWidth;
        long skipBytes = skipTexels * (long)texelSize;
        GlBuffer sourceGlBuffer = (GlBuffer)source.buffer();
        GlStateManager._glBindBuffer(35052, sourceGlBuffer.handle());
        GlStateManager._pixelStore(3314, sourceWidth);
        GlStateManager._pixelStore(32878, sourceHeight);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        GlStateManager._pixelStore(3317, destination.getFormat().byteAlignment());
        GlStateManager._texSubImage2D(target, mipLevel, destinationX, destinationY, copyWidth, copyHeight, GlConst.toGlExternalId(destination.getFormat()), GlConst.toGlType(destination.getFormat()), source.offset() + skipBytes);
        GlStateManager._glBindBuffer(35052, 0);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel) {
        this.copyTextureToBuffer(source, destination, offset, callback, mipLevel, 0, 0, source.getWidth(mipLevel), source.getHeight(mipLevel));
    }

    @Override
    public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel, int x, int y, int width, int height) {
        ((GlBuffer)destination).checkCanBeUsed();
        GlStateManager.clearGlErrors();
        boolean isDepth = source.getFormat().hasDepthAspect();
        int textureId = ((GlTexture)source).glId();
        this.device.directStateAccess().bindFrameBufferTextures(this.readFbo, !isDepth ? textureId : 0, isDepth ? textureId : 0, mipLevel, 36008);
        GlStateManager._glBindBuffer(35051, ((GlBuffer)destination).handle());
        GlStateManager._pixelStore(3333, source.getFormat().byteAlignment());
        GlStateManager._pixelStore(3330, width);
        if (isDepth) {
            GlStateManager._glReadBuffer(0);
        }
        GlStateManager._readPixels(x, y, width, height, GlConst.toGlExternalId(source.getFormat()), GlConst.toGlType(source.getFormat()), offset);
        RenderSystem.queueFencedTask(callback);
        GlStateManager._glFramebufferTexture2D(36008, isDepth ? 36096 : 36064, 3553, 0, mipLevel);
        GlStateManager._glBindFramebuffer(36008, 0);
        GlStateManager._glBindBuffer(35051, 0);
        int error = GlStateManager._getError();
        if (error != 0) {
            throw new IllegalStateException("Couldn't perform copyTobuffer for texture " + source.getLabel() + ": GL error " + error);
        }
    }

    @Override
    public void copyTextureToTexture(GpuTexture source, GpuTexture destination, int mipLevel, int destX, int destY, int sourceX, int sourceY, int width, int height) {
        GlStateManager.clearGlErrors();
        GlStateManager._disableScissorTest();
        boolean isDepth = source.getFormat().hasDepthAspect();
        int sourceId = ((GlTexture)source).glId();
        int destId = ((GlTexture)destination).glId();
        this.device.directStateAccess().bindFrameBufferTextures(this.readFbo, isDepth ? 0 : sourceId, isDepth ? sourceId : 0, mipLevel, 0);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, isDepth ? 0 : destId, isDepth ? destId : 0, mipLevel, 0);
        this.device.directStateAccess().blitFrameBuffers(this.readFbo, this.drawFbo, sourceX, sourceY, width, height, destX, destY, width, height, isDepth ? 256 : 16384, 9728);
        int error = GlStateManager._getError();
        if (error != 0) {
            throw new IllegalStateException("Couldn't perform copyToTexture for texture " + source.getLabel() + " to " + destination.getLabel() + ": GL error " + error);
        }
    }

    void presentTexture(long windowHandle, GpuTextureView textureView, int swapchainWidth, int swapchainHeight) {
        this.device.makeCurrent(windowHandle);
        int destY = Math.max(0, swapchainHeight - textureView.getHeight(0));
        int copyWidth = Math.min(swapchainWidth, textureView.getWidth(0));
        int copyHeight = Math.min(swapchainHeight, textureView.getHeight(0));
        GlStateManager._disableScissorTest();
        GlStateManager._viewport(0, 0, textureView.getWidth(0), textureView.getHeight(0));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(15);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, ((GlTexture)textureView.texture()).glId(), 0, 0, 0);
        this.device.directStateAccess().blitFrameBuffers(this.drawFbo, 0, 0, 0, copyWidth, copyHeight, 0, destY, copyWidth, copyHeight + destY, 16384, 9728);
    }

    @Override
    public GpuFence createFence() {
        return new GlFence(this);
    }

    protected void executeDraw(GlRenderPass renderPass, int baseVertex, int firstIndex, int drawCount, @Nullable IndexType indexType, int instanceCount, int firstInstance) {
        this.setupDraw(renderPass);
        if (indexType != null) {
            if (firstInstance > 0) {
                ARBBaseInstance.glDrawElementsInstancedBaseVertexBaseInstance((int)renderPass.pipeline.primitiveTopology(), (int)drawCount, (int)GlConst.toGl(indexType), (long)((long)firstIndex * (long)indexType.bytes), (int)instanceCount, (int)baseVertex, (int)firstInstance);
            } else {
                GL33C.glDrawElementsInstancedBaseVertex((int)renderPass.pipeline.primitiveTopology(), (int)drawCount, (int)GlConst.toGl(indexType), (long)((long)firstIndex * (long)indexType.bytes), (int)instanceCount, (int)baseVertex);
            }
        } else if (firstInstance > 0) {
            ARBBaseInstance.glDrawArraysInstancedBaseInstance((int)renderPass.pipeline.primitiveTopology(), (int)baseVertex, (int)drawCount, (int)instanceCount, (int)firstInstance);
        } else {
            GL33C.glDrawArraysInstanced((int)renderPass.pipeline.primitiveTopology(), (int)baseVertex, (int)drawCount, (int)instanceCount);
        }
    }

    public void executeDraws(GlRenderPass renderPass, @Nullable IndexType indexType, @Nullable PointerBuffer firstIndexOffsets, IntBuffer indexCounts, IntBuffer vertexOffsets, int drawCount) {
        this.setupDraw(renderPass);
        if (indexType == null) {
            GL33C.nglMultiDrawArrays((int)renderPass.pipeline.primitiveTopology(), (long)MemoryUtil.memAddress((IntBuffer)vertexOffsets), (long)MemoryUtil.memAddress((IntBuffer)indexCounts), (int)drawCount);
        } else {
            assert (firstIndexOffsets != null);
            GL33C.nglMultiDrawElementsBaseVertex((int)renderPass.pipeline.primitiveTopology(), (long)MemoryUtil.memAddress((IntBuffer)indexCounts), (int)GlConst.toGl(indexType), (long)MemoryUtil.memAddress((CustomBuffer)firstIndexOffsets), (int)drawCount, (long)MemoryUtil.memAddress((IntBuffer)vertexOffsets));
        }
    }

    protected void executeDrawIndirect(GlRenderPass renderPass, @Nullable IndexType indexType, GlBuffer commands, long offset, int drawCount) {
        this.setupDraw(renderPass);
        GlStateManager._glBindBuffer(36671, commands.handle());
        if (indexType == null) {
            if (drawCount > 1) {
                ARBMultiDrawIndirect.glMultiDrawArraysIndirect((int)renderPass.pipeline.primitiveTopology(), (long)offset, (int)drawCount, (int)0);
            } else {
                ARBDrawIndirect.glDrawArraysIndirect((int)renderPass.pipeline.primitiveTopology(), (long)offset);
            }
        } else if (drawCount > 1) {
            ARBMultiDrawIndirect.glMultiDrawElementsIndirect((int)renderPass.pipeline.primitiveTopology(), (int)GlConst.toGl(indexType), (long)offset, (int)drawCount, (int)0);
        } else {
            ARBDrawIndirect.glDrawElementsIndirect((int)renderPass.pipeline.primitiveTopology(), (int)GlConst.toGl(indexType), (long)offset);
        }
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void setupDraw(GlRenderPass renderPass) {
        if (renderPass.vertexBufferDirty) {
            renderPass.pipeline.vertexArray().bind(renderPass.vertexBuffers);
            renderPass.vertexBufferDirty = false;
        }
        if (renderPass.indexBufferDirty) {
            renderPass.indexBufferDirty = false;
            GlStateManager._glBindBuffer(34963, ((GlBuffer)renderPass.indexBuffer).handle());
        }
        if (this.lastPipeline != renderPass.pipeline) {
            renderPass.pipeline.bind();
            this.lastPipeline = renderPass.pipeline;
        }
        GlProgram glProgram = renderPass.pipeline.program();
        if (renderPass.pushConstantsDirty) {
            renderPass.pushConstantsDirty = false;
            Uniform.Ubo pushConstantUBO = glProgram.pushConstant();
            if (pushConstantUBO != null) {
                assert (renderPass.pushConstants != null);
                GL33C.glBindBufferRange((int)35345, (int)pushConstantUBO.blockBinding(), (int)((GlBuffer)renderPass.pushConstants.buffer()).handle(), (long)renderPass.pushConstants.offset(), (long)renderPass.pushConstants.length());
            }
        }
        if (renderPass.anyUniformDirty) {
            renderPass.anyUniformDirty = false;
            block9: for (int i = 0; i < renderPass.dirtyUniforms.size(); ++i) {
                int target;
                int n;
                int n2;
                Uniform uniform;
                if (!renderPass.dirtyUniforms.getBoolean(i)) continue;
                renderPass.dirtyUniforms.set(i, false);
                Uniform dirtyUniform = glProgram.getUniform(i);
                if (dirtyUniform == null) continue;
                Objects.requireNonNull(dirtyUniform);
                int n3 = 0;
                switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Uniform.Ubo.class, Uniform.Utb.class, Uniform.Sampler.class}, (Uniform)uniform, n3)) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        Uniform.Ubo ubo = (Uniform.Ubo)uniform;
                        try {
                            int n4 = n2 = ubo.blockBinding();
                        }
                        catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }
                    }
                    int blockBinding = n2;
                    GpuBufferSlice bufferView = (GpuBufferSlice)renderPass.uniforms.get(i);
                    GL33C.glBindBufferRange((int)35345, (int)blockBinding, (int)((GlBuffer)bufferView.buffer()).handle(), (long)bufferView.offset(), (long)bufferView.length());
                    continue block9;
                    case 1: {
                        GpuFormat format;
                        int samplerIndex;
                        int n5;
                        Uniform.Utb utb = (Uniform.Utb)uniform;
                        {
                            GpuFormat gpuFormat;
                            int n6 = n5 = utb.samplerIndex();
                            samplerIndex = n5;
                            format = gpuFormat = utb.format();
                            n6 = n5 = utb.texture();
                        }
                        int texture = n5;
                        GlStateManager._activeTexture(33984 + samplerIndex);
                        GL33C.glBindTexture((int)35882, (int)texture);
                        GpuBufferSlice bufferView2 = (GpuBufferSlice)renderPass.uniforms.get(i);
                        GL33C.glTexBuffer((int)35882, (int)GlConst.toGlInternalId(format), (int)((GlBuffer)bufferView2.buffer()).handle());
                        continue block9;
                    }
                    case 2: 
                }
                Uniform.Sampler sampler = (Uniform.Sampler)uniform;
                {
                    int n7 = n = sampler.samplerIndex();
                }
                int samplerIndex = n;
                TextureViewAndSampler viewAndSampler = (TextureViewAndSampler)renderPass.uniforms.get(i);
                if (viewAndSampler == null) continue;
                GlTextureView textureView = (GlTextureView)viewAndSampler.view();
                GlStateManager._activeTexture(33984 + samplerIndex);
                GlTexture texture = textureView.texture();
                if ((texture.usage() & 0x10) != 0) {
                    target = 34067;
                    GL33C.glBindTexture((int)34067, (int)texture.id);
                } else {
                    target = 3553;
                    GlStateManager._bindTexture(texture.id);
                }
                GL33C.glBindSampler((int)samplerIndex, (int)((GlSampler)viewAndSampler.sampler()).getId());
                GlStateManager._texParameter(target, 33084, textureView.baseMipLevel());
                GlStateManager._texParameter(target, 33085, textureView.baseMipLevel() + textureView.mipLevels() - 1);
            }
        }
        if (!renderPass.scissorStateDirty) return;
        renderPass.scissorStateDirty = false;
        if (renderPass.isScissorEnabled()) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(renderPass.getScissorX(), renderPass.getScissorY(), renderPass.getScissorWidth(), renderPass.getScissorHeight());
            return;
        }
        GlStateManager._disableScissorTest();
    }

    @Override
    public void submitRenderPass() {
        GlStateManager._glBindFramebuffer(36160, 0);
        this.device.debugLabels().popDebugGroup();
    }

    @Override
    public void writeTimestamp(GpuQueryPool pool, int index) {
        ((GlQueryPool)pool).writeTimestamp(index);
    }
}


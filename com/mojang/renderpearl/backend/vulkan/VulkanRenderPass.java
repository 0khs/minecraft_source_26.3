/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.EXTMultiDraw
 *  org.lwjgl.vulkan.KHRPushDescriptor
 *  org.lwjgl.vulkan.KHRSynchronization2
 *  org.lwjgl.vulkan.VK12
 *  org.lwjgl.vulkan.VkBufferViewCreateInfo
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDescriptorBufferInfo
 *  org.lwjgl.vulkan.VkDescriptorBufferInfo$Buffer
 *  org.lwjgl.vulkan.VkDescriptorImageInfo
 *  org.lwjgl.vulkan.VkDescriptorImageInfo$Buffer
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkDrawIndexedIndirectCommand
 *  org.lwjgl.vulkan.VkDrawIndirectCommand
 *  org.lwjgl.vulkan.VkMultiDrawIndexedInfoEXT
 *  org.lwjgl.vulkan.VkMultiDrawInfoEXT
 *  org.lwjgl.vulkan.VkRect2D
 *  org.lwjgl.vulkan.VkRect2D$Buffer
 *  org.lwjgl.vulkan.VkViewport
 *  org.lwjgl.vulkan.VkViewport$Buffer
 *  org.lwjgl.vulkan.VkWriteDescriptorSet
 *  org.lwjgl.vulkan.VkWriteDescriptorSet$Buffer
 */
package com.mojang.renderpearl.backend.vulkan;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanQueryPool;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTMultiDraw;
import org.lwjgl.vulkan.KHRPushDescriptor;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.lwjgl.vulkan.VkDrawIndirectCommand;
import org.lwjgl.vulkan.VkMultiDrawIndexedInfoEXT;
import org.lwjgl.vulkan.VkMultiDrawInfoEXT;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class VulkanRenderPass
implements RenderPassBackend {
    private final VulkanDevice device;
    private final VulkanCommandEncoder encoder;
    private final CheckpointExtension.CheckpointStorage checkpointStorage;
    private final @Nullable RenderPass.RenderArea renderArea;
    private final int outputWidth;
    private final int outputHeight;
    private final boolean hasDepth;
    private final Supplier<String> label;
    protected int pushedDebugGroups = 0;
    private final VkCommandBuffer commandBuffer;
    protected @Nullable VulkanRenderPipeline pipeline;
    private boolean anyDescriptorDirty = false;
    protected final ReferenceList<@Nullable Object> uniforms = new ReferenceArrayList();

    public VulkanRenderPass(VulkanDevice device, VulkanCommandEncoder encoder, VkCommandBuffer commandBuffer, CheckpointExtension.CheckpointStorage checkpointStorage, RenderPass.RenderArea renderArea, int outputWidth, int outputHeight, boolean hasDepth, Supplier<String> label) {
        this.device = device;
        this.encoder = encoder;
        this.commandBuffer = commandBuffer;
        this.checkpointStorage = checkpointStorage;
        this.renderArea = renderArea;
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.hasDepth = hasDepth;
        this.label = label;
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkViewport.Buffer viewport = VkViewport.calloc((int)1, (MemoryStack)stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width((float)outputWidth);
            viewport.height((float)outputHeight);
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);
            VK12.vkCmdSetViewport((VkCommandBuffer)this.commandBuffer(), (int)0, (VkViewport.Buffer)viewport);
            VulkanRenderPass.setScissor(stack, this.commandBuffer(), renderArea.x(), renderArea.y(), renderArea.width(), renderArea.height());
        }
    }

    private VkCommandBuffer commandBuffer() {
        return this.commandBuffer;
    }

    @Override
    public void pushDebugGroup(Supplier<String> label) {
        ++this.pushedDebugGroups;
        this.device.instance().debug().beginDebugGroup(this.commandBuffer(), label);
    }

    @Override
    public void popDebugGroup() {
        if (this.pushedDebugGroups == 0) {
            throw new IllegalStateException("Can't pop more debug groups than was pushed!");
        }
        --this.pushedDebugGroups;
        this.device.instance().debug().endDebugGroup(this.commandBuffer());
    }

    @Override
    public void setPipeline(BackendRenderPipeline pipeline) {
        if (!(pipeline instanceof VulkanRenderPipeline)) {
            throw new IllegalArgumentException("Pipeline must be instance of VulkanRenderPipeline");
        }
        VulkanRenderPipeline vulkanRenderPipeline = (VulkanRenderPipeline)pipeline;
        this.pipeline = vulkanRenderPipeline;
        this.anyDescriptorDirty = true;
        this.uniforms.clear();
        this.uniforms.size(vulkanRenderPipeline.uniforms().size());
        VK12.vkCmdBindPipeline((VkCommandBuffer)this.commandBuffer(), (int)0, (long)(this.hasDepth ? this.pipeline.withDepthPipeline() : this.pipeline.withoutDepthPipeline()));
    }

    @Override
    public void setUniform(int index, @Nullable Object value) {
        this.uniforms.set(index, value);
        this.anyDescriptorDirty = true;
    }

    @Override
    public void pushConstants(ByteBuffer value) {
        assert (this.pipeline != null);
        VK12.vkCmdPushConstants((VkCommandBuffer)this.commandBuffer(), (long)this.pipeline.pipelineLayout(), (int)Integer.MAX_VALUE, (int)0, (ByteBuffer)value);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VulkanRenderPass.setScissor(stack, this.commandBuffer(), x, y, width, height);
        }
    }

    private static void setScissor(MemoryStack stack, VkCommandBuffer commandBuffer, int x, int y, int width, int height) {
        VkRect2D.Buffer scissor = VkRect2D.calloc((int)1, (MemoryStack)stack);
        scissor.offset().set(x, y);
        scissor.extent().set(width, height);
        VK12.vkCmdSetScissor((VkCommandBuffer)commandBuffer, (int)0, (VkRect2D.Buffer)scissor);
    }

    @Override
    public void disableScissor() {
        if (this.renderArea != null) {
            this.enableScissor(this.renderArea.x(), this.renderArea.y(), this.renderArea.width(), this.renderArea.height());
        } else {
            this.enableScissor(0, 0, this.outputWidth, this.outputHeight);
        }
    }

    @Override
    public void setVertexBuffer(int slot, @Nullable GpuBufferSlice vertexBuffer) {
        if (vertexBuffer == null) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            long buffer = ((VulkanGpuBuffer)vertexBuffer.buffer()).vkBuffer();
            long offset = vertexBuffer.offset();
            VK12.vkCmdBindVertexBuffers((VkCommandBuffer)this.commandBuffer(), (int)slot, (LongBuffer)stack.longs(buffer), (LongBuffer)stack.longs(offset));
        }
    }

    @Override
    public void setIndexBuffer(GpuBuffer indexBuffer, IndexType indexType) {
        int type = switch (indexType) {
            default -> throw new MatchException(null, null);
            case IndexType.SHORT -> 0;
            case IndexType.INT -> 1;
        };
        VK12.vkCmdBindIndexBuffer((VkCommandBuffer)this.commandBuffer(), (long)((VulkanGpuBuffer)indexBuffer).vkBuffer(), (long)0L, (int)type);
    }

    @Override
    public void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
        this.pushDescriptors();
        VK12.vkCmdDrawIndexed((VkCommandBuffer)this.commandBuffer(), (int)indexCount, (int)instanceCount, (int)firstIndex, (int)vertexOffset, (int)firstInstance);
    }

    @Override
    public void multiDrawIndexed(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
        this.pushDescriptors();
        EXTMultiDraw.nvkCmdDrawMultiIndexedEXT((VkCommandBuffer)this.commandBuffer(), (int)drawCount, (long)MemoryUtil.memAddress((IntBuffer)drawParameters), (int)instanceCount, (int)firstInstance, (int)VkMultiDrawIndexedInfoEXT.SIZEOF, (long)0L);
    }

    @Override
    public void multiDrawIndexed(PointerBuffer firstIndexOffsets, IntBuffer indexCounts, IntBuffer vertexOffsets, int drawCount) {
        throw new UnsupportedOperationException("Vulkan does not support the multiDrawDirectSeparate device feature");
    }

    @Override
    public void drawIndexedIndirect(GpuBufferSlice commands, int drawCount) {
        this.pushDescriptors();
        VK12.vkCmdDrawIndexedIndirect((VkCommandBuffer)this.commandBuffer(), (long)((VulkanGpuBuffer)commands.buffer()).vkBuffer(), (long)commands.offset(), (int)drawCount, (int)VkDrawIndexedIndirectCommand.SIZEOF);
    }

    @Override
    public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        this.pushDescriptors();
        VK12.vkCmdDraw((VkCommandBuffer)this.commandBuffer(), (int)vertexCount, (int)instanceCount, (int)firstVertex, (int)firstInstance);
    }

    @Override
    public void multiDraw(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
        this.pushDescriptors();
        EXTMultiDraw.nvkCmdDrawMultiEXT((VkCommandBuffer)this.commandBuffer(), (int)drawCount, (long)MemoryUtil.memAddress((IntBuffer)drawParameters), (int)instanceCount, (int)firstInstance, (int)VkMultiDrawInfoEXT.SIZEOF);
    }

    @Override
    public void multiDraw(IntBuffer firstVertices, IntBuffer vertexCounts, int drawCount) {
        throw new UnsupportedOperationException("Vulkan does not support the multiDrawDirectSeparate device feature");
    }

    @Override
    public void drawIndirect(GpuBufferSlice commands, int drawCount) {
        this.pushDescriptors();
        VK12.vkCmdDrawIndirect((VkCommandBuffer)this.commandBuffer(), (long)((VulkanGpuBuffer)commands.buffer()).vkBuffer(), (long)commands.offset(), (int)drawCount, (int)VkDrawIndirectCommand.SIZEOF);
    }

    private void pushDescriptors() {
        if (!this.anyDescriptorDirty) {
            return;
        }
        assert (this.pipeline != null);
        List<BindGroupLayout.UniformDescription> uniforms = this.pipeline.uniforms();
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc((int)uniforms.size(), (MemoryStack)stack);
            for (int i = 0; i < uniforms.size(); ++i) {
                Record value;
                BindGroupLayout.UniformDescription uniform = uniforms.get(i);
                VkWriteDescriptorSet set = ((VkWriteDescriptorSet)writes.get()).sType$Default();
                set.dstBinding(i);
                set.dstArrayElement(0);
                set.descriptorCount(1);
                if (uniform.type() == UniformType.UNIFORM_BUFFER) {
                    GpuBufferSlice buffer = (GpuBufferSlice)this.uniforms.get(i);
                    if (buffer == null) {
                        throw new IllegalStateException("Missing uniform " + uniform.name() + " (should be " + String.valueOf((Object)uniform.type()) + ")");
                    }
                    VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc((int)1, (MemoryStack)stack);
                    bufferInfo.buffer(((VulkanGpuBuffer)buffer.buffer()).vkBuffer());
                    bufferInfo.offset(buffer.offset());
                    bufferInfo.range(buffer.length());
                    set.descriptorType(6);
                    set.pBufferInfo(bufferInfo);
                    continue;
                }
                if (uniform.type() == UniformType.COMBINED_IMAGE_SAMPLER) {
                    value = (TextureViewAndSampler)this.uniforms.get(i);
                    if (value == null) {
                        throw new IllegalStateException("Missing sampler " + uniform.name());
                    }
                    VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc((int)1, (MemoryStack)stack);
                    imageInfo.sampler(((VulkanGpuSampler)((TextureViewAndSampler)value).sampler()).vkSampler());
                    imageInfo.imageView(((VulkanGpuTextureView)((TextureViewAndSampler)value).view()).vkImageView());
                    imageInfo.imageLayout(1);
                    set.descriptorType(1);
                    set.pImageInfo(imageInfo);
                    continue;
                }
                if (uniform.type() != UniformType.TEXEL_BUFFER) continue;
                value = (GpuBufferSlice)this.uniforms.get(i);
                if (value == null) {
                    throw new IllegalStateException("Missing uniform " + uniform.name() + " (should be " + String.valueOf((Object)uniform.type()) + ")");
                }
                LongBuffer bufferViewPtr = stack.callocLong(1);
                try (MemoryStack memoryStack = stack.push();){
                    assert (uniform.gpuFormat() != null);
                    VkBufferViewCreateInfo viewCreateInfo = VkBufferViewCreateInfo.calloc((MemoryStack)stack).sType$Default();
                    viewCreateInfo.buffer(((VulkanGpuBuffer)((GpuBufferSlice)value).buffer()).vkBuffer());
                    viewCreateInfo.offset(((GpuBufferSlice)value).offset());
                    viewCreateInfo.range(((GpuBufferSlice)value).length());
                    viewCreateInfo.format(VulkanConst.toVk(uniform.gpuFormat()));
                    VulkanUtils.crashIfFailure(this.device, VK12.vkCreateBufferView((VkDevice)this.device.vkDevice(), (VkBufferViewCreateInfo)viewCreateInfo, null, (LongBuffer)bufferViewPtr), "Couldn't create buffer view for texel buffer");
                    long bufferViewHandle = bufferViewPtr.get(0);
                    this.encoder.queueForDestroy(() -> VK12.vkDestroyBufferView((VkDevice)this.device.vkDevice(), (long)bufferViewHandle, null));
                }
                set.descriptorType(4);
                set.pTexelBufferView(bufferViewPtr);
            }
            KHRPushDescriptor.vkCmdPushDescriptorSetKHR((VkCommandBuffer)this.commandBuffer(), (int)0, (long)this.pipeline.pipelineLayout(), (int)0, (VkWriteDescriptorSet.Buffer)((VkWriteDescriptorSet.Buffer)writes.flip()));
        }
        this.anyDescriptorDirty = false;
    }

    @Override
    public void writeTimestamp(GpuQueryPool pool, int index) {
        long queryPool = ((VulkanQueryPool)pool).vkQueryPool();
        VK12.vkResetQueryPool((VkDevice)this.device.vkDevice(), (long)queryPool, (int)index, (int)1);
        KHRSynchronization2.vkCmdWriteTimestamp2KHR((VkCommandBuffer)this.commandBuffer(), (long)65536L, (long)queryPool, (int)index);
    }

    public Supplier<String> getLabel() {
        return this.label;
    }
}


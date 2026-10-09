/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongList
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.Struct
 *  org.lwjgl.vulkan.VK12
 *  org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
 *  org.lwjgl.vulkan.VkDescriptorSetLayoutBinding$Buffer
 *  org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo
 *  org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo$Buffer
 *  org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState
 *  org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState$Buffer
 *  org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineLayoutCreateInfo
 *  org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR
 *  org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo
 *  org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo$Buffer
 *  org.lwjgl.vulkan.VkPipelineVertexInputDivisorStateCreateInfoEXT
 *  org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo
 *  org.lwjgl.vulkan.VkPushConstantRange
 *  org.lwjgl.vulkan.VkPushConstantRange$Buffer
 *  org.lwjgl.vulkan.VkShaderModuleCreateInfo
 *  org.lwjgl.vulkan.VkVertexInputAttributeDescription
 *  org.lwjgl.vulkan.VkVertexInputAttributeDescription$Buffer
 *  org.lwjgl.vulkan.VkVertexInputBindingDescription
 *  org.lwjgl.vulkan.VkVertexInputBindingDescription$Buffer
 *  org.lwjgl.vulkan.VkVertexInputBindingDivisorDescription$Buffer
 *  org.lwjgl.vulkan.VkVertexInputBindingDivisorDescriptionEXT
 *  org.lwjgl.vulkan.VkVertexInputBindingDivisorDescriptionEXT$Buffer
 */
package com.mojang.renderpearl.backend.vulkan;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.vulkan.Destroyable;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputDivisorStateCreateInfoEXT;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDivisorDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDivisorDescriptionEXT;

public final class VulkanRenderPipeline
implements BackendRenderPipeline,
Destroyable {
    private final VulkanDevice device;
    private final long withDepthPipeline;
    private final long withoutDepthPipeline;
    private final long pipelineLayout;
    private final long descriptorSetLayout;
    private final LongList shaderModules;
    private final List<BindGroupLayout.UniformDescription> uniforms;
    private boolean closed;

    public VulkanRenderPipeline(VulkanDevice device, long withDepthPipeline, long withoutDepthPipeline, long pipelineLayout, long descriptorSetLayout, LongList shaderModules, List<BindGroupLayout.UniformDescription> uniforms) {
        this.device = device;
        this.withDepthPipeline = withDepthPipeline;
        this.withoutDepthPipeline = withoutDepthPipeline;
        this.pipelineLayout = pipelineLayout;
        this.descriptorSetLayout = descriptorSetLayout;
        this.shaderModules = shaderModules;
        this.uniforms = uniforms;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.device.createCommandEncoder().queueForDestroy(this);
    }

    public static VulkanRenderPipeline compile(VulkanDevice device, BackendRenderPipeline.CreateInfo pipelineCreateInfo) {
        long pipelineLayout;
        long descriptorSetLayout;
        LongBuffer pointer;
        try (MemoryStack stack = MemoryStack.stackPush();){
            int descriptorCount = pipelineCreateInfo.uniforms().size();
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc((int)descriptorCount, (MemoryStack)stack);
            for (int i = 0; i < descriptorCount; ++i) {
                VkDescriptorSetLayoutBinding binding = VkDescriptorSetLayoutBinding.calloc((MemoryStack)stack);
                binding.descriptorType(switch (pipelineCreateInfo.uniforms().get(i).type()) {
                    default -> throw new MatchException(null, null);
                    case UniformType.UNIFORM_BUFFER -> 6;
                    case UniformType.COMBINED_IMAGE_SAMPLER -> 1;
                    case UniformType.TEXEL_BUFFER -> 4;
                });
                binding.descriptorCount(1);
                binding.binding(i);
                binding.stageFlags(17);
                bindings.put((Struct)binding);
            }
            bindings.flip();
            VkDescriptorSetLayoutCreateInfo setCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc((MemoryStack)stack).sType$Default();
            setCreateInfo.flags(1);
            setCreateInfo.pBindings(bindings);
            pointer = stack.callocLong(1);
            VulkanUtils.crashIfFailure(device, VK12.vkCreateDescriptorSetLayout((VkDevice)device.vkDevice(), (VkDescriptorSetLayoutCreateInfo)setCreateInfo, null, (LongBuffer)pointer), "Can't create descriptor set layout for " + pipelineCreateInfo.name());
            descriptorSetLayout = pointer.get(0);
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc((MemoryStack)stack).sType$Default();
            if (pipelineCreateInfo.pushConstantsSize() != 0) {
                VkPushConstantRange.Buffer range = VkPushConstantRange.calloc((int)1, (MemoryStack)stack);
                range.stageFlags(Integer.MAX_VALUE);
                range.offset(0);
                range.size(pipelineCreateInfo.pushConstantsSize());
                createInfo.pPushConstantRanges(range);
            }
            createInfo.pSetLayouts(stack.longs(descriptorSetLayout));
            pointer = stack.callocLong(1);
            VulkanUtils.crashIfFailure(device, VK12.vkCreatePipelineLayout((VkDevice)device.vkDevice(), (VkPipelineLayoutCreateInfo)createInfo, null, (LongBuffer)pointer), "Can't create pipeline for " + pipelineCreateInfo.name());
            pipelineLayout = pointer.get(0);
            device.instance().debug().setObjectName(device.vkDevice(), 17, pipelineLayout, () -> "Pipeline layout for " + pipelineCreateInfo.name());
        }
        LongArrayList compiledShaderModules = new LongArrayList();
        try (MemoryStack stack = MemoryStack.stackPush();){
            long withoutDepthPipeline;
            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc((int)pipelineCreateInfo.shaders().size(), (MemoryStack)stack);
            for (BackendRenderPipeline.CreateInfo.Shader shader : pipelineCreateInfo.shaders()) {
                long module;
                try (MemoryStack memoryStack = stack.push();){
                    VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.calloc((MemoryStack)stack).sType$Default().pCode(shader.module().spv());
                    LongBuffer pointer2 = stack.callocLong(1);
                    VulkanUtils.crashIfFailure(device, VK12.vkCreateShaderModule((VkDevice)device.vkDevice(), (VkShaderModuleCreateInfo)info, null, (LongBuffer)pointer2), "Can't compile " + shader.name() + " (" + String.valueOf((Object)shader.module().type()) + ") for pipeline " + pipelineCreateInfo.name());
                    device.instance().debug().setObjectName(device.vkDevice(), 15, pointer2.get(0), pipelineCreateInfo::name);
                    module = pointer2.get(0);
                    compiledShaderModules.add(module);
                }
                ByteBuffer memoryStack = stack.UTF8((CharSequence)shader.entryPoint());
                VkPipelineShaderStageCreateInfo stage = VkPipelineShaderStageCreateInfo.calloc((MemoryStack)stack).sType$Default().stage(VulkanConst.toVk(shader.module().type())).module(module).pName(memoryStack);
                shaderStages.put((Struct)stage);
            }
            shaderStages.flip();
            List<BackendRenderPipeline.CreateInfo.VertexBuffer> vertexBindings = pipelineCreateInfo.vertexBuffers();
            VkVertexInputBindingDescription.Buffer vertexBindingDescriptions = VkVertexInputBindingDescription.calloc((int)vertexBindings.size(), (MemoryStack)stack);
            VkVertexInputBindingDivisorDescriptionEXT.Buffer vertexBindingDivisorDescriptions = VkVertexInputBindingDivisorDescriptionEXT.calloc((int)vertexBindings.size(), (MemoryStack)stack);
            for (BackendRenderPipeline.CreateInfo.VertexBuffer vertexBuffer : vertexBindings) {
                VkVertexInputBindingDescription bindingDescription = VkVertexInputBindingDescription.calloc((MemoryStack)stack).binding(vertexBuffer.bufferSlot()).stride(vertexBuffer.stride()).inputRate(vertexBuffer.stepRate() > 0 ? 1 : 0);
                vertexBindingDescriptions.put((Struct)bindingDescription);
                if (vertexBuffer.stepRate() <= 0) continue;
                VkVertexInputBindingDivisorDescriptionEXT divisorBinding = VkVertexInputBindingDivisorDescriptionEXT.calloc((MemoryStack)stack).binding(vertexBuffer.bufferSlot()).divisor(vertexBuffer.stepRate());
                vertexBindingDivisorDescriptions.put((Struct)divisorBinding);
            }
            vertexBindingDescriptions.flip();
            vertexBindingDivisorDescriptions.flip();
            VkVertexInputAttributeDescription.Buffer vertexAttributeDescriptions = VkVertexInputAttributeDescription.calloc((int)pipelineCreateInfo.attribBindings().size(), (MemoryStack)stack);
            for (BackendRenderPipeline.CreateInfo.AttribBinding attribBinding : pipelineCreateInfo.attribBindings()) {
                VkVertexInputAttributeDescription attributeDescription = VkVertexInputAttributeDescription.calloc((MemoryStack)stack).location(attribBinding.location()).binding(attribBinding.bufferSlot()).offset(attribBinding.offset()).format(VulkanConst.toVk(attribBinding.format()));
                vertexAttributeDescriptions.put((Struct)attributeDescription);
            }
            vertexAttributeDescriptions.flip();
            VkPipelineVertexInputDivisorStateCreateInfoEXT vkPipelineVertexInputDivisorStateCreateInfoEXT = VkPipelineVertexInputDivisorStateCreateInfoEXT.calloc((MemoryStack)stack).sType$Default().pVertexBindingDivisors((VkVertexInputBindingDivisorDescription.Buffer)vertexBindingDivisorDescriptions);
            VkPipelineVertexInputStateCreateInfo vertexInputState = VkPipelineVertexInputStateCreateInfo.calloc((MemoryStack)stack).sType$Default().pVertexAttributeDescriptions(vertexAttributeDescriptions).pVertexBindingDescriptions(vertexBindingDescriptions);
            if (vkPipelineVertexInputDivisorStateCreateInfoEXT.vertexBindingDivisorCount() > 0) {
                vertexInputState.pNext(vkPipelineVertexInputDivisorStateCreateInfoEXT);
            }
            VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc((MemoryStack)stack).sType$Default().topology(VulkanConst.toVk(pipelineCreateInfo.primitiveTopology()));
            VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc((MemoryStack)stack).sType$Default().polygonMode(VulkanConst.toVk(pipelineCreateInfo.polygonMode())).cullMode(pipelineCreateInfo.cull() ? 2 : 0).frontFace(1).lineWidth(1.0f);
            VkPipelineDepthStencilStateCreateInfo vkDepthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc((MemoryStack)stack).sType$Default();
            DepthStencilState depthStencilState = pipelineCreateInfo.depthStencilState();
            if (depthStencilState != null) {
                rasterizationState.depthBiasEnable(depthStencilState.depthBiasConstant() != 0.0f || depthStencilState.depthBiasScaleFactor() != 0.0f);
                rasterizationState.depthBiasConstantFactor(depthStencilState.depthBiasConstant());
                rasterizationState.depthBiasSlopeFactor(depthStencilState.depthBiasScaleFactor());
                vkDepthStencilState.depthTestEnable(true);
                vkDepthStencilState.depthWriteEnable(depthStencilState.writeDepth());
                vkDepthStencilState.depthCompareOp(VulkanConst.toVk(depthStencilState.depthTest()));
            }
            List<@Nullable ColorTargetState> colorTargetStates = pipelineCreateInfo.colorTargetStates();
            VkPipelineColorBlendAttachmentState.Buffer blendAttachments = VkPipelineColorBlendAttachmentState.calloc((int)colorTargetStates.size(), (MemoryStack)stack);
            for (ColorTargetState colorTargetState : colorTargetStates) {
                blendAttachments.colorWriteMask(colorTargetState != null ? VulkanConst.toVk(colorTargetState) : 0);
                if (colorTargetState != null && colorTargetState.blendFunction().isPresent()) {
                    VulkanRenderPipeline.applyBlendInformation(blendAttachments, colorTargetState.blendFunction().get());
                }
                blendAttachments.position(blendAttachments.position() + 1);
            }
            blendAttachments.position(0);
            VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc((MemoryStack)stack).sType$Default().pAttachments(blendAttachments);
            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc((MemoryStack)stack).sType$Default().scissorCount(1).viewportCount(1);
            VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc((MemoryStack)stack).sType$Default().rasterizationSamples(1).sampleShadingEnable(false);
            VkPipelineDynamicStateCreateInfo dynamicStateInfo = VkPipelineDynamicStateCreateInfo.calloc((MemoryStack)stack).sType$Default().pDynamicStates(stack.ints(1, 0));
            VkPipelineRenderingCreateInfoKHR renderingInfo = VkPipelineRenderingCreateInfoKHR.calloc((MemoryStack)stack).sType$Default();
            IntBuffer colorAttachmentFormats = stack.mallocInt(colorTargetStates.size());
            for (int i = 0; i < colorTargetStates.size(); ++i) {
                ColorTargetState colorTargetState = colorTargetStates.get(i);
                colorAttachmentFormats.put(i, colorTargetState != null ? VulkanConst.toVk(colorTargetState.format()) : 0);
            }
            renderingInfo.pColorAttachmentFormats(colorAttachmentFormats);
            renderingInfo.depthAttachmentFormat(126);
            VkGraphicsPipelineCreateInfo.Buffer createInfo = VkGraphicsPipelineCreateInfo.calloc((int)1, (MemoryStack)stack).sType$Default().flags(0).pStages(shaderStages).pVertexInputState(vertexInputState).pInputAssemblyState(inputAssemblyState).pRasterizationState(rasterizationState).pDepthStencilState(vkDepthStencilState).pColorBlendState(colorBlendState).pViewportState(viewportState).pMultisampleState(multisampleState).pDynamicState(dynamicStateInfo).layout(pipelineLayout).pNext(renderingInfo);
            LongBuffer pointer3 = stack.callocLong(1);
            VulkanUtils.crashIfFailure(device, VK12.vkCreateGraphicsPipelines((VkDevice)device.vkDevice(), (long)0L, (VkGraphicsPipelineCreateInfo.Buffer)createInfo, null, (LongBuffer)pointer3), "Can't compile pipeline " + pipelineCreateInfo.name());
            long withDepthPipeline = pointer3.get(0);
            device.instance().debug().setObjectName(device.vkDevice(), 19, withDepthPipeline, () -> "Pipeline " + pipelineCreateInfo.name());
            if (depthStencilState == null) {
                renderingInfo.depthAttachmentFormat(0);
                VulkanUtils.crashIfFailure(device, VK12.vkCreateGraphicsPipelines((VkDevice)device.vkDevice(), (long)0L, (VkGraphicsPipelineCreateInfo.Buffer)createInfo, null, (LongBuffer)pointer3), "Can't compile pipeline " + pipelineCreateInfo.name());
                withoutDepthPipeline = pointer3.get(0);
                device.instance().debug().setObjectName(device.vkDevice(), 19, withoutDepthPipeline, () -> "Pipeline " + pipelineCreateInfo.name());
            } else {
                withoutDepthPipeline = 0L;
            }
            VulkanRenderPipeline vulkanRenderPipeline = new VulkanRenderPipeline(device, withDepthPipeline, withoutDepthPipeline, pipelineLayout, descriptorSetLayout, (LongList)compiledShaderModules, pipelineCreateInfo.uniforms());
            return vulkanRenderPipeline;
        }
    }

    @Override
    public void destroy() {
        if (this.withDepthPipeline == 0L) {
            return;
        }
        VK12.vkDestroyPipeline((VkDevice)this.device.vkDevice(), (long)this.withoutDepthPipeline, null);
        VK12.vkDestroyPipeline((VkDevice)this.device.vkDevice(), (long)this.withDepthPipeline, null);
        VK12.vkDestroyPipelineLayout((VkDevice)this.device.vkDevice(), (long)this.pipelineLayout, null);
        VK12.vkDestroyDescriptorSetLayout((VkDevice)this.device.vkDevice(), (long)this.descriptorSetLayout, null);
        for (int i = 0; i < this.shaderModules.size(); ++i) {
            VK12.vkDestroyShaderModule((VkDevice)this.device.vkDevice(), (long)this.shaderModules.getLong(i), null);
        }
    }

    private static void applyBlendInformation(VkPipelineColorBlendAttachmentState.Buffer blendAttachments, BlendFunction blendFunction) {
        blendAttachments.blendEnable(true).colorBlendOp(VulkanConst.toVk(blendFunction.color().op())).alphaBlendOp(VulkanConst.toVk(blendFunction.alpha().op())).dstAlphaBlendFactor(VulkanConst.toVk(blendFunction.alpha().destFactor())).dstColorBlendFactor(VulkanConst.toVk(blendFunction.color().destFactor())).srcAlphaBlendFactor(VulkanConst.toVk(blendFunction.alpha().sourceFactor())).srcColorBlendFactor(VulkanConst.toVk(blendFunction.color().sourceFactor()));
    }

    public VulkanDevice device() {
        return this.device;
    }

    public long withDepthPipeline() {
        return this.withDepthPipeline;
    }

    public long withoutDepthPipeline() {
        return this.withoutDepthPipeline;
    }

    public long pipelineLayout() {
        return this.pipelineLayout;
    }

    public List<BindGroupLayout.UniformDescription> uniforms() {
        return this.uniforms;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntIntPair
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.util.vma.Vma
 *  org.lwjgl.vulkan.EXTCalibratedTimestamps
 *  org.lwjgl.vulkan.VK12
 *  org.lwjgl.vulkan.VkBufferCopy
 *  org.lwjgl.vulkan.VkBufferCopy$Buffer
 *  org.lwjgl.vulkan.VkCalibratedTimestampInfoEXT
 *  org.lwjgl.vulkan.VkCalibratedTimestampInfoEXT$Buffer
 *  org.lwjgl.vulkan.VkCalibratedTimestampInfoKHR
 *  org.lwjgl.vulkan.VkCalibratedTimestampInfoKHR$Buffer
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkPhysicalDeviceLimits
 *  org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Properties
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.vulkan;

import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.DeviceLimits;
import com.mojang.renderpearl.api.device.HintsAndWorkarounds;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanInstance;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanQueryPool;
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.lang.invoke.CallSite;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.EXTCalibratedTimestamps;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCalibratedTimestampInfoEXT;
import org.lwjgl.vulkan.VkCalibratedTimestampInfoKHR;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Properties;
import org.slf4j.Logger;

public class VulkanDevice
implements GpuDeviceBackend {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VulkanInstance instance;
    private final VkDevice vkDevice;
    private final long vma;
    private final DeviceInfo deviceInfo;
    private final VulkanQueue graphicsQueue;
    private final VulkanQueue computeQueue;
    private final VulkanQueue transferQueue;
    private final boolean isIntegratedIntelMoltenVK;
    private final FeatureSet enabledFeatures;
    private final VulkanCommandEncoder commandEncoder;
    private final CheckpointExtension checkpointExtension;

    public VulkanDevice(VulkanInstance instance, VulkanPhysicalDevice physicalDevice, FeatureSet enabledFeatureSet, VkDevice vkDevice, long vma, CheckpointExtension checkpointExtension) {
        this.instance = instance;
        this.vkDevice = vkDevice;
        this.vma = vma;
        this.enabledFeatures = enabledFeatureSet;
        this.checkpointExtension = checkpointExtension;
        HashSet<CallSite> extensionNames = new HashSet<CallSite>();
        for (String name : instance.getEnabledExtensions()) {
            extensionNames.add((CallSite)((Object)(name + " (I)")));
        }
        for (String name : enabledFeatureSet.extensions()) {
            extensionNames.add((CallSite)((Object)(name + " (D)")));
        }
        VkPhysicalDeviceLimits limits = physicalDevice.vkPhysicalDeviceProperties().limits();
        VkPhysicalDeviceVulkan11Properties vk11Properties = physicalDevice.vkPhysicalDeviceVulkan11Properties();
        int indirectDrawCount = Integer.compareUnsigned(limits.maxDrawIndirectCount(), Integer.MAX_VALUE) > 0 ? Integer.MAX_VALUE : limits.maxDrawIndirectCount();
        this.deviceInfo = new DeviceInfo(physicalDevice.deviceName(), physicalDevice.vendorName(), physicalDevice.driverInfo(), true, "Vulkan", limits.timestampPeriod(), new DeviceLimits((int)limits.maxSamplerAnisotropy(), (int)limits.minUniformBufferOffsetAlignment(), limits.maxImageDimension2D(), vk11Properties.maxMemoryAllocationSize() < 0L ? Long.MAX_VALUE : vk11Properties.maxMemoryAllocationSize(), physicalDevice.vkPhysicalDeviceMultiDrawPropertiesEXT().maxMultiDrawCount() < 0 ? Integer.MAX_VALUE : physicalDevice.vkPhysicalDeviceMultiDrawPropertiesEXT().maxMultiDrawCount(), limits.maxColorAttachments(), indirectDrawCount), new DeviceFeatures(enabledFeatureSet.contains(VulkanFeatureSets.WIREFRAME_FEATURESET), true, enabledFeatureSet.contains(VulkanFeatureSets.MULTI_DRAW_FEATURESET), false, true, true, true, true), Collections.unmodifiableSet(extensionNames), new HintsAndWorkarounds(false, false, Util.isAppleSiliconMac(physicalDevice.deviceName()), false), physicalDevice.deviceType());
        IntIntPair graphicsQueueFamily = physicalDevice.graphicsQueueFamilyAndIndex();
        assert (graphicsQueueFamily != null);
        IntIntPair computeQueueFamily = physicalDevice.computeQueueFamilyAndIndex();
        IntIntPair transferQueueFamily = physicalDevice.transferQueueFamilyAndIndex();
        this.graphicsQueue = new VulkanQueue(this, graphicsQueueFamily.leftInt(), graphicsQueueFamily.rightInt());
        this.computeQueue = computeQueueFamily != null ? new VulkanQueue(this, computeQueueFamily.leftInt(), computeQueueFamily.rightInt()) : this.graphicsQueue;
        this.transferQueue = transferQueueFamily != null ? new VulkanQueue(this, transferQueueFamily.leftInt(), transferQueueFamily.rightInt()) : this.computeQueue;
        this.isIntegratedIntelMoltenVK = physicalDevice.vkPhysicalDeviceProperties().deviceType() == 1 && physicalDevice.vkPhysicalDeviceProperties().vendorID() == 32902 && physicalDevice.vkPhysicalDeviceDriverProperties().driverID() == 14;
        physicalDevice.close();
        this.commandEncoder = new VulkanCommandEncoder(this);
    }

    @Override
    public void close() {
        this.checkpointExtension.close();
        this.commandEncoder.destroy();
        Vma.vmaDestroyAllocator((long)this.vma);
        VK12.vkDestroyDevice((VkDevice)this.vkDevice, null);
        this.instance.close();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    public VulkanInstance instance() {
        return this.instance;
    }

    public VkDevice vkDevice() {
        return this.vkDevice;
    }

    public VulkanQueue graphicsQueue() {
        return this.graphicsQueue;
    }

    public VulkanQueue computeQueue() {
        return this.computeQueue;
    }

    public VulkanQueue transferQueue() {
        return this.transferQueue;
    }

    public long vma() {
        return this.vma;
    }

    @Override
    public GpuSurfaceBackend createSurface(long windowHandle, BooleanSupplier isIconified) {
        return new VulkanGpuSurface(this, windowHandle);
    }

    @Override
    public VulkanCommandEncoder createCommandEncoder() {
        return this.commandEncoder;
    }

    @Override
    public GpuSampler createSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        return new VulkanGpuSampler(this, addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
    }

    @Override
    public GpuTexture createTexture(@Nullable String label, @GpuTexture.Usage int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        return new VulkanGpuTexture(this, usage, this.isDebuggingEnabled() && label != null ? label : "", format, width, height, depthOrLayers, mipLevels);
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
        return new VulkanGpuTextureView(this, (VulkanGpuTexture)texture, baseMipLevel, mipLevels);
    }

    @Override
    public VulkanGpuBuffer createBuffer(@Nullable Supplier<String> label, @GpuBuffer.Usage int usage, long size) {
        return new VulkanGpuBuffer.Direct(this, label, usage, size, this.isIntegratedIntelMoltenVK);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, @GpuBuffer.Usage int usage, ByteBuffer data) {
        GpuBuffer buffer = this.createBuffer((Supplier)label, usage | 8, (long)data.remaining());
        GpuBufferSlice stagingBuffer = this.commandEncoder.transientMemory().uploadStaging(data, 1L, 16);
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkBufferCopy.Buffer regions = VkBufferCopy.calloc((int)1, (MemoryStack)stack).srcOffset(stagingBuffer.offset()).dstOffset(0L).size((long)data.remaining());
            VK12.vkCmdCopyBuffer((VkCommandBuffer)this.commandEncoder.objectInitCommandBuffer(), (long)((VulkanGpuBuffer)stagingBuffer.buffer()).vkBuffer(), (long)((VulkanGpuBuffer)buffer).vkBuffer(), (VkBufferCopy.Buffer)regions);
        }
        return buffer;
    }

    @Override
    public List<String> getLastDebugMessages() {
        return List.of();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.instance.debug().enabled();
    }

    @Override
    public BackendRenderPipeline.Pending compilePipeline(BackendRenderPipeline.CreateInfo pipelineCreateInfo) {
        VulkanRenderPipeline pipeline = VulkanRenderPipeline.compile(this, pipelineCreateInfo);
        return () -> pipeline;
    }

    @Override
    public GpuQueryPool createTimestampQueryPool(int size) {
        return new VulkanQueryPool(this, size);
    }

    @Override
    public long getTimestampCalibrationOffset() {
        double timestampPeriod = this.deviceInfo.timestampPeriod();
        if (!this.enabledFeatures.contains(VulkanFeatureSets.CALIBRATED_TIMESTAMP_FEATURESET)) {
            long deviceTime = this.commandEncoder.getTimestampNow();
            long hostTime = System.nanoTime();
            long deviceTimeInNanos = timestampPeriod == 1.0 ? deviceTime : (long)((double)deviceTime * timestampPeriod);
            return hostTime - deviceTimeInNanos;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkCalibratedTimestampInfoEXT.Buffer infos = VkCalibratedTimestampInfoEXT.calloc((int)2, (MemoryStack)stack);
            ((VkCalibratedTimestampInfoKHR)infos.get(0)).sType$Default().timeDomain(0);
            ((VkCalibratedTimestampInfoKHR)infos.get(1)).sType$Default().timeDomain(1);
            LongBuffer timestampValues = stack.callocLong(infos.capacity());
            LongBuffer deviation = stack.callocLong(1);
            EXTCalibratedTimestamps.vkGetCalibratedTimestampsEXT((VkDevice)this.vkDevice, (VkCalibratedTimestampInfoKHR.Buffer)infos, (LongBuffer)timestampValues, (LongBuffer)deviation);
            long deviceTime = timestampValues.get(0);
            long hostTime = timestampValues.get(1);
            long deviceInNanos = timestampPeriod == 1.0 ? deviceTime : (long)((double)deviceTime * timestampPeriod);
            long l = hostTime - deviceInNanos;
            return l;
        }
    }

    public CheckpointExtension checkpointExtension() {
        return this.checkpointExtension;
    }
}


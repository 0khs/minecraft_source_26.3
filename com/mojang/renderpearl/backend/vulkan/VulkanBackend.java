/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLVideo
 *  org.lwjgl.sdl.SDLVulkan
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.SharedLibrary
 *  org.lwjgl.util.vma.Vma
 *  org.lwjgl.util.vma.VmaAllocatorCreateInfo
 *  org.lwjgl.util.vma.VmaVulkanFunctions
 *  org.lwjgl.vulkan.VK
 *  org.lwjgl.vulkan.VK12
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkDeviceCreateInfo
 *  org.lwjgl.vulkan.VkDeviceQueueCreateInfo
 *  org.lwjgl.vulkan.VkDeviceQueueCreateInfo$Buffer
 *  org.lwjgl.vulkan.VkInstance
 *  org.lwjgl.vulkan.VkPhysicalDevice
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
 *  org.lwjgl.vulkan.VkPhysicalDeviceProperties
 *  org.lwjgl.vulkan.VkPhysicalDeviceProperties2
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.vulkan;

import com.mojang.blaze3d.platform.NativeLibrariesBootstrap;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.VulkanInstance;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import com.mojang.renderpearl.backend.vulkan.checkpoints.AmdCheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.checkpoints.NoopCheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.checkpoints.NvidiaCheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDLVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.slf4j.Logger;

public class VulkanBackend
implements GpuBackend {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean libraryLoaded;
    private @Nullable BackendCreationException libraryLoadFailure;

    @Override
    public String getName() {
        return "Vulkan";
    }

    @Override
    public void loadLibrary() throws BackendCreationException {
        if (this.libraryLoaded) {
            return;
        }
        if (this.libraryLoadFailure != null) {
            throw this.libraryLoadFailure;
        }
        if (!NativeLibrariesBootstrap.isVulkanLoaderAvailable()) {
            this.libraryLoadFailure = new BackendCreationException("Vulkan loader library is missing", BackendCreationException.Reason.VULKAN_LOADER_MISSING);
            throw this.libraryLoadFailure;
        }
        if (!SDLVulkan.SDL_Vulkan_LoadLibrary((CharSequence)((SharedLibrary)VK.getFunctionProvider()).getPath())) {
            this.libraryLoadFailure = new BackendCreationException("Vulkan is not supported: " + Objects.requireNonNullElse(SDLError.SDL_GetError(), "<no error>"), BackendCreationException.Reason.PLATFORM_ERROR);
            throw this.libraryLoadFailure;
        }
        if (VK.getFunctionProvider().getFunctionAddress((CharSequence)"vkGetInstanceProcAddr") != SDLVulkan.SDL_Vulkan_GetVkGetInstanceProcAddr()) {
            this.libraryLoadFailure = new BackendCreationException("vkGetInstanceProcAddr mismatch", BackendCreationException.Reason.PLATFORM_ERROR);
            SDLVulkan.SDL_Vulkan_UnloadLibrary();
            throw this.libraryLoadFailure;
        }
        this.libraryLoaded = true;
    }

    @Override
    public void unloadLibrary() {
        if (!this.libraryLoaded) {
            return;
        }
        SDLVulkan.SDL_Vulkan_UnloadLibrary();
        this.libraryLoaded = false;
    }

    public static @Nullable BackendCreationException checkBackendAvailable() {
        VulkanBackend probe = new VulkanBackend();
        try {
            probe.loadLibrary();
        }
        catch (BackendCreationException e) {
            return e;
        }
        try {
            BackendCreationException backendCreationException = probe.checkBackendAvailableWithLoadedLibrary();
            return backendCreationException;
        }
        finally {
            probe.unloadLibrary();
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private @Nullable BackendCreationException checkBackendAvailableWithLoadedLibrary() {
        Set<FeatureSet> requiredFeatureSets = VulkanFeatureSets.requiredFeatureSets();
        Set<FeatureSet> requiredIfExtensionsAvailableFeatureSets = VulkanFeatureSets.requiredIfExtensionsAvailableFeatureSets();
        try (VulkanInstance instance = new VulkanInstance(0, false, false);){
            VulkanPhysicalDevice physicalDevice = VulkanBackend.findPhysicalDevice(instance, requiredFeatureSets, requiredIfExtensionsAvailableFeatureSets);
            try {
                BackendCreationException backendCreationException = null;
                if (physicalDevice != null) {
                    physicalDevice.close();
                }
                return backendCreationException;
            }
            catch (Throwable throwable) {
                if (physicalDevice != null) {
                    try {
                        physicalDevice.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        catch (BackendCreationException e) {
            return e;
        }
    }

    @Override
    public long createWindow(@Nullable String title, int width, int height, long flags) {
        return SDLVideo.SDL_CreateWindow((CharSequence)title, (int)width, (int)height, (long)(0x10000000L | flags));
    }

    @Override
    public GpuDevice createDevice(GpuDebugOptions debugOptions) throws BackendCreationException {
        FeatureSet enabledFeatures;
        if (!NativeLibrariesBootstrap.isVulkanLoaderAvailable()) {
            throw new BackendCreationException("Vulkan loader library is missing", BackendCreationException.Reason.VULKAN_LOADER_MISSING);
        }
        Set<FeatureSet> requiredFeatureSets = VulkanFeatureSets.requiredFeatureSets();
        Set<FeatureSet> requiredIfExtensionsAvailableFeatureSets = VulkanFeatureSets.requiredIfExtensionsAvailableFeatureSets();
        Set<FeatureSet> optionalFeatureSets = VulkanFeatureSets.optionalFeatureSets();
        VulkanInstance instance = null;
        VulkanPhysicalDevice physicalDevice = null;
        VkDevice device = null;
        long vma = 0L;
        CheckpointExtension checkpointExtension = NoopCheckpointExtension.INSTANCE;
        try {
            boolean renderdocAttached = "1".equals(System.getenv("ENABLE_VULKAN_RENDERDOC_CAPTURE"));
            instance = new VulkanInstance(debugOptions.logLevel(), debugOptions.useLabels() || renderdocAttached, debugOptions.useValidationLayers());
            physicalDevice = VulkanBackend.findPhysicalDevice(instance, requiredFeatureSets, requiredIfExtensionsAvailableFeatureSets);
            Set<String> deviceExtensions = VulkanUtils.enumerateExtensions(physicalDevice.vkPhysicalDevice());
            ObjectOpenHashSet enabledFeatureSets = new ObjectOpenHashSet(requiredFeatureSets);
            for (FeatureSet featureSet : requiredIfExtensionsAvailableFeatureSets) {
                if (!featureSet.isSupported(physicalDevice.vkPhysicalDevice(), deviceExtensions)) continue;
                enabledFeatureSets.add(featureSet);
                LOGGER.info("Enabling required for device FeatureSet [{}]", (Object)featureSet.name());
            }
            for (FeatureSet featureSet : optionalFeatureSets) {
                if (featureSet.isSupported(physicalDevice.vkPhysicalDevice(), deviceExtensions)) {
                    if (featureSet.checkCondition(physicalDevice.vkPhysicalDevice())) {
                        enabledFeatureSets.add(featureSet);
                        LOGGER.info("Enabling optional FeatureSet [{}]", (Object)featureSet.name());
                        continue;
                    }
                    LOGGER.info("Optional FeatureSet [{}] supported but device condition failed and will not be enabled", (Object)featureSet.name());
                    continue;
                }
                LOGGER.info("Optional FeatureSet [{}] not supported", (Object)featureSet.name());
            }
            if (enabledFeatureSets.contains(VulkanFeatureSets.AMD_BUFFER_MARKER_FEATURESET)) {
                checkpointExtension = new AmdCheckpointExtension();
            } else if (enabledFeatureSets.contains(VulkanFeatureSets.NV_DIAGNOSTIC_CHECKPOINT_FEATURESET)) {
                checkpointExtension = new NvidiaCheckpointExtension();
            }
            enabledFeatures = new FeatureSet("Enabled", (Collection<FeatureSet>)enabledFeatureSets);
            device = VulkanBackend.createDevice(enabledFeatures, physicalDevice);
            vma = VulkanBackend.createVma(device);
        }
        catch (BackendCreationException e) {
            if (vma != 0L) {
                Vma.vmaDestroyAllocator((long)vma);
            }
            if (device != null) {
                VK12.vkDestroyDevice(device, null);
            }
            if (physicalDevice != null) {
                physicalDevice.close();
            }
            if (instance != null) {
                instance.close();
            }
            throw e;
        }
        return new FrontendGpuDevice(new VulkanDevice(instance, physicalDevice, enabledFeatures, device, vma, checkpointExtension));
    }

    private static long createVma(VkDevice vkDevice) throws BackendCreationException {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.calloc((MemoryStack)stack).set(vkDevice.getPhysicalDevice().getInstance(), vkDevice);
            VmaAllocatorCreateInfo createInfo = VmaAllocatorCreateInfo.calloc((MemoryStack)stack).instance(vkDevice.getPhysicalDevice().getInstance()).vulkanApiVersion(VK12.VK_API_VERSION_1_2).device(vkDevice).physicalDevice(vkDevice.getPhysicalDevice()).pVulkanFunctions(vmaVulkanFunctions);
            PointerBuffer pointer = stack.callocPointer(1);
            VulkanUtils.throwIfFailure(Vma.vmaCreateAllocator((VmaAllocatorCreateInfo)createInfo, (PointerBuffer)pointer), "Failed to create VMA allocator", BackendCreationException.Reason.OTHER);
            long l = pointer.get(0);
            return l;
        }
    }

    private static VulkanPhysicalDevice findPhysicalDevice(VulkanInstance instance, Set<FeatureSet> requiredFeatureSets, Set<FeatureSet> requiredIfExtensionsAvailable) throws BackendCreationException {
        BackendCreationException deviceFailureReason = null;
        VkPhysicalDevice selectedDevice = null;
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer intBuffer = stack.callocInt(1);
            VulkanUtils.throwIfFailure(VK12.vkEnumeratePhysicalDevices((VkInstance)instance.vkInstance(), (IntBuffer)intBuffer, null), "Failed to get number of physical devices", BackendCreationException.Reason.VULKAN_NO_DEVICE);
            if (intBuffer.get(0) == 0) {
                throw new BackendCreationException("No Vulkan capable devices", BackendCreationException.Reason.VULKAN_NO_DEVICE);
            }
            PointerBuffer pPhysicalDevices = stack.callocPointer(intBuffer.get(0));
            VulkanUtils.throwIfFailure(VK12.vkEnumeratePhysicalDevices((VkInstance)instance.vkInstance(), (IntBuffer)intBuffer, (PointerBuffer)pPhysicalDevices), "Failed to get physical devices", BackendCreationException.Reason.VULKAN_NO_DEVICE);
            int numDevices = intBuffer.get(0);
            if (numDevices == 0) {
                throw new BackendCreationException("No Vulkan capable devices", BackendCreationException.Reason.VULKAN_NO_DEVICE);
            }
            for (int i = 0; i < numDevices; ++i) {
                if (pPhysicalDevices.get(i) == 0L) continue;
                VkPhysicalDevice currentDevice = new VkPhysicalDevice(pPhysicalDevices.get(i), instance.vkInstance());
                BackendCreationException failureReason = VulkanBackend.checkDeviceSuitability(currentDevice, requiredFeatureSets, requiredIfExtensionsAvailable);
                if (failureReason != null) {
                    if (deviceFailureReason != null) continue;
                    deviceFailureReason = failureReason;
                    continue;
                }
                if (selectedDevice == null) {
                    selectedDevice = currentDevice;
                    continue;
                }
                if (!VulkanBackend.isDeviceDiscrete(currentDevice) || VulkanBackend.isDeviceDiscrete(selectedDevice)) continue;
                LOGGER.info("Preferring discrete GPU: {}", (Object)VulkanBackend.getDeviceName(currentDevice));
                selectedDevice = currentDevice;
                break;
            }
        }
        if (selectedDevice == null) {
            if (deviceFailureReason == null) {
                throw new BackendCreationException("No Vulkan capable devices", BackendCreationException.Reason.VULKAN_NO_DEVICE);
            }
            throw deviceFailureReason;
        }
        return new VulkanPhysicalDevice(selectedDevice);
    }

    private static boolean deviceMeetsFeatureQueryRequirements(VkPhysicalDevice vkPhysicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc((MemoryStack)stack);
            VK12.vkGetPhysicalDeviceProperties((VkPhysicalDevice)vkPhysicalDevice, (VkPhysicalDeviceProperties)properties);
            boolean bl = properties.apiVersion() >= VK12.VK_API_VERSION_1_1;
            return bl;
        }
    }

    private static boolean isDeviceDiscrete(VkPhysicalDevice vkPhysicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPhysicalDeviceProperties2 deviceProperties = VkPhysicalDeviceProperties2.calloc((MemoryStack)stack).sType$Default();
            VK12.vkGetPhysicalDeviceProperties2((VkPhysicalDevice)vkPhysicalDevice, (VkPhysicalDeviceProperties2)deviceProperties);
            boolean bl = deviceProperties.properties().deviceType() == 2;
            return bl;
        }
    }

    private static String getDeviceName(VkPhysicalDevice vkPhysicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPhysicalDeviceProperties2 deviceProperties = VkPhysicalDeviceProperties2.calloc((MemoryStack)stack).sType$Default();
            VK12.vkGetPhysicalDeviceProperties2((VkPhysicalDevice)vkPhysicalDevice, (VkPhysicalDeviceProperties2)deviceProperties);
            String string = deviceProperties.properties().deviceNameString();
            return string;
        }
    }

    private static @Nullable BackendCreationException checkDeviceSuitability(VkPhysicalDevice vkPhysicalDevice, Set<FeatureSet> requiredFeatureSets, Set<FeatureSet> requiredIfExtensionsAvailable) throws BackendCreationException {
        try (VulkanPhysicalDevice physicalDevice = new VulkanPhysicalDevice(vkPhysicalDevice);){
            Object object;
            String deviceName = physicalDevice.deviceName();
            if (!VulkanBackend.deviceMeetsFeatureQueryRequirements(vkPhysicalDevice)) {
                LOGGER.warn("Device [{}] does not support Vulkan 1.1, skipping further capability checks", (Object)deviceName);
                BackendCreationException backendCreationException = new BackendCreationException("Device missing capabilities", BackendCreationException.Reason.VULKAN_DEVICE_VERSION_TOO_LOW, List.of("VULKAN_CORE_1_1"));
                return backendCreationException;
            }
            VulkanUtils.DeviceUUID deviceUUID = new VulkanUtils.DeviceUUID(physicalDevice.vkPhysicalDeviceDriverProperties().driverID(), physicalDevice.vkPhysicalDeviceProperties().vendorID(), physicalDevice.vkPhysicalDeviceProperties().deviceID());
            if (VulkanUtils.KNOWN_PROBLEMATIC_DEVICES.contains(deviceUUID)) {
                LOGGER.warn("Device [{}] is known to be problematic, skipping", (Object)deviceName);
                BackendCreationException backendCreationException = new BackendCreationException("Device known problematic", BackendCreationException.Reason.VULKAN_KNOWN_PROBLEMATIC, List.of());
                return backendCreationException;
            }
            ObjectOpenHashSet deviceRequiredFeatureSets = new ObjectOpenHashSet(requiredFeatureSets);
            Set<String> deviceExtensions = VulkanUtils.enumerateExtensions(vkPhysicalDevice);
            for (FeatureSet featureSet : requiredIfExtensionsAvailable) {
                if (!deviceExtensions.containsAll(featureSet.extensions())) continue;
                LOGGER.warn("Device [{}] supports all extensions from FeatureSet [{}], making required", (Object)deviceName, (Object)featureSet.name());
                deviceRequiredFeatureSets.add(featureSet);
            }
            ReferenceArrayList missingCapabilities = new ReferenceArrayList();
            BackendCreationException.Reason mostProminentReason = null;
            for (FeatureSet featureSet : deviceRequiredFeatureSets) {
                Set<VulkanFeature> missingFeatures = featureSet.unsupportedFeatures(vkPhysicalDevice);
                if (missingFeatures.isEmpty()) continue;
                LOGGER.warn("Device [{}] does not support required features from FeatureSet [{}], missing: {}", new Object[]{deviceName, featureSet.name(), missingFeatures});
                mostProminentReason = BackendCreationException.Reason.VULKAN_MISSING_FEATURE;
                for (VulkanFeature missingFeature : missingFeatures) {
                    missingCapabilities.add(missingFeature.name());
                }
            }
            for (FeatureSet featureSet : deviceRequiredFeatureSets) {
                Set<String> missingExtensions = featureSet.unsupportedExtensions(deviceExtensions);
                if (missingExtensions.isEmpty()) continue;
                LOGGER.warn("Device [{}] does not support required extensions from FeatureSet [{}], missing: {}", new Object[]{deviceName, featureSet.name(), missingExtensions});
                mostProminentReason = BackendCreationException.Reason.VULKAN_MISSING_EXTENSION;
                missingCapabilities.addAll(missingExtensions);
            }
            if (physicalDevice.graphicsQueueFamilyAndIndex() == null) {
                LOGGER.warn("Device [{}] does not have a graphics queue", (Object)deviceName);
                mostProminentReason = BackendCreationException.Reason.VULKAN_NO_GRAPHICS_QUEUE;
                missingCapabilities.add("COMBINED_GRAPHICS_COMPUTE_PRESENT_QUEUE");
            }
            if (physicalDevice.vkPhysicalDeviceProperties().apiVersion() < VK12.VK_API_VERSION_1_2) {
                LOGGER.warn("Device [{}] does not support Vulkan 1.2", (Object)deviceName);
                mostProminentReason = BackendCreationException.Reason.VULKAN_DEVICE_VERSION_TOO_LOW;
                missingCapabilities.add("VULKAN_CORE_1_2");
            }
            if (mostProminentReason != null) {
                LOGGER.debug("Device [{}] is not suitable", (Object)deviceName);
                object = new BackendCreationException("Device missing capabilities", mostProminentReason, (List<String>)missingCapabilities);
                return object;
            }
            assert (missingCapabilities.isEmpty());
            LOGGER.debug("Device [{}] is suitable", (Object)deviceName);
            object = null;
            return object;
        }
    }

    private static VkDevice createDevice(FeatureSet featureSet, VulkanPhysicalDevice physicalDevice) throws BackendCreationException {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPhysicalDeviceFeatures2 deviceFeatures = VkPhysicalDeviceFeatures2.calloc((MemoryStack)stack).sType$Default();
            for (VulkanFeature requiredDeviceFeature : featureSet.features()) {
                requiredDeviceFeature.set(deviceFeatures, true, stack);
            }
            Int2IntMap queuesToCreate = physicalDevice.queueFamilyCreateInfoMap();
            VkDeviceQueueCreateInfo.Buffer queueCreationInfo = VkDeviceQueueCreateInfo.calloc((int)queuesToCreate.size(), (MemoryStack)stack);
            for (Object familyCount : queuesToCreate.int2IntEntrySet()) {
                queueCreationInfo.sType$Default();
                queueCreationInfo.queueFamilyIndex(familyCount.getIntKey());
                queueCreationInfo.pQueuePriorities(stack.callocFloat(familyCount.getIntValue()));
                queueCreationInfo.position(queueCreationInfo.position() + 1);
            }
            queueCreationInfo.position(0);
            PointerBuffer enabledExtensionsBuffer = stack.callocPointer(featureSet.extensions().size());
            for (String name : featureSet.extensions()) {
                enabledExtensionsBuffer.put(stack.UTF8((CharSequence)name));
            }
            enabledExtensionsBuffer.flip();
            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc((MemoryStack)stack).sType$Default();
            deviceCreateInfo.pNext(deviceFeatures.pNext());
            deviceCreateInfo.pQueueCreateInfos(queueCreationInfo);
            deviceCreateInfo.ppEnabledExtensionNames(enabledExtensionsBuffer);
            deviceCreateInfo.pEnabledFeatures(deviceFeatures.features());
            PointerBuffer pointer = stack.callocPointer(1);
            VulkanUtils.throwIfFailure(VK12.vkCreateDevice((VkPhysicalDevice)physicalDevice.vkPhysicalDevice(), (VkDeviceCreateInfo)deviceCreateInfo, null, (PointerBuffer)pointer), "Failed to create device", BackendCreationException.Reason.VULKAN_NO_DEVICE);
            VkDevice vkDevice = new VkDevice(pointer.get(0), physicalDevice.vkPhysicalDevice(), deviceCreateInfo);
            return vkDevice;
        }
    }
}


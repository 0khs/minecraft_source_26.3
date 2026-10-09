/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VkPhysicalDevice
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
 */
package com.mojang.renderpearl.backend.vulkan.init;

import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;

public final class FeatureSet
extends Record {
    private final String name;
    private final Set<String> extensions;
    private final Set<VulkanFeature> features;
    private final Condition condition;

    public FeatureSet(String name, Set<String> extensions, Set<VulkanFeature> features, Condition condition) {
        this.name = name;
        this.extensions = Set.copyOf(extensions);
        this.features = Set.copyOf(features);
        this.condition = condition;
    }

    public FeatureSet(String name, Set<String> extensions, Set<VulkanFeature> features) {
        this(name, extensions, features, Condition.IDENTITY_CONDITION);
    }

    public FeatureSet(String name, Collection<FeatureSet> others) {
        Condition condition = others.stream().map(FeatureSet::condition).reduce(Condition.IDENTITY_CONDITION, Condition::reduce);
        ObjectOpenHashSet extensions = new ObjectOpenHashSet();
        ObjectOpenHashSet features = new ObjectOpenHashSet();
        for (FeatureSet other : others) {
            extensions.addAll(other.extensions);
            features.addAll(other.features);
        }
        this(name, (Set<String>)extensions, (Set<VulkanFeature>)features, condition);
    }

    public FeatureSet composite(FeatureSet other) {
        return new FeatureSet(this.name, List.of(this, other));
    }

    public boolean contains(FeatureSet other) {
        return this.extensions.containsAll(other.extensions) && this.features.containsAll(other.features);
    }

    public boolean checkCondition(VkPhysicalDevice device) throws BackendCreationException {
        return this.condition.test(device);
    }

    public boolean isSupported(VkPhysicalDevice vkPhysicalDevice) throws BackendCreationException {
        return this.isSupported(vkPhysicalDevice, VulkanUtils.enumerateExtensions(vkPhysicalDevice));
    }

    public boolean isSupported(VkPhysicalDevice vkPhysicalDevice, Set<String> deviceSupportedExtensions) throws BackendCreationException {
        if (!deviceSupportedExtensions.containsAll(this.extensions)) {
            return false;
        }
        return this.allFeaturesSupported(vkPhysicalDevice);
    }

    public boolean allFeaturesSupported(VkPhysicalDevice vkPhysicalDevice) {
        if (!this.features.isEmpty()) {
            try (MemoryStack stack = MemoryStack.stackPush();){
                VkPhysicalDeviceFeatures2 supportedFeatures = VulkanUtils.enumerateFeatures(vkPhysicalDevice, this.features, stack);
                for (VulkanFeature feature : this.features) {
                    if (feature.get(supportedFeatures)) continue;
                    boolean bl = false;
                    return bl;
                }
            }
        }
        return true;
    }

    public Set<String> unsupportedExtensions(Set<String> deviceSupportedExtensions) {
        if (this.extensions.isEmpty()) {
            return Set.of();
        }
        ObjectOpenHashSet supportedExtensions = new ObjectOpenHashSet(this.extensions);
        supportedExtensions.removeAll(deviceSupportedExtensions);
        if (supportedExtensions.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(supportedExtensions);
    }

    public Set<String> supportedExtensions(Set<String> deviceSupportedExtensions) {
        if (this.extensions.isEmpty()) {
            return Set.of();
        }
        ObjectOpenHashSet supportedExtensions = new ObjectOpenHashSet();
        for (String extension : this.extensions) {
            if (!deviceSupportedExtensions.contains(extension)) continue;
            supportedExtensions.add(extension);
        }
        if (supportedExtensions.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(supportedExtensions);
    }

    public Set<VulkanFeature> unsupportedFeatures(VkPhysicalDevice vkPhysicalDevice) {
        ObjectOpenHashSet unsupportedFeatures = new ObjectOpenHashSet();
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPhysicalDeviceFeatures2 featuresChain = VulkanUtils.enumerateFeatures(vkPhysicalDevice, this.features, stack);
            for (VulkanFeature feature : this.features) {
                if (feature.get(featuresChain)) continue;
                unsupportedFeatures.add(feature);
            }
        }
        if (unsupportedFeatures.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(unsupportedFeatures);
    }

    public Set<VulkanFeature> supportedFeatures(VkPhysicalDevice vkPhysicalDevice) {
        ObjectOpenHashSet supportedFeatures = new ObjectOpenHashSet();
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPhysicalDeviceFeatures2 featuresChain = VulkanUtils.enumerateFeatures(vkPhysicalDevice, this.features, stack);
            for (VulkanFeature feature : this.features) {
                if (!feature.get(featuresChain)) continue;
                supportedFeatures.add(feature);
            }
        }
        if (supportedFeatures.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(supportedFeatures);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{FeatureSet.class, "name;extensions;features;condition", "name", "extensions", "features", "condition"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FeatureSet.class, "name;extensions;features;condition", "name", "extensions", "features", "condition"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FeatureSet.class, "name;extensions;features;condition", "name", "extensions", "features", "condition"}, this, o);
    }

    public String name() {
        return this.name;
    }

    public Set<String> extensions() {
        return this.extensions;
    }

    public Set<VulkanFeature> features() {
        return this.features;
    }

    public Condition condition() {
        return this.condition;
    }

    public static interface Condition {
        public static final Condition IDENTITY_CONDITION = vkPhysicalDevice -> true;

        public static Condition reduce(Condition a, Condition b) {
            if (a == b) {
                return a;
            }
            if (a == IDENTITY_CONDITION) {
                return b;
            }
            if (b == IDENTITY_CONDITION) {
                return a;
            }
            return device -> a.test(device) && b.test(device);
        }

        public boolean test(VkPhysicalDevice var1) throws BackendCreationException;
    }
}


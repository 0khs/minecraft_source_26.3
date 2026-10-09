/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
 */
package com.mojang.renderpearl.backend.vulkan.init;

import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;

public final class VulkanFeature
extends Record {
    private final VulkanPNextStruct struct;
    private final String name;
    private final long offset;

    public VulkanFeature(VulkanPNextStruct struct, String name) {
        String structClassName = struct.pNextStructClass().getSimpleName();
        if (!structClassName.contains("Features")) {
            throw new IllegalArgumentException("Struct name \"" + structClassName + "\" does not contain \"Features\". All Vulkan features structs are expected to have \"Features\" in the name.");
        }
        this(struct, name, struct.fieldOffset(name));
    }

    public VulkanFeature(VulkanPNextStruct struct, String name, long offset) {
        this.struct = struct;
        this.name = name;
        this.offset = offset;
    }

    public boolean get(VkPhysicalDeviceFeatures2 features2) {
        return this.get(features2.address());
    }

    public boolean get(long pNextChain) {
        long structAddr = this.struct.findStructInPNextChain(pNextChain);
        if (structAddr == 0L) {
            return false;
        }
        return this.getVkBool32(structAddr);
    }

    private boolean getVkBool32(long pointer) {
        return MemoryUtil.memGetInt((long)(pointer + this.offset)) != 0;
    }

    private void putVkBool32(boolean value, long structAddr) {
        MemoryUtil.memPutInt((long)(structAddr + this.offset), (int)(value ? 1 : 0));
    }

    public boolean set(VkPhysicalDeviceFeatures2 features2, boolean value) {
        return this.set(features2.address(), value);
    }

    private boolean set(long pNextChain, boolean value) {
        long structAddr = this.struct.findStructInPNextChain(pNextChain);
        if (structAddr == 0L) {
            return false;
        }
        this.putVkBool32(value, structAddr);
        return true;
    }

    public void set(VkPhysicalDeviceFeatures2 features2, boolean value, MemoryStack stack) {
        this.set(features2.address(), value, stack);
    }

    public void set(long pNextChain, boolean value, MemoryStack stack) {
        long structAddr = this.struct.findOrCreateStructInPNextChain(pNextChain, stack);
        this.putVkBool32(value, structAddr);
    }

    @Override
    public String toString() {
        return String.valueOf(this.struct) + "." + this.name;
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{VulkanFeature.class, "struct;name;offset", "struct", "name", "offset"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{VulkanFeature.class, "struct;name;offset", "struct", "name", "offset"}, this, o);
    }

    public VulkanPNextStruct struct() {
        return this.struct;
    }

    public String name() {
        return this.name;
    }

    public long offset() {
        return this.offset;
    }
}


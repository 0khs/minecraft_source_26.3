/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.Pointer
 *  org.lwjgl.system.Struct
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
 *  org.lwjgl.vulkan.VkPhysicalDeviceProperties2
 */
package com.mojang.renderpearl.backend.vulkan.init;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;

public final class VulkanPNextStruct
extends Record {
    private final Class<?> pNextStructClass;
    private final int sType;
    private final int structSize;
    private static final int OFFSET_PNEXT = VkPhysicalDeviceProperties2.PNEXT;
    private static final int OFFSET_STYPE = VkPhysicalDeviceProperties2.STYPE;

    public <T extends Struct<?>> VulkanPNextStruct(Class<T> pNextStructClass) {
        int structSize;
        int sType;
        Field sTypeOffsetField;
        Field pNextOffsetField;
        Method sType$DefaultFunction;
        Method createFunction;
        String structClassName = pNextStructClass.getSimpleName();
        try {
            createFunction = pNextStructClass.getMethod("calloc", MemoryStack.class);
            sType$DefaultFunction = pNextStructClass.getMethod("sType$Default", new Class[0]);
            pNextOffsetField = pNextStructClass.getField("PNEXT");
            sTypeOffsetField = pNextStructClass.getField("STYPE");
        }
        catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Struct class " + structClassName + " does not have required member " + e.getMessage());
        }
        try {
            int pNextOffset = (Integer)pNextOffsetField.get(null);
            if (pNextOffset != OFFSET_PNEXT) {
                throw new IllegalArgumentException("Invalid pNext offset on class " + structClassName);
            }
            int sTypeOffset = (Integer)sTypeOffsetField.get(null);
            if (sTypeOffset != OFFSET_STYPE) {
                throw new IllegalArgumentException("Invalid sType offset on class " + structClassName);
            }
            try (MemoryStack stack = MemoryStack.stackPush();){
                Struct structInstance = (Struct)createFunction.invoke(null, stack);
                sType$DefaultFunction.invoke((Object)structInstance, new Object[0]);
                sType = VulkanPNextStruct.sType(structInstance.address());
                structSize = structInstance.sizeof();
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
        this(pNextStructClass, sType, structSize);
    }

    public VulkanPNextStruct(Class<?> pNextStructClass, int sType, int structSize) {
        this.pNextStructClass = pNextStructClass;
        this.sType = sType;
        this.structSize = structSize;
    }

    private static long pNext(long pointer) {
        return VkPhysicalDeviceProperties2.npNext((long)pointer);
    }

    private static void pNext(long pointer, long value) {
        VkPhysicalDeviceProperties2.npNext((long)pointer, (long)value);
    }

    private static int sType(long pointer) {
        return VkPhysicalDeviceProperties2.nsType((long)pointer);
    }

    private static void sType(long pointer, int value) {
        VkPhysicalDeviceProperties2.nsType((long)pointer, (int)value);
    }

    public long fieldOffset(String name) {
        int sourceClassOffset;
        Class sourceClass;
        if (this.pNextStructClass == VkPhysicalDeviceFeatures2.class) {
            sourceClass = VkPhysicalDeviceFeatures.class;
            sourceClassOffset = VkPhysicalDeviceFeatures2.FEATURES;
        } else {
            sourceClass = this.pNextStructClass;
            sourceClassOffset = 0;
        }
        try {
            Method method = sourceClass.getMethod(name, new Class[0]);
            if (method.getReturnType() != Boolean.TYPE) {
                throw new IllegalArgumentException("Only booleans are supported for struct methods");
            }
            Field offsetField = sourceClass.getField(name.toUpperCase(Locale.ROOT));
            return sourceClassOffset + (Integer)offsetField.get(null);
        }
        catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find field " + name + " in struct " + this.pNextStructClass.getSimpleName());
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public long findOrCreateStructInPNextChain(VkPhysicalDeviceProperties2 properties2, MemoryStack stack) {
        return this.findOrCreateStructInPNextChain(properties2.address(), stack);
    }

    public long findOrCreateStructInPNextChain(VkPhysicalDeviceFeatures2 features2, MemoryStack stack) {
        return this.findOrCreateStructInPNextChain(features2.address(), stack);
    }

    public long findOrCreateStructInPNextChain(long pNextChain, MemoryStack stack) {
        long foundStruct = VulkanPNextStruct.findStructInPNextChain(pNextChain, this.sType);
        if (foundStruct != 0L) {
            return foundStruct;
        }
        long newStruct = stack.ncalloc(Pointer.POINTER_SIZE, 1, this.structSize);
        VulkanPNextStruct.sType(newStruct, this.sType);
        VulkanPNextStruct.pNext(newStruct, VulkanPNextStruct.pNext(pNextChain));
        VulkanPNextStruct.pNext(pNextChain, newStruct);
        return newStruct;
    }

    public long findStructInPNextChain(long pNextChain) {
        return VulkanPNextStruct.findStructInPNextChain(pNextChain, this.sType);
    }

    private static long findStructInPNextChain(long pNextChain, int sType) {
        while (pNextChain != 0L) {
            if (VulkanPNextStruct.sType(pNextChain) == sType) {
                return pNextChain;
            }
            pNextChain = VulkanPNextStruct.pNext(pNextChain);
        }
        return 0L;
    }

    @Override
    public String toString() {
        return this.pNextStructClass.getSimpleName();
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{VulkanPNextStruct.class, "pNextStructClass;sType;structSize", "pNextStructClass", "sType", "structSize"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{VulkanPNextStruct.class, "pNextStructClass;sType;structSize", "pNextStructClass", "sType", "structSize"}, this, o);
    }

    public Class<?> pNextStructClass() {
        return this.pNextStructClass;
    }

    public int sType() {
        return this.sType;
    }

    public int structSize() {
        return this.structSize;
    }
}


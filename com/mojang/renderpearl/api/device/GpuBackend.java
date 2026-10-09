/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.api.device;

import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.device.GpuDevice;
import org.jspecify.annotations.Nullable;

public interface GpuBackend {
    public String getName();

    public void loadLibrary() throws BackendCreationException;

    public void unloadLibrary();

    public long createWindow(@Nullable String var1, int var2, int var3, long var4);

    public GpuDevice createDevice(GpuDebugOptions var1) throws BackendCreationException;
}


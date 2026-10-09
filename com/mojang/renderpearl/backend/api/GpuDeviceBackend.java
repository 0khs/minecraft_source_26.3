/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.backend.api;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public interface GpuDeviceBackend {
    public GpuSurfaceBackend createSurface(long var1, BooleanSupplier var3);

    public CommandEncoderBackend createCommandEncoder();

    public GpuSampler createSampler(AddressMode var1, AddressMode var2, FilterMode var3, FilterMode var4, int var5, OptionalDouble var6);

    public GpuTexture createTexture(@Nullable String var1, @GpuTexture.Usage int var2, GpuFormat var3, int var4, int var5, int var6, int var7);

    public GpuTextureView createTextureView(GpuTexture var1, int var2, int var3);

    public GpuBuffer createBuffer(@Nullable Supplier<String> var1, @GpuBuffer.Usage int var2, long var3);

    public GpuBuffer createBuffer(@Nullable Supplier<String> var1, @GpuBuffer.Usage int var2, ByteBuffer var3);

    public List<String> getLastDebugMessages();

    public boolean isDebuggingEnabled();

    public BackendRenderPipeline.Pending compilePipeline(BackendRenderPipeline.CreateInfo var1);

    public void close();

    public GpuQueryPool createTimestampQueryPool(int var1);

    public long getTimestampCalibrationOffset();

    public DeviceInfo getDeviceInfo();
}


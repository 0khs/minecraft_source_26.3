/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.frontend;

import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import com.mojang.renderpearl.frontend.FrontendGpuSurface;
import com.mojang.renderpearl.frontend.TracyGpuProfiler;
import com.mojang.renderpearl.frontend.shaders.PipelineBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FrontendGpuDevice
implements GpuDevice {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean STRICT_VALIDATION = SharedConstants.IS_RUNNING_IN_IDE;
    private final GpuDeviceBackend backend;
    private final @Nullable TracyGpuProfiler profiler;
    private final CommandEncoder encoder;
    private final PipelineBuilder pipelineBuilder;

    public FrontendGpuDevice(GpuDeviceBackend backend) {
        this.backend = backend;
        this.profiler = TracyClient.isAvailable() ? new TracyGpuProfiler(this) : null;
        this.encoder = new FrontendCommandEncoder(this.profiler, backend, backend.createCommandEncoder());
        this.pipelineBuilder = new PipelineBuilder(backend);
    }

    @Override
    public GpuSurface createSurface(long windowHandle, BooleanSupplier isIconified) {
        return new FrontendGpuSurface(this.backend.createSurface(windowHandle, isIconified));
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return this.encoder;
    }

    @Override
    public GpuSampler createSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        int maxSupportedAnisotropy = this.getDeviceInfo().limits().maxAnisotropy();
        if (maxAnisotropy < 1 || maxAnisotropy > maxSupportedAnisotropy) {
            throw new IllegalArgumentException("maxAnisotropy out of range; must be >= 1 and <= " + maxSupportedAnisotropy + ", but was " + maxAnisotropy);
        }
        return this.backend.createSampler(addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> label, @GpuTexture.Usage int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        return this.createTexture(this.isDebuggingEnabled() && label != null ? label.get() : null, usage, format, width, height, depthOrLayers, mipLevels);
    }

    @Override
    public GpuTexture createTexture(@Nullable String label, @GpuTexture.Usage int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        this.verifyTextureCreationArgs(usage, width, height, depthOrLayers, mipLevels);
        return this.backend.createTexture(label, usage, format, width, height, depthOrLayers, mipLevels);
    }

    private void verifyTextureCreationArgs(@GpuTexture.Usage int usage, int width, int height, int depthOrLayers, int mipLevels) {
        boolean isCubemap;
        if (mipLevels < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        }
        int maxDimension = Math.max(width, height);
        int maxMipSupported = Mth.log2(maxDimension) + 1;
        if (mipLevels > maxMipSupported) {
            throw new IllegalArgumentException("mipLevels must be at most " + maxMipSupported + " for a texture of width " + width + " and height " + height + " (asked for " + mipLevels + " mipLevels)");
        }
        if (depthOrLayers < 1) {
            throw new IllegalArgumentException("depthOrLayers must be at least 1");
        }
        boolean bl = isCubemap = (usage & 0x10) != 0;
        if (isCubemap) {
            if (width != height) {
                throw new IllegalArgumentException("Cubemap compatible textures must be square, but size is " + width + "x" + height);
            }
            if (depthOrLayers % 6 != 0) {
                throw new IllegalArgumentException("Cubemap compatible textures must have a layer count with a multiple of 6, was " + depthOrLayers);
            }
            if (depthOrLayers > 6) {
                throw new UnsupportedOperationException("Array textures are not yet supported");
            }
        } else if (depthOrLayers > 1) {
            throw new UnsupportedOperationException("Array or 3D textures are not yet supported");
        }
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture) {
        return this.createTextureView(texture, 0, texture.getMipLevels());
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
        this.verifyTextureViewCreationArgs(texture, baseMipLevel, mipLevels);
        return this.backend.createTextureView(texture, baseMipLevel, mipLevels);
    }

    private void verifyTextureViewCreationArgs(GpuTexture texture, int baseMipLevel, int mipLevels) {
        if (texture.isClosed()) {
            throw new IllegalArgumentException("Can't create texture view with closed texture");
        }
        if (baseMipLevel < 0 || baseMipLevel + mipLevels > texture.getMipLevels()) {
            throw new IllegalArgumentException(mipLevels + " mip levels starting from " + baseMipLevel + " would be out of range for texture with only " + texture.getMipLevels() + " mip levels");
        }
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, @GpuBuffer.Usage int usage, long size) {
        if (size <= 0L) {
            throw new IllegalArgumentException("Buffer size must be greater than zero");
        }
        return this.backend.createBuffer(label, usage, size);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, @GpuBuffer.Usage int usage, ByteBuffer data) {
        if (!data.hasRemaining()) {
            throw new IllegalArgumentException("Buffer source must not be empty");
        }
        return this.backend.createBuffer(label, usage, data);
    }

    @Override
    public List<String> getLastDebugMessages() {
        return this.backend.getLastDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.backend.isDebuggingEnabled();
    }

    @Override
    public CompletableFuture<CompiledRenderPipeline.Pending> compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource, Executor executor) {
        return this.pipelineBuilder.compilePipeline(pipeline, shaderSource, executor);
    }

    @Override
    public void close() {
        this.pipelineBuilder.close();
        this.backend.close();
    }

    @Override
    public GpuQueryPool createTimestampQueryPool(int size) {
        return this.backend.createTimestampQueryPool(size);
    }

    long getTimestampCalibrationOffset() {
        return this.backend.getTimestampCalibrationOffset();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.backend.getDeviceInfo();
    }
}


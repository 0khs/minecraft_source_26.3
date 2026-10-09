/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.Version
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLHints
 *  org.lwjgl.sdl.SDLInit
 *  org.lwjgl.sdl.SDLTimer
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.platform.SDLEventHandler;
import com.mojang.blaze3d.platform.SdlDebug;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.DynamicGpuData;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.Version;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class RenderSystem {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final double DEFAULT_DEPTH_CLEAR_VALUE = 0.0;
    public static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    public static final int PROJECTION_MATRIX_UBO_SIZE = new Std140SizeCalculator().putMat4f().get();
    private static @Nullable Thread renderThread;
    private static @Nullable GpuDevice DEVICE;
    private static @Nullable GpuBackend BACKEND;
    private static final AutoStorageIndexBuffer sharedSequential;
    private static final AutoStorageIndexBuffer sharedSequentialQuad;
    private static final AutoStorageIndexBuffer sharedSequentialLines;
    private static ProjectionType projectionType;
    private static ProjectionType savedProjectionType;
    private static final Matrix4fStack modelViewStack;
    private static @Nullable GpuBufferSlice shaderFog;
    private static @Nullable GpuBufferSlice shaderLightDirections;
    private static @Nullable GpuBufferSlice projectionMatrixBuffer;
    private static @Nullable GpuBufferSlice savedProjectionMatrixBuffer;
    private static final AtomicLong pollEventsWaitStart;
    private static final AtomicBoolean pollingEvents;
    private static final ArrayListDeque<GpuAsyncTask> PENDING_FENCES;
    public static boolean isRenderingLevel;
    private static @Nullable GpuBuffer globalSettingsUniform;
    private static @Nullable DynamicGpuData dynamicGpuData;
    private static final ScissorState scissorStateForRenderTypeDraws;
    private static final SamplerCache samplerCache;
    private static @Nullable PipelineCache fallbackPipelineCache;
    private static @Nullable PipelineCache currentPipelineCache;

    public static SamplerCache getSamplerCache() {
        return samplerCache;
    }

    public static void setFallbackPipelineCache(PipelineCache pipelineCache) {
        if (fallbackPipelineCache != null) {
            throw new IllegalStateException("Fallback pipeline cache already set");
        }
        fallbackPipelineCache = pipelineCache;
    }

    public static @Nullable PipelineCache setCurrentPipelineCache(PipelineCache pipelineCache) {
        PipelineCache oldCache = currentPipelineCache;
        currentPipelineCache = pipelineCache;
        return oldCache;
    }

    public static @Nullable CompiledRenderPipeline getCompiledPipelineNullable(RenderPipeline pipeline) {
        CompiledRenderPipeline cachedPipeline;
        if (currentPipelineCache != null && (cachedPipeline = currentPipelineCache.get(pipeline)) != null) {
            return cachedPipeline;
        }
        if (fallbackPipelineCache == null) {
            throw new IllegalStateException("Fallback pipeline cache not yet set");
        }
        return fallbackPipelineCache.get(pipeline);
    }

    public static CompiledRenderPipeline getCompiledPipeline(RenderPipeline pipeline) {
        CompiledRenderPipeline compiledPipeline = RenderSystem.getCompiledPipelineNullable(pipeline);
        if (compiledPipeline != null) {
            return compiledPipeline;
        }
        throw new IllegalStateException("Failed to find or load pipeline " + String.valueOf(pipeline.getLocation()));
    }

    public static void initRenderThread() {
        if (renderThread != null) {
            throw new IllegalStateException("Could not initialize render thread");
        }
        renderThread = Thread.currentThread();
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static void assertOnRenderThread() {
        if (!RenderSystem.isOnRenderThread()) {
            throw RenderSystem.constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    public static void pollEvents(SDLEventHandler eventHandler) {
        pollEventsWaitStart.set(Util.getMillis());
        pollingEvents.set(true);
        eventHandler.pollEvents();
        pollingEvents.set(false);
    }

    public static boolean isFrozenAtPollEvents() {
        return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
    }

    public static void pumpEvents(SDLEventHandler eventHandler) {
        pollEventsWaitStart.set(Util.getMillis());
        pollingEvents.set(true);
        eventHandler.pumpEvents();
        pollingEvents.set(false);
    }

    public static void setShaderFog(GpuBufferSlice fog) {
        shaderFog = fog;
    }

    public static @Nullable GpuBufferSlice getShaderFog() {
        return shaderFog;
    }

    public static void setShaderLights(GpuBufferSlice buffer) {
        shaderLightDirections = buffer;
    }

    public static @Nullable GpuBufferSlice getShaderLights() {
        return shaderLightDirections;
    }

    public static void enableScissorForRenderTypeDraws(int x, int y, int width, int height) {
        scissorStateForRenderTypeDraws.enable(x, y, width, height);
    }

    public static void disableScissorForRenderTypeDraws() {
        scissorStateForRenderTypeDraws.disable();
    }

    public static ScissorState getScissorStateForRenderTypeDraws() {
        return scissorStateForRenderTypeDraws;
    }

    public static String getBackendDescription() {
        return String.format(Locale.ROOT, "LWJGL version %s", Version.getVersion());
    }

    public static TimeSource.NanoTimeSource initBackendSystem() {
        SdlDebug.init();
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.name", (CharSequence)"Minecraft");
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.version", (CharSequence)SharedConstants.getCurrentVersion().name());
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.identifier", (CharSequence)"com.mojang.minecraft");
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.creator", (CharSequence)"Mojang Studios");
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.copyright", (CharSequence)"Copyright Mojang AB.");
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.url", (CharSequence)"https://www.minecraft.net");
        SDLInit.SDL_SetAppMetadataProperty((CharSequence)"SDL.app.metadata.type", (CharSequence)"game");
        SDLHints.SDL_SetHint((CharSequence)"SDL_NO_SIGNAL_HANDLERS", (CharSequence)"1");
        SDLHints.SDL_SetHint((CharSequence)"SDL_VIDEO_MINIMIZE_ON_FOCUS_LOSS", (CharSequence)"0");
        SDLHints.SDL_SetHint((CharSequence)"SDL_QUIT_ON_LAST_WINDOW_CLOSE", (CharSequence)"0");
        SDLHints.SDL_SetHint((CharSequence)"SDL_MOUSE_FOCUS_CLICKTHROUGH", (CharSequence)"1");
        SDLHints.SDL_SetHint((CharSequence)"SDL_ENABLE_SCREEN_KEYBOARD", (CharSequence)"0");
        SDLHints.SDL_SetHint((CharSequence)"SDL_IME_IMPLEMENTED_UI", (CharSequence)"composition");
        if (!SDLInit.SDL_Init((int)32)) {
            throw new IllegalStateException("Unable to initialize SDL: " + SDLError.SDL_GetError());
        }
        return SDLTimer::SDL_GetTicksNS;
    }

    public static void initRenderer(GpuDevice device) {
        if (DEVICE != null) {
            throw new IllegalStateException("RenderSystem.DEVICE already initialized");
        }
        DEVICE = device;
        dynamicGpuData = new DynamicGpuData();
        samplerCache.initialize();
    }

    public static void shutdownRenderer() {
        if (currentPipelineCache != null) {
            currentPipelineCache.close();
        }
        if (fallbackPipelineCache != null) {
            fallbackPipelineCache.close();
        }
        sharedSequential.close();
        sharedSequentialQuad.close();
        sharedSequentialLines.close();
        samplerCache.close();
        if (dynamicGpuData != null) {
            dynamicGpuData.close();
        }
        if (DEVICE != null) {
            DEVICE.close();
        }
    }

    public static void trackBackendLibraryForShutdown(GpuBackend backend) {
        BACKEND = backend;
    }

    public static void unloadTrackedBackendLibrary() {
        if (BACKEND == null) {
            return;
        }
        BACKEND.unloadLibrary();
        BACKEND = null;
    }

    public static void setupDefaultState() {
        modelViewStack.clear();
    }

    public static void setProjectionMatrix(GpuBufferSlice projectionMatrixBuffer, ProjectionType type) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.projectionMatrixBuffer = projectionMatrixBuffer;
        projectionType = type;
    }

    public static void backupProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        savedProjectionMatrixBuffer = projectionMatrixBuffer;
        savedProjectionType = projectionType;
    }

    public static void restoreProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        projectionMatrixBuffer = savedProjectionMatrixBuffer;
        projectionType = savedProjectionType;
    }

    public static @Nullable GpuBufferSlice getProjectionMatrixBuffer() {
        RenderSystem.assertOnRenderThread();
        return projectionMatrixBuffer;
    }

    public static Matrix4f getModelViewMatrixCopy() {
        RenderSystem.assertOnRenderThread();
        return new Matrix4f((Matrix4fc)modelViewStack);
    }

    public static Matrix4fStack getModelViewStack() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static AutoStorageIndexBuffer getSequentialBuffer(PrimitiveTopology primitiveTopology) {
        RenderSystem.assertOnRenderThread();
        return switch (primitiveTopology) {
            case PrimitiveTopology.QUADS -> sharedSequentialQuad;
            case PrimitiveTopology.LINES -> sharedSequentialLines;
            default -> sharedSequential;
        };
    }

    public static void setGlobalSettingsUniform(GpuBuffer buffer) {
        globalSettingsUniform = buffer;
    }

    public static @Nullable GpuBuffer getGlobalSettingsUniform() {
        return globalSettingsUniform;
    }

    public static ProjectionType getProjectionType() {
        RenderSystem.assertOnRenderThread();
        return projectionType;
    }

    public static void queueFencedTask(Runnable task) {
        PENDING_FENCES.addLast(new GpuAsyncTask(task, RenderSystem.getDevice().createCommandEncoder().createFence()));
    }

    public static void executePendingTasks() {
        GpuAsyncTask task = PENDING_FENCES.peekFirst();
        while (task != null) {
            if (task.fence.awaitCompletion(0L)) {
                try {
                    task.callback.run();
                }
                finally {
                    task.fence.close();
                }
                PENDING_FENCES.removeFirst();
                task = PENDING_FENCES.peekFirst();
                continue;
            }
            return;
        }
    }

    public static GpuDevice getDevice() {
        if (DEVICE == null) {
            throw new IllegalStateException("Can't getDevice() before it was initialized");
        }
        return DEVICE;
    }

    public static @Nullable GpuDevice tryGetDevice() {
        return DEVICE;
    }

    public static boolean isWireframeAvailable() {
        return RenderSystem.getDevice().getDeviceInfo().features().wireframeFillMode();
    }

    public static DynamicGpuData getDynamicUniforms() {
        if (dynamicGpuData == null) {
            throw new IllegalStateException("Can't getDynamicUniforms() before device was initialized");
        }
        return dynamicGpuData;
    }

    public static void bindDefaultUniforms(RenderPass renderPass) {
        GpuBufferSlice shaderLights;
        GpuBuffer globalUniform;
        GpuBufferSlice fog;
        GpuBufferSlice projectionMatrix = RenderSystem.getProjectionMatrixBuffer();
        if (projectionMatrix != null) {
            renderPass.setUniform("Projection", projectionMatrix);
        }
        if ((fog = RenderSystem.getShaderFog()) != null) {
            renderPass.setUniform("Fog", fog);
        }
        if ((globalUniform = RenderSystem.getGlobalSettingsUniform()) != null) {
            renderPass.setUniform("Globals", globalUniform);
        }
        if ((shaderLights = RenderSystem.getShaderLights()) != null) {
            renderPass.setUniform("Lighting", shaderLights);
        }
    }

    public static void resizeAllAutoStorageIndexBuffers() {
        sharedSequential.resizeToRequestedIndexCount();
        sharedSequentialQuad.resizeToRequestedIndexCount();
        sharedSequentialLines.resizeToRequestedIndexCount();
    }

    static {
        sharedSequential = new AutoStorageIndexBuffer(1, 1, java.util.function.IntConsumer::accept);
        sharedSequentialQuad = new AutoStorageIndexBuffer(4, 6, (c, i) -> {
            c.accept(i);
            c.accept(i + 1);
            c.accept(i + 2);
            c.accept(i + 2);
            c.accept(i + 3);
            c.accept(i);
        });
        sharedSequentialLines = new AutoStorageIndexBuffer(4, 6, (c, i) -> {
            c.accept(i);
            c.accept(i + 1);
            c.accept(i + 2);
            c.accept(i + 3);
            c.accept(i + 2);
            c.accept(i + 1);
        });
        projectionType = ProjectionType.PERSPECTIVE;
        savedProjectionType = ProjectionType.PERSPECTIVE;
        modelViewStack = new Matrix4fStack(16);
        shaderFog = null;
        pollEventsWaitStart = new AtomicLong();
        pollingEvents = new AtomicBoolean(false);
        PENDING_FENCES = new ArrayListDeque();
        isRenderingLevel = false;
        scissorStateForRenderTypeDraws = new ScissorState();
        samplerCache = new SamplerCache();
    }

    public static final class AutoStorageIndexBuffer
    implements AutoCloseable {
        private final int vertexStride;
        private final int indexStride;
        private final IndexGenerator generator;
        private @Nullable GpuBuffer buffer;
        private IndexType type = IndexType.SHORT;
        private int indexCount;
        private int maxRequestedIndexCount;

        private AutoStorageIndexBuffer(int vertexStride, int indexStride, IndexGenerator generator) {
            this.vertexStride = vertexStride;
            this.indexStride = indexStride;
            this.generator = generator;
        }

        @Override
        public void close() {
            if (this.buffer != null) {
                this.buffer.close();
            }
        }

        public boolean hasStorage(int indexCount) {
            return indexCount <= this.indexCount;
        }

        public void requestIndexCount(int indexCount) {
            this.maxRequestedIndexCount = Math.max(this.maxRequestedIndexCount, indexCount);
        }

        public void resizeToRequestedIndexCount() {
            this.ensureStorage(this.maxRequestedIndexCount);
        }

        public GpuBuffer getBuffer(int indexCount) {
            this.requestIndexCount(indexCount);
            this.ensureStorage(indexCount);
            return this.buffer;
        }

        public GpuBuffer getBuffer() {
            return this.buffer;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void ensureStorage(int indexCount) {
            if (this.hasStorage(indexCount)) {
                return;
            }
            indexCount = Mth.roundToward(indexCount * 2, this.indexStride);
            LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", (Object)this.indexCount, (Object)indexCount);
            int primitiveCount = indexCount / this.indexStride;
            int vertexCount = primitiveCount * this.vertexStride;
            IndexType type = IndexType.least(vertexCount);
            int bufferSize = Mth.roundToward(indexCount * type.bytes, 4);
            ByteBuffer data = MemoryUtil.memAlloc((int)bufferSize);
            try {
                this.type = type;
                IntConsumer intConsumer = this.intConsumer(data);
                for (int ii = 0; ii < indexCount; ii += this.indexStride) {
                    this.generator.accept(intConsumer, ii * this.vertexStride / this.indexStride);
                }
                data.flip();
                if (this.buffer != null) {
                    this.buffer.close();
                }
                this.buffer = RenderSystem.getDevice().createBuffer(() -> "Auto Storage index buffer", 64, data);
            }
            finally {
                MemoryUtil.memFree((ByteBuffer)data);
            }
            this.indexCount = indexCount;
        }

        private IntConsumer intConsumer(ByteBuffer buffer) {
            switch (this.type) {
                case SHORT: {
                    return value -> buffer.putShort((short)value);
                }
            }
            return buffer::putInt;
        }

        public IndexType type() {
            return this.type;
        }

        private static interface IndexGenerator {
            public void accept(IntConsumer var1, int var2);
        }
    }

    private record GpuAsyncTask(Runnable callback, GpuFence fence) {
    }
}


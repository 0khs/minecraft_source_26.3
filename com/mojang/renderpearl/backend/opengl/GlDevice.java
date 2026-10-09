/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBClipControl
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL33C
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLVideo
 *  org.lwjgl.system.MemoryStack
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.device.GpuOutOfMemoryException;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import com.mojang.renderpearl.backend.opengl.BufferStorage;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.FrameBufferCache;
import com.mojang.renderpearl.backend.opengl.GlBackend;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlCommandEncoder;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlDebug;
import com.mojang.renderpearl.backend.opengl.GlDebugLabel;
import com.mojang.renderpearl.backend.opengl.GlHeuristics;
import com.mojang.renderpearl.backend.opengl.GlPipelineRecompiler;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlQueryPool;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlSampler;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlSurface;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.backend.opengl.GlTextureView;
import com.mojang.renderpearl.backend.opengl.VertexArray;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBClipControl;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

class GlDevice
implements GpuDeviceBackend {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static boolean USE_GL_ARB_vertex_attrib_binding = true;
    protected static boolean USE_GL_KHR_debug = true;
    protected static boolean USE_GL_EXT_debug_label = true;
    protected static boolean USE_GL_ARB_debug_output = true;
    protected static boolean USE_GL_ARB_direct_state_access = true;
    protected static boolean USE_GL_ARB_buffer_storage = true;
    protected static boolean USE_GL_ARB_base_instance = true;
    protected static boolean USE_GL_ARB_draw_indirect = true;
    protected static boolean USE_GL_ARB_multi_draw_indirect = true;
    protected static boolean USE_GL_ARB_shader_draw_parameters = true;
    private final long initialWindowHandle;
    private final long glContext;
    private final GlHeuristics heuristics;
    private final GlCommandEncoder encoder;
    private final @Nullable GlDebug debugLog;
    private final GlDebugLabel debugLabels;
    private final DirectStateAccess directStateAccess;
    private final FrameBufferCache frameBufferCache = new FrameBufferCache();
    private final BiFunction<GlProgram, BackendRenderPipeline.CreateInfo, VertexArray> vertexArraySource;
    private final BufferStorage bufferStorage;
    private final DeviceInfo deviceInfo;
    private final GlPipelineRecompiler recompiler;
    private boolean shaderCompilerRequiresSacrifice = true;
    private long currentWindow = 0L;

    public GlDevice(GlBackend backend, GpuDebugOptions debugOptions) throws BackendCreationException {
        this.initialWindowHandle = backend.createWindow("Minecraft - RenderPearl OpenGL Hidden Utility Window", 320, 480, 0x80020008L);
        if (this.initialWindowHandle == 0L) {
            throw new BackendCreationException("Failed to create window for OpenGL context: " + Objects.requireNonNullElse(SDLError.SDL_GetError(), "<no error>"), BackendCreationException.Reason.OPENGL_MISSING);
        }
        long glContext = SDLVideo.SDL_GL_CreateContext((long)this.initialWindowHandle);
        if (glContext == 0L) {
            SDLVideo.SDL_DestroyWindow((long)this.initialWindowHandle);
            throw new BackendCreationException("Failed to create OpenGL context: " + Objects.requireNonNullElse(SDLError.SDL_GetError(), "<no error>"), BackendCreationException.Reason.OPENGL_MISSING);
        }
        this.glContext = glContext;
        this.makeCurrent(this.initialWindowHandle);
        try {
            int maxSupportedAnisotropy;
            try (MemoryStack stack = MemoryStack.stackPush();){
                IntBuffer majorVersion = stack.callocInt(1);
                IntBuffer minorVersion = stack.callocInt(1);
                SDLVideo.SDL_GL_GetAttribute((int)17, (IntBuffer)majorVersion);
                SDLVideo.SDL_GL_GetAttribute((int)18, (IntBuffer)minorVersion);
                if (majorVersion.get(0) < 3 || majorVersion.get(0) == 3 && minorVersion.get(0) < 3) {
                    throw new BackendCreationException("Failed to create OpenGL 3.3 context, got OpenGL " + majorVersion.get(0) + "." + minorVersion.get(0), BackendCreationException.Reason.OPENGL_MISSING);
                }
                long secondWindow = backend.createWindow("Minecraft - RenderPearl OpenGL Hidden Test Window", 320, 480, 0x80020008L);
                if (secondWindow == 0L) {
                    throw new BackendCreationException("Failed to create window for OpenGL after creating context: " + Objects.requireNonNullElse(SDLError.SDL_GetError(), "<no error>"), BackendCreationException.Reason.OPENGL_MISSING);
                }
                SDLVideo.SDL_DestroyWindow((long)secondWindow);
            }
            GLCapabilities capabilities = GL.createCapabilities();
            HashSet<String> enabledExtensions = new HashSet<String>();
            if (capabilities.GL_EXT_texture_filter_anisotropic) {
                maxSupportedAnisotropy = Mth.floor(GL33C.glGetFloat((int)34047));
                enabledExtensions.add("GL_EXT_texture_filter_anisotropic");
            } else {
                maxSupportedAnisotropy = 1;
            }
            this.heuristics = new GlHeuristics(GlStateManager._getString(7937), GlStateManager._getString(7936));
            this.debugLog = GlDebug.enableDebugCallback(debugOptions.logLevel(), debugOptions.synchronousLogs(), enabledExtensions);
            this.debugLabels = GlDebugLabel.create(capabilities, debugOptions.useLabels(), enabledExtensions);
            this.bufferStorage = BufferStorage.create(capabilities, enabledExtensions, this.heuristics.couldBeIntelGen7() || this.heuristics.isNvidia());
            this.directStateAccess = DirectStateAccess.create(capabilities, enabledExtensions, this.heuristics);
            this.vertexArraySource = VertexArray.createSource(capabilities, enabledExtensions);
            GL33C.glEnable((int)34895);
            GL33C.glEnable((int)34370);
            if (capabilities.GL_ARB_clip_control) {
                ARBClipControl.glClipControl((int)36001, (int)37727);
                enabledExtensions.add("GL_ARB_clip_control");
            }
            if (capabilities.GL_ARB_shader_draw_parameters && USE_GL_ARB_shader_draw_parameters) {
                enabledExtensions.add("GL_ARB_shader_draw_parameters");
            }
            if (capabilities.GL_ARB_draw_indirect && USE_GL_ARB_draw_indirect) {
                enabledExtensions.add("GL_ARB_draw_indirect");
                if (capabilities.GL_ARB_multi_draw_indirect && USE_GL_ARB_multi_draw_indirect) {
                    enabledExtensions.add("GL_ARB_multi_draw_indirect");
                }
            }
            if (capabilities.GL_ARB_base_instance && USE_GL_ARB_base_instance) {
                enabledExtensions.add("GL_ARB_base_instance");
            }
            this.deviceInfo = this.heuristics.createDeviceInfo(capabilities, maxSupportedAnisotropy, enabledExtensions);
            this.encoder = new GlCommandEncoder(this);
            this.recompiler = new GlPipelineRecompiler(this.debugLabels, this.deviceInfo.features().shaderDrawParameters());
        }
        catch (Throwable throwable) {
            SDLVideo.SDL_GL_DestroyContext((long)glContext);
            SDLVideo.SDL_DestroyWindow((long)this.initialWindowHandle);
            throw throwable;
        }
    }

    public GlHeuristics heuristics() {
        return this.heuristics;
    }

    public GlDebugLabel debugLabels() {
        return this.debugLabels;
    }

    @Override
    public GpuSurfaceBackend createSurface(long windowHandle, BooleanSupplier isIconified) {
        return new GlSurface(this, windowHandle, isIconified);
    }

    @Override
    public CommandEncoderBackend createCommandEncoder() {
        return this.encoder;
    }

    @Override
    public GpuSampler createSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        return new GlSampler(addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
    }

    @Override
    public GpuTexture createTexture(@Nullable String label, @GpuTexture.Usage int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        int error;
        int target;
        boolean isCubemap;
        GlStateManager.clearGlErrors();
        int id = GlStateManager._genTexture();
        if (label == null) {
            label = String.valueOf(id);
        }
        boolean bl = isCubemap = (usage & 0x10) != 0;
        if (isCubemap) {
            GL33C.glBindTexture((int)34067, (int)id);
            target = 34067;
        } else {
            GlStateManager._bindTexture(id);
            target = 3553;
        }
        GlStateManager._texParameter(target, 33085, mipLevels - 1);
        GlStateManager._texParameter(target, 33082, 0);
        GlStateManager._texParameter(target, 33083, mipLevels - 1);
        if (format.hasDepthAspect()) {
            GlStateManager._texParameter(target, 34892, 0);
        }
        int glInternalID = GlConst.toGlInternalId(format);
        int glExternalID = GlConst.toGlExternalId(format);
        int glType = GlConst.toGlType(format);
        if (glInternalID == 0 || glExternalID == 0 || glType == 0) {
            throw new IllegalArgumentException(String.valueOf((Object)format) + " format cannot be used to create textures");
        }
        if (isCubemap) {
            for (int cubeTarget : GlConst.CUBEMAP_TARGETS) {
                for (int i = 0; i < mipLevels; ++i) {
                    GlStateManager._texImage2D(cubeTarget, i, glInternalID, width >> i, height >> i, 0, glExternalID, glType, null);
                }
            }
        } else {
            for (int i = 0; i < mipLevels; ++i) {
                GlStateManager._texImage2D(target, i, glInternalID, width >> i, height >> i, 0, glExternalID, glType, null);
            }
        }
        if ((error = GlStateManager._getError()) == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate texture of " + width + "x" + height + " for " + label);
        }
        if (error != 0) {
            throw new IllegalStateException("OpenGL error " + error);
        }
        GlTexture texture = new GlTexture(usage, label, format, width, height, depthOrLayers, mipLevels, id, this.frameBufferCache);
        this.debugLabels.applyLabel(texture);
        return texture;
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
        return new GlTextureView((GlTexture)texture, baseMipLevel, mipLevels, this.frameBufferCache);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, @GpuBuffer.Usage int usage, long size) {
        GlStateManager.clearGlErrors();
        GlBuffer buffer = this.bufferStorage.createBuffer(this.heuristics, this.directStateAccess, usage, size);
        int error = GlStateManager._getError();
        if (error == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate buffer of " + size + " for " + String.valueOf(label));
        }
        if (error != 0) {
            throw new IllegalStateException("OpenGL error " + error);
        }
        this.debugLabels.applyLabel(buffer, label);
        return buffer;
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, @GpuBuffer.Usage int usage, ByteBuffer data) {
        GlStateManager.clearGlErrors();
        long size = data.remaining();
        GlBuffer buffer = this.bufferStorage.createBuffer(this.heuristics, this.directStateAccess, usage, data);
        int error = GlStateManager._getError();
        if (error == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate buffer of " + size + " for " + String.valueOf(label));
        }
        if (error != 0) {
            throw new IllegalStateException("OpenGL error " + error);
        }
        this.debugLabels.applyLabel(buffer, label);
        return buffer;
    }

    @Override
    public List<String> getLastDebugMessages() {
        return this.debugLog == null ? Collections.emptyList() : this.debugLog.getLastOpenGlDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.debugLog != null;
    }

    private void sacrificeShaderToOpenGlAndAmd() {
        if (!this.shaderCompilerRequiresSacrifice) {
            return;
        }
        this.shaderCompilerRequiresSacrifice = false;
        String glRenderer = GlStateManager._getString(7937);
        if (!glRenderer.contains("AMD")) {
            return;
        }
        int shader = GlStateManager.glCreateShader(35633);
        int program = GlStateManager.glCreateProgram();
        GlStateManager.glAttachShader(program, shader);
        GlStateManager.glDeleteShader(shader);
        GlStateManager.glDeleteProgram(program);
    }

    void markAmdShaderCompilerAngry() {
        this.shaderCompilerRequiresSacrifice = true;
    }

    @Override
    public void close() {
        this.encoder.close();
        SDLVideo.SDL_GL_DestroyContext((long)this.glContext);
        SDLVideo.SDL_DestroyWindow((long)this.initialWindowHandle);
    }

    public DirectStateAccess directStateAccess() {
        return this.directStateAccess;
    }

    @Override
    public BackendRenderPipeline.Pending compilePipeline(BackendRenderPipeline.CreateInfo createInfo) {
        Map<BackendRenderPipeline.CreateInfo.Shader, String> decompiledShaders = this.recompiler.decompileShaders(createInfo);
        if (decompiledShaders == null) {
            return BackendRenderPipeline.Pending.NULL;
        }
        return () -> {
            this.sacrificeShaderToOpenGlAndAmd();
            GlProgram glProgram = this.recompiler.compileProgram(createInfo, decompiledShaders);
            if (glProgram == null) {
                return null;
            }
            VertexArray vertexArray = this.vertexArraySource.apply(glProgram, createInfo);
            return new GlRenderPipeline(this, createInfo, glProgram, vertexArray);
        };
    }

    public BufferStorage getBufferStorage() {
        return this.bufferStorage;
    }

    public FrameBufferCache frameBufferCache() {
        return this.frameBufferCache;
    }

    @Override
    public GpuQueryPool createTimestampQueryPool(int size) {
        return new GlQueryPool(size);
    }

    @Override
    public long getTimestampCalibrationOffset() {
        long deviceTime = GL33C.glGetInteger64((int)36392);
        long hostTime = System.nanoTime();
        return hostTime - deviceTime;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    void makeCurrent(long windowHandle) {
        if (windowHandle == this.currentWindow) {
            return;
        }
        this.currentWindow = windowHandle;
        SDLVideo.SDL_GL_MakeCurrent((long)windowHandle, (long)this.glContext);
    }
}


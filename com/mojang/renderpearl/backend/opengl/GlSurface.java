/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.sdl.SDLVideo
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import com.mojang.renderpearl.backend.opengl.GlCommandEncoder;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.lwjgl.sdl.SDLVideo;

public class GlSurface
implements GpuSurfaceBackend {
    private static final Set<GpuSurface.PresentMode> SUPPORTED_PRESENT_MODES = EnumSet.of(GpuSurface.PresentMode.FIFO, GpuSurface.PresentMode.IMMEDIATE);
    private final GlDevice device;
    private final long windowHandle;
    private final BooleanSupplier isIconified;
    private int swapchainWidth;
    private int swapchainHeight;

    GlSurface(GlDevice device, long windowHandle, BooleanSupplier isIconified) {
        this.device = device;
        this.windowHandle = windowHandle;
        this.isIconified = isIconified;
    }

    @Override
    public void configure(GpuSurface.Configuration config) throws SurfaceException {
        this.device.makeCurrent(this.windowHandle);
        SDLVideo.SDL_GL_SetSwapInterval((int)(config.presentMode() == GpuSurface.PresentMode.FIFO ? 1 : 0));
        this.swapchainWidth = config.width();
        this.swapchainHeight = config.height();
    }

    @Override
    public boolean isSuboptimal() {
        return false;
    }

    @Override
    public void acquireNextTexture() throws SurfaceException {
        if (this.isIconified.getAsBoolean()) {
            throw new SurfaceException("Cannot acquire minimized window");
        }
    }

    @Override
    public void blitFromTexture(CommandEncoderBackend commandEncoder, GpuTextureView textureView) {
        ((GlCommandEncoder)commandEncoder).presentTexture(this.windowHandle, textureView, this.swapchainWidth, this.swapchainHeight);
    }

    @Override
    public void present() {
        SDLVideo.SDL_GL_SwapWindow((long)this.windowHandle);
    }

    @Override
    public void close() {
    }

    @Override
    public Collection<GpuSurface.PresentMode> supportedPresentModes() {
        return SUPPORTED_PRESENT_MODES;
    }
}


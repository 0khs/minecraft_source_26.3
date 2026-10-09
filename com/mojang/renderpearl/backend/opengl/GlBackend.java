/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLVideo
 *  org.lwjgl.system.SharedLibrary
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.SharedLibrary;

public class GlBackend
implements GpuBackend {
    private static final int VERSION_MAJOR = 3;
    private static final int VERSION_MINOR = 3;
    private boolean libraryLoaded;
    private @Nullable BackendCreationException libraryLoadFailure;

    @Override
    public String getName() {
        return "OpenGL";
    }

    @Override
    public void loadLibrary() throws BackendCreationException {
        if (this.libraryLoaded) {
            return;
        }
        if (this.libraryLoadFailure != null) {
            throw this.libraryLoadFailure;
        }
        if (!SDLVideo.SDL_GL_LoadLibrary((CharSequence)((SharedLibrary)GL.getFunctionProvider()).getPath())) {
            this.libraryLoadFailure = new BackendCreationException("OpenGL is not supported: " + Objects.requireNonNullElse(SDLError.SDL_GetError(), "<no error>"), BackendCreationException.Reason.OPENGL_MISSING);
            throw this.libraryLoadFailure;
        }
        if (GL.getFunctionProvider().getFunctionAddress((CharSequence)"glGetError") != SDLVideo.SDL_GL_GetProcAddress((CharSequence)"glGetError")) {
            this.libraryLoadFailure = new BackendCreationException("glGetError mismatch", BackendCreationException.Reason.OPENGL_MISSING);
            SDLVideo.SDL_GL_UnloadLibrary();
            throw this.libraryLoadFailure;
        }
        this.libraryLoaded = true;
    }

    @Override
    public void unloadLibrary() {
        if (!this.libraryLoaded) {
            return;
        }
        SDLVideo.SDL_GL_UnloadLibrary();
        this.libraryLoaded = false;
    }

    @Override
    public long createWindow(@Nullable String title, int width, int height, long flags) {
        SDLVideo.SDL_GL_SetAttribute((int)17, (int)3);
        SDLVideo.SDL_GL_SetAttribute((int)18, (int)3);
        SDLVideo.SDL_GL_SetAttribute((int)20, (int)1);
        SDLVideo.SDL_GL_SetAttribute((int)19, (int)2);
        SDLVideo.SDL_GL_SetAttribute((int)22, (int)1);
        return SDLVideo.SDL_CreateWindow((CharSequence)title, (int)width, (int)height, (long)(2L | flags));
    }

    @Override
    public GpuDevice createDevice(GpuDebugOptions debugOptions) throws BackendCreationException {
        return new FrontendGpuDevice(new GlDevice(this, debugOptions));
    }
}


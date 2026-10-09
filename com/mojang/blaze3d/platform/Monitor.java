/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLStdinc
 *  org.lwjgl.sdl.SDLVideo
 *  org.lwjgl.sdl.SDL_DisplayMode
 *  org.lwjgl.sdl.SDL_Rect
 *  org.lwjgl.system.MemoryStack
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.logging.LogUtils;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

public record Monitor(String name, int id, List<VideoMode> videoModes, VideoMode currentMode, int x, int y, int w, int h) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static @Nullable Monitor tryCreate(int id) {
        String name = Monitor.queryMonitorName(id);
        ImmutableList.Builder videoModes = ImmutableList.builder();
        try (MemoryStack stack = MemoryStack.stackPush();){
            VideoMode mode;
            PointerBuffer modes = SDLVideo.SDL_GetFullscreenDisplayModes((int)id);
            if (modes == null) {
                LOGGER.warn("Failed to query video modes of monitor {}: {}", (Object)name, (Object)SDLError.SDL_GetError());
                Monitor monitor = null;
                return monitor;
            }
            try {
                for (int i = 0; i < modes.limit(); ++i) {
                    mode = new VideoMode(SDL_DisplayMode.create((long)modes.get(i)));
                    if (mode.getRedBits() < 8 || mode.getGreenBits() < 8 || mode.getBlueBits() < 8) continue;
                    videoModes.add((Object)mode);
                }
            }
            finally {
                SDLStdinc.SDL_free((PointerBuffer)modes);
            }
            SDL_Rect bounds = SDL_Rect.malloc((MemoryStack)stack);
            if (!SDLVideo.SDL_GetDisplayBounds((int)id, (SDL_Rect)bounds)) {
                LOGGER.warn("Failed to query monitor bounds of {}: {}", (Object)name, (Object)SDLError.SDL_GetError());
                mode = null;
                return mode;
            }
            SDL_DisplayMode currentMode = SDLVideo.SDL_GetDesktopDisplayMode((int)id);
            if (currentMode == null) {
                LOGGER.warn("Failed to query current desktop video mode of monitor {}: {}", (Object)name, (Object)SDLError.SDL_GetError());
                Monitor monitor = null;
                return monitor;
            }
            Monitor monitor = new Monitor(name, id, (List<VideoMode>)videoModes.build(), new VideoMode(currentMode), bounds.x(), bounds.y(), bounds.w(), bounds.h());
            return monitor;
        }
    }

    private static String queryMonitorName(int id) {
        String monitorName = Objects.requireNonNullElse(SDLVideo.SDL_GetDisplayName((int)id), "unknown");
        return monitorName + "[0x" + HEX_FORMAT.toHexDigits(id) + "]";
    }

    public VideoMode getPreferredVideoMode(Optional<VideoMode> expectedMode) {
        return expectedMode.filter(this.videoModes::contains).orElse(this.currentMode);
    }

    public int indexOfMode(VideoMode expectedMode) {
        return this.videoModes.indexOf(expectedMode);
    }

    public VideoMode mode(int mode) {
        return this.videoModes.get(mode);
    }

    public int modeCount() {
        return this.videoModes.size();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s(%s at (%d,%d))", this.name, this.currentMode, this.x, this.y);
    }
}


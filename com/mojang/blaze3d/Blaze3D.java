/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLMisc
 *  org.lwjgl.sdl.SDLTimer
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d;

import com.mojang.logging.LogUtils;
import java.net.URI;
import java.nio.file.Path;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLMisc;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class Blaze3D {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void youJustLostTheGame() {
        MemoryUtil.memSet((long)0L, (int)0, (long)1L);
    }

    public static double getTime() {
        return (double)SDLTimer.SDL_GetTicksNS() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
    }

    private Blaze3D() {
    }

    public static void openUri(URI uri) {
        Util.nonCriticalIoPool().execute(() -> {
            if (!SDLMisc.SDL_OpenURL((CharSequence)uri.toString())) {
                LOGGER.warn("Failed to open uri {}: {}", (Object)uri, (Object)SDLError.SDL_GetError());
            }
        });
    }

    public static void openPath(Path path) {
        Blaze3D.openUri(path.normalize().toUri());
    }
}


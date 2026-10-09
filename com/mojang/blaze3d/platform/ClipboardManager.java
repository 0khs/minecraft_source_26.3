/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.sdl.SDLClipboard
 *  org.lwjgl.sdl.SDLError
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.logging.LogUtils;
import net.minecraft.util.StringDecomposer;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLError;
import org.slf4j.Logger;

public class ClipboardManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public String getClipboard() {
        String clipboard = SDLClipboard.SDL_GetClipboardText();
        if (clipboard == null) {
            LOGGER.error("Failed to read clipboard: {}", (Object)SDLError.SDL_GetError());
            return "";
        }
        return StringDecomposer.filterBrokenSurrogates(clipboard);
    }

    public void setClipboard(String clipboard) {
        if (!SDLClipboard.SDL_SetClipboardText((CharSequence)clipboard)) {
            LOGGER.error("Failed to set clipboard: {}", (Object)SDLError.SDL_GetError());
        }
    }
}


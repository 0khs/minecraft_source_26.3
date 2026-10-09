/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.sdl.SDLLog
 *  org.lwjgl.sdl.SDL_LogOutputFunction
 *  org.lwjgl.sdl.SDL_LogOutputFunctionI
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.logging.LogUtils;
import java.util.HexFormat;
import org.lwjgl.sdl.SDLLog;
import org.lwjgl.sdl.SDL_LogOutputFunction;
import org.lwjgl.sdl.SDL_LogOutputFunctionI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class SdlDebug {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final SDL_LogOutputFunction CALLBACK = SDL_LogOutputFunction.create(SdlDebug::onLogMessage);

    private SdlDebug() {
    }

    public static void init() {
        SDLLog.SDL_SetLogOutputFunction((SDL_LogOutputFunctionI)CALLBACK, (long)0L);
        SDLLog.SDL_SetLogPriorities((int)(LOGGER.isDebugEnabled() ? 3 : 4));
    }

    private static String printUnknownToken(int token) {
        return "Unknown (0x" + HEX_FORMAT.toHexDigits(token) + ")";
    }

    private static String categoryToString(int category) {
        return switch (category) {
            case 0 -> "APPLICATION";
            case 1 -> "ERROR";
            case 2 -> "ASSERT";
            case 3 -> "SYSTEM";
            case 4 -> "AUDIO";
            case 5 -> "VIDEO";
            case 6 -> "RENDER";
            case 7 -> "INPUT";
            case 8 -> "TEST";
            case 9 -> "GPU";
            default -> SdlDebug.printUnknownToken(category);
        };
    }

    private static void onLogMessage(long userData, int category, int priority, long message) {
        String text = message == 0L ? "" : MemoryUtil.memUTF8((long)message);
        String categoryName = SdlDebug.categoryToString(category);
        switch (priority) {
            case 2: 
            case 3: {
                LOGGER.debug("SDL [{}]: {}", (Object)categoryName, (Object)text);
                break;
            }
            case 4: {
                LOGGER.info("SDL [{}]: {}", (Object)categoryName, (Object)text);
                break;
            }
            case 5: {
                LOGGER.warn("SDL [{}]: {}", (Object)categoryName, (Object)text);
                break;
            }
            default: {
                LOGGER.error("SDL [{}]: {}", (Object)categoryName, (Object)text);
            }
        }
    }
}


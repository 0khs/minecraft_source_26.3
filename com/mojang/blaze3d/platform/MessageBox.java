/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLMessageBox
 *  org.lwjgl.sdl.SDL_MessageBoxButtonData
 *  org.lwjgl.sdl.SDL_MessageBoxButtonData$Buffer
 *  org.lwjgl.sdl.SDL_MessageBoxData
 *  org.lwjgl.system.MemoryStack
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.logging.LogUtils;
import java.nio.IntBuffer;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLMessageBox;
import org.lwjgl.sdl.SDL_MessageBoxButtonData;
import org.lwjgl.sdl.SDL_MessageBoxData;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

public class MessageBox {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_TITLE = "Minecraft";
    private static final int BUTTON_ID_YES = 1;
    private static final String BUTTON_TITLE_YES = "Yes";
    private static final int BUTTON_ID_NO = 2;
    private static final String BUTTON_TITLE_NO = "No";

    public static void error(String message) {
        if (!SDLMessageBox.SDL_ShowSimpleMessageBox((int)16, (CharSequence)DEFAULT_TITLE, (CharSequence)message, (long)0L)) {
            String error = SDLError.SDL_GetError();
            LOGGER.error("Failed to show error message '{}': {}", (Object)message, (Object)error);
        }
    }

    public static boolean errorWithContinue(String message) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer buttonResult = stack.callocInt(1);
            SDL_MessageBoxButtonData.Buffer buttonData = SDL_MessageBoxButtonData.calloc((int)2, (MemoryStack)stack);
            ((SDL_MessageBoxButtonData)buttonData.get(0)).buttonID(1).text(stack.UTF8((CharSequence)BUTTON_TITLE_YES));
            ((SDL_MessageBoxButtonData)buttonData.get(1)).buttonID(2).text(stack.UTF8((CharSequence)BUTTON_TITLE_NO));
            SDL_MessageBoxData data = SDL_MessageBoxData.calloc((MemoryStack)stack).flags(16).title(stack.UTF8((CharSequence)DEFAULT_TITLE, true)).message(stack.UTF8((CharSequence)message, true)).buttons(buttonData);
            if (SDLMessageBox.SDL_ShowMessageBox((SDL_MessageBoxData)data, (IntBuffer)buttonResult)) {
                boolean bl = buttonResult.get(0) == 1;
                return bl;
            }
            String error = SDLError.SDL_GetError();
            LOGGER.error("Failed to show error message '{}': {}", (Object)message, (Object)error);
            boolean bl = false;
            return bl;
        }
    }
}


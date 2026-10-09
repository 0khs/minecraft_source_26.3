/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.sdl.SDLMouse
 */
package com.mojang.blaze3d.platform.cursor;

import org.lwjgl.sdl.SDLMouse;

public class CursorType {
    public static final CursorType DEFAULT = new CursorType("default", 0L);
    private final String name;
    private final long handle;

    private CursorType(String name, long handle) {
        this.name = name;
        this.handle = handle;
    }

    public void select() {
        long cursor = this.handle == 0L ? SDLMouse.SDL_GetDefaultCursor() : this.handle;
        SDLMouse.SDL_SetCursor((long)cursor);
    }

    public String toString() {
        return this.name;
    }

    public static CursorType createStandardCursor(int shape, String name, CursorType fallback) {
        long handle = SDLMouse.SDL_CreateSystemCursor((int)shape);
        if (handle == 0L) {
            return fallback;
        }
        return new CursorType(name, handle);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.sdl.SDLEvents
 *  org.lwjgl.sdl.SDLKeyboard
 *  org.lwjgl.sdl.SDL_Event
 *  org.lwjgl.sdl.SDL_KeyboardEvent
 *  org.lwjgl.sdl.SDL_MouseButtonEvent
 *  org.lwjgl.sdl.SDL_MouseMotionEvent
 *  org.lwjgl.sdl.SDL_MouseWheelEvent
 *  org.lwjgl.sdl.SDL_TextEditingEvent
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.input.PreeditEvent;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_KeyboardEvent;
import org.lwjgl.sdl.SDL_MouseButtonEvent;
import org.lwjgl.sdl.SDL_MouseMotionEvent;
import org.lwjgl.sdl.SDL_MouseWheelEvent;
import org.lwjgl.sdl.SDL_TextEditingEvent;

public class SDLEventHandler {
    private final List<String> dropFiles = new ArrayList<String>();
    private final Minecraft minecraft;
    private final Window window;

    public SDLEventHandler(Minecraft minecraft, Window window) {
        this.minecraft = minecraft;
        this.window = window;
    }

    private static long getWindowHandle(SDL_Event event) {
        return SDLEvents.SDL_GetWindowFromEvent((SDL_Event)event);
    }

    public void pollEvents() {
        try (SDL_Event event = SDL_Event.malloc();){
            block17: while (SDLEvents.SDL_PollEvent((SDL_Event)event)) {
                switch (event.type()) {
                    case 768: 
                    case 769: {
                        this.handleKeyEvent(event);
                        continue block17;
                    }
                    case 772: {
                        this.handleKeymapChangedEvent();
                        continue block17;
                    }
                    case 770: {
                        this.handleTextEditingEvent(event);
                        continue block17;
                    }
                    case 771: {
                        this.handleTextInputEvent(event);
                        continue block17;
                    }
                    case 1024: {
                        this.handleMouseMotionEvent(event);
                        continue block17;
                    }
                    case 1025: 
                    case 1026: {
                        this.handleMouseButtonEvent(event);
                        continue block17;
                    }
                    case 1027: {
                        this.handleMouseWheelEvent(event);
                        continue block17;
                    }
                    case 4098: {
                        this.handleDropBeginEvent();
                        continue block17;
                    }
                    case 4096: {
                        this.handleDropFileEvent(event);
                        continue block17;
                    }
                    case 4099: {
                        this.handleDropCompleteEvent(event);
                        continue block17;
                    }
                }
                this.window.handleEvent(event);
            }
        }
    }

    public void pumpEvents() {
        this.flushInputEvents();
        this.pollEvents();
    }

    public void flushInputEvents() {
        SDLEvents.SDL_PumpEvents();
        SDLEvents.SDL_FlushEvents((int)768, (int)4871);
    }

    private void handleKeymapChangedEvent() {
        this.minecraft.execute(() -> {
            Screen patt0$temp = this.minecraft.gui.screen();
            if (patt0$temp instanceof KeyBindsScreen) {
                KeyBindsScreen keyBindsScreen = (KeyBindsScreen)patt0$temp;
                keyBindsScreen.refreshKeybindLabels();
            }
        });
    }

    private void handleKeyEvent(SDL_Event event) {
        SDL_KeyboardEvent keyEvent = event.key();
        int action = event.type() == 769 ? 0 : (keyEvent.repeat() ? -1 : 1);
        KeyEvent key = new KeyEvent(keyEvent.scancode(), keyEvent.key(), keyEvent.mod());
        this.minecraft.execute(() -> this.minecraft.keyboardHandler.keyPress(SDLEventHandler.getWindowHandle(event), action, key));
    }

    private void handleTextInputEvent(SDL_Event event) {
        String text = event.text().textString();
        if (text != null) {
            long handle = SDLEventHandler.getWindowHandle(event);
            this.minecraft.execute(() -> this.minecraft.keyboardHandler.textInput(handle, text));
        }
    }

    private void handleTextEditingEvent(SDL_Event event) {
        SDL_TextEditingEvent edit = event.edit();
        PreeditEvent preedit = PreeditEvent.fromSdlTextEditing(edit.textString(), edit.start(), edit.length());
        long handle = SDLEventHandler.getWindowHandle(event);
        this.minecraft.execute(() -> this.minecraft.keyboardHandler.textEditing(handle, preedit));
    }

    private void handleMouseMotionEvent(SDL_Event event) {
        SDL_MouseMotionEvent motion = event.motion();
        long handle = SDLEventHandler.getWindowHandle(event);
        this.minecraft.execute(() -> this.minecraft.mouseHandler.onMove(handle, motion.x(), motion.y(), motion.xrel(), motion.yrel()));
    }

    private void handleMouseButtonEvent(SDL_Event event) {
        SDL_MouseButtonEvent buttonEvent = event.button();
        int action = event.type() == 1025 ? 1 : 0;
        MouseButtonInfo buttonInfo = new MouseButtonInfo(buttonEvent.button(), SDLKeyboard.SDL_GetModState());
        long handle = SDLEventHandler.getWindowHandle(event);
        this.minecraft.execute(() -> this.minecraft.mouseHandler.onButton(handle, buttonInfo, action));
    }

    private void handleDropFileEvent(SDL_Event event) {
        String data = event.drop().dataString();
        if (data != null) {
            this.dropFiles.add(data);
        }
    }

    private void handleDropBeginEvent() {
        this.dropFiles.clear();
    }

    private void handleMouseWheelEvent(SDL_Event event) {
        SDL_MouseWheelEvent wheel = event.wheel();
        Long handle = SDLEventHandler.getWindowHandle(event);
        boolean invertedScroll = SDLEventHandler.isShiftInvertedScroll(wheel.y(), wheel.x());
        double scrollX = invertedScroll ? 0.0 : (double)wheel.x();
        double scrollY = invertedScroll ? (double)(-wheel.x()) : (double)wheel.y();
        this.minecraft.execute(() -> this.minecraft.mouseHandler.onScroll(handle, scrollX, scrollY));
    }

    private static boolean isShiftInvertedScroll(double y, double x) {
        return InputQuirks.SHIFT_INVERTS_SCROLL_AXIS && y == 0.0 && x != 0.0 && (SDLKeyboard.SDL_GetModState() & 3) != 0;
    }

    private void handleDropCompleteEvent(SDL_Event event) {
        if (!this.dropFiles.isEmpty()) {
            long handle = SDLEventHandler.getWindowHandle(event);
            List<String> files = List.copyOf(this.dropFiles);
            this.minecraft.execute(() -> this.minecraft.mouseHandler.onDrop(handle, files));
        }
        this.dropFiles.clear();
    }
}


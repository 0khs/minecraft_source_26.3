/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.sdl.SDLKeyboard
 */
package net.minecraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLKeyboard;

public class InputQuirks {
    private static final Util.OS PLATFORM = Util.getPlatform();
    private static final boolean ON_WINDOWS = PLATFORM == Util.OS.WINDOWS;
    private static final boolean ON_OSX;
    public static final boolean REPLACE_CTRL_KEY_WITH_CMD_KEY;
    public static final int EDIT_SHORTCUT_KEY_MODIFIER;
    public static final boolean SHIFT_INVERTS_SCROLL_AXIS;
    public static final boolean EMULATE_RIGHT_CLICK_WITH_CTRL_KEY;
    public static final boolean RESTORE_KEY_STATE_AFTER_MOUSE_GRAB;
    private static final Map<String, String> KEYBOARD_DISPLAY_OVERRIDES;

    public static boolean isQuitShortcutDown() {
        short modifiers = SDLKeyboard.SDL_GetModState();
        return ON_OSX ? (modifiers & 0xC00) != 0 && InputConstants.isKeyDown(20) : (modifiers & 0x300) != 0 && InputConstants.isKeyDown(61);
    }

    public static String keyboardTranslationKey(String name) {
        return KEYBOARD_DISPLAY_OVERRIDES.getOrDefault(name, name);
    }

    static {
        REPLACE_CTRL_KEY_WITH_CMD_KEY = ON_OSX = PLATFORM == Util.OS.OSX;
        EDIT_SHORTCUT_KEY_MODIFIER = REPLACE_CTRL_KEY_WITH_CMD_KEY ? 3072 : 192;
        SHIFT_INVERTS_SCROLL_AXIS = ON_OSX;
        EMULATE_RIGHT_CLICK_WITH_CTRL_KEY = ON_OSX;
        RESTORE_KEY_STATE_AFTER_MOUSE_GRAB = !ON_OSX;
        KEYBOARD_DISPLAY_OVERRIDES = switch (PLATFORM) {
            case Util.OS.OSX -> Map.of("key.keyboard.left.alt", "key.keyboard.left.option", "key.keyboard.right.alt", "key.keyboard.right.option", "key.keyboard.left.win", "key.keyboard.left.command", "key.keyboard.right.win", "key.keyboard.right.command");
            case Util.OS.WINDOWS -> Map.of("key.keyboard.left.win", "key.keyboard.left.windows", "key.keyboard.right.win", "key.keyboard.right.windows");
            case Util.OS.LINUX -> Map.of("key.keyboard.left.win", "key.keyboard.left.meta", "key.keyboard.right.win", "key.keyboard.right.meta");
            default -> Map.of();
        };
    }
}


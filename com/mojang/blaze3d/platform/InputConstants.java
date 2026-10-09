/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLKeyboard
 *  org.lwjgl.sdl.SDLMouse
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLMouse;
import org.slf4j.Logger;

public class InputConstants {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int KEY_0 = 39;
    public static final int KEY_1 = 30;
    public static final int KEY_2 = 31;
    public static final int KEY_3 = 32;
    public static final int KEY_4 = 33;
    public static final int KEY_5 = 34;
    public static final int KEY_6 = 35;
    public static final int KEY_7 = 36;
    public static final int KEY_8 = 37;
    public static final int KEY_9 = 38;
    public static final int KEY_A = 4;
    public static final int KEY_B = 5;
    public static final int KEY_C = 6;
    public static final int KEY_D = 7;
    public static final int KEY_E = 8;
    public static final int KEY_F = 9;
    public static final int KEY_G = 10;
    public static final int KEY_H = 11;
    public static final int KEY_I = 12;
    public static final int KEY_J = 13;
    public static final int KEY_K = 14;
    public static final int KEY_L = 15;
    public static final int KEY_M = 16;
    public static final int KEY_N = 17;
    public static final int KEY_O = 18;
    public static final int KEY_P = 19;
    public static final int KEY_Q = 20;
    public static final int KEY_R = 21;
    public static final int KEY_S = 22;
    public static final int KEY_T = 23;
    public static final int KEY_U = 24;
    public static final int KEY_V = 25;
    public static final int KEY_W = 26;
    public static final int KEY_X = 27;
    public static final int KEY_Y = 28;
    public static final int KEY_Z = 29;
    public static final int KEY_F1 = 58;
    public static final int KEY_F2 = 59;
    public static final int KEY_F3 = 60;
    public static final int KEY_F4 = 61;
    public static final int KEY_F5 = 62;
    public static final int KEY_F6 = 63;
    public static final int KEY_F7 = 64;
    public static final int KEY_F8 = 65;
    public static final int KEY_F9 = 66;
    public static final int KEY_F10 = 67;
    public static final int KEY_F11 = 68;
    public static final int KEY_F12 = 69;
    public static final int KEY_F13 = 104;
    public static final int KEY_F14 = 105;
    public static final int KEY_F15 = 106;
    public static final int KEY_F16 = 107;
    public static final int KEY_F17 = 108;
    public static final int KEY_F18 = 109;
    public static final int KEY_F19 = 110;
    public static final int KEY_F20 = 111;
    public static final int KEY_F21 = 112;
    public static final int KEY_F22 = 113;
    public static final int KEY_F23 = 114;
    public static final int KEY_F24 = 115;
    public static final int KEY_NUMLOCK = 83;
    public static final int KEY_NUMPAD0 = 98;
    public static final int KEY_NUMPAD1 = 89;
    public static final int KEY_NUMPAD2 = 90;
    public static final int KEY_NUMPAD3 = 91;
    public static final int KEY_NUMPAD4 = 92;
    public static final int KEY_NUMPAD5 = 93;
    public static final int KEY_NUMPAD6 = 94;
    public static final int KEY_NUMPAD7 = 95;
    public static final int KEY_NUMPAD8 = 96;
    public static final int KEY_NUMPAD9 = 97;
    public static final int KEY_NUMPADCOMMA = 220;
    public static final int KEY_NUMPADENTER = 88;
    public static final int KEY_NUMPADEQUALS = 103;
    public static final int KEY_DOWN = 81;
    public static final int KEY_LEFT = 80;
    public static final int KEY_RIGHT = 79;
    public static final int KEY_UP = 82;
    public static final int KEY_ADD = 87;
    public static final int KEY_APOSTROPHE = 52;
    public static final int KEY_BACKSLASH = 49;
    public static final int KEY_COMMA = 54;
    public static final int KEY_EQUALS = 46;
    public static final int KEY_GRAVE = 53;
    public static final int KEY_LBRACKET = 47;
    public static final int KEY_MINUS = 45;
    public static final int KEY_MULTIPLY = 85;
    public static final int KEY_PERIOD = 55;
    public static final int KEY_RBRACKET = 48;
    public static final int KEY_SEMICOLON = 51;
    public static final int KEY_SLASH = 56;
    public static final int KEY_SPACE = 44;
    public static final int KEY_TAB = 43;
    public static final int KEY_LALT = 226;
    public static final int KEY_LCONTROL = 224;
    public static final int KEY_LSHIFT = 225;
    public static final int KEY_LGUI = 227;
    public static final int KEY_RALT = 230;
    public static final int KEY_RCONTROL = 228;
    public static final int KEY_RSHIFT = 229;
    public static final int KEY_RGUI = 231;
    public static final int KEY_RETURN = 40;
    public static final int KEY_ESCAPE = 41;
    public static final int KEY_BACKSPACE = 42;
    public static final int KEY_DELETE = 76;
    public static final int KEY_END = 77;
    public static final int KEY_HOME = 74;
    public static final int KEY_INSERT = 73;
    public static final int KEY_PAGEDOWN = 78;
    public static final int KEY_PAGEUP = 75;
    public static final int KEY_CAPSLOCK = 57;
    public static final int KEY_PAUSE = 72;
    public static final int KEY_SCROLLLOCK = 71;
    public static final int KEY_PRINTSCREEN = 70;
    public static final int PRESS = 1;
    public static final int RELEASE = 0;
    public static final int REPEAT = -1;
    public static final int MOUSE_BUTTON_LEFT = 1;
    public static final int MOUSE_BUTTON_MIDDLE = 2;
    public static final int MOUSE_BUTTON_RIGHT = 3;
    public static final int MOUSE_BUTTON_4 = 4;
    public static final int MOUSE_BUTTON_5 = 5;
    public static final int MOUSE_BUTTON_6 = 6;
    public static final int MOUSE_BUTTON_7 = 7;
    public static final int MOUSE_BUTTON_8 = 8;
    public static final int MOD_SHIFT = 3;
    public static final int MOD_CONTROL = 192;
    public static final int MOD_ALT = 768;
    public static final int MOD_SUPER = 3072;
    public static final int MOD_CAPS_LOCK = 8192;
    public static final int MOD_NUM_LOCK = 4096;
    public static final int KEYCODE_A = 97;
    public static final int KEYCODE_B = 98;
    public static final int KEYCODE_C = 99;
    public static final int KEYCODE_E = 101;
    public static final int KEYCODE_F = 102;
    public static final int KEYCODE_L = 108;
    public static final int KEYCODE_M = 109;
    public static final int KEYCODE_O = 111;
    public static final int KEYCODE_R = 114;
    public static final int KEYCODE_U = 117;
    public static final int KEYCODE_V = 118;
    public static final int KEYCODE_W = 119;
    public static final int KEYCODE_X = 120;
    public static final int KEYCODE_Y = 121;
    public static final int KEYCODE_Z = 122;
    public static final int KEYCODE_RETURN = 13;
    public static final int KEYCODE_NUMPADENTER = 1073741912;
    public static final int KEYCODE_PAGEUP = 0x4000004B;
    public static final int KEYCODE_PAGEDOWN = 0x4000004E;
    public static final int KEYCODE_BACKSPACE = 8;
    public static final int KEYCODE_UP = 1073741906;
    public static final int KEYCODE_DOWN = 1073741905;
    public static final int KEYCODE_FORWARD = 1073741906;
    public static final int KEYCODE_BACKWARD = 1073741905;
    public static final int KEYCODE_LEFT = 0x40000050;
    public static final int KEYCODE_RIGHT = 0x4000004F;
    public static final int KEYCODE_NUMPAD9 = 1073741921;
    public static final int KEYCODE_NUMPAD3 = 1073741915;
    public static final int KEYCODE_DELETE = 127;
    public static final int KEYCODE_HOME = 0x4000004A;
    public static final int KEYCODE_END = 0x4000004D;
    public static final int KEYCODE_F5 = 1073741886;
    public static final int KEYCODE_TAB = 9;
    public static final int KEYCODE_LCONTROL = 0x400000E0;
    public static final int KEYCODE_RCONTROL = 0x400000E4;
    public static final int KEYCODE_SPACE = 32;
    public static final Key UNKNOWN = Type.KEYBOARD.getOrCreate(0);

    public static Key getKey(KeyEvent event) {
        return Type.KEYBOARD.getOrCreate(event.key());
    }

    public static Key getKey(String name) {
        if (Key.NAME_MAP.containsKey(name)) {
            return Key.NAME_MAP.get(name);
        }
        for (Type type : Type.values()) {
            if (!name.startsWith(type.defaultPrefix)) continue;
            String humanReadableValue = name.substring(type.defaultPrefix.length() + 1);
            int intValue = Integer.parseInt(humanReadableValue);
            return type.getOrCreate(intValue);
        }
        throw new IllegalArgumentException("Unknown key name: " + name);
    }

    public static boolean isKeyDown(int key) {
        ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
        return keyboardState != null && keyboardState.get(key) != 0;
    }

    public static void grabMouse(Window window, double xpos, double ypos) {
        SDLMouse.SDL_WarpMouseInWindow((long)window.handle(), (float)((float)xpos), (float)((float)ypos));
        if (!SDLMouse.SDL_SetWindowRelativeMouseMode((long)window.handle(), (boolean)true)) {
            LOGGER.warn("Failed to enable relative mouse mode: {}", (Object)SDLError.SDL_GetError());
        }
    }

    public static void releaseMouse(Window window, double xpos, double ypos) {
        SDLMouse.SDL_WarpMouseInWindow((long)window.handle(), (float)((float)xpos), (float)((float)ypos));
        if (!SDLMouse.SDL_SetWindowRelativeMouseMode((long)window.handle(), (boolean)false)) {
            LOGGER.warn("Failed to disable relative mouse mode: {}", (Object)SDLError.SDL_GetError());
        }
    }

    public static enum Type {
        KEYBOARD("key.keyboard", (value, name) -> {
            if (KEY_KEYBOARD_UNKNOWN.equals(name)) {
                return Component.translatable(name);
            }
            int keycode = SDLKeyboard.SDL_GetKeyFromScancode((int)value, (short)0, (boolean)false);
            String systemName = SDLKeyboard.SDL_GetKeyName((int)keycode);
            if (systemName != null && systemName.codePointCount(0, systemName.length()) == 1) {
                return Component.literal(systemName.toUpperCase(Locale.ROOT));
            }
            return Component.translatable(InputQuirks.keyboardTranslationKey(name));
        }),
        MOUSE("key.mouse", (value, name) -> Language.getInstance().has((String)name) ? Component.translatable(name) : Component.translatable("key.mouse", value));

        private static final String KEY_KEYBOARD_UNKNOWN = "key.keyboard.unknown";
        private final Int2ObjectMap<Key> map = new Int2ObjectOpenHashMap();
        private final String defaultPrefix;
        private final BiFunction<Integer, String, Component> displayTextSupplier;

        private static void addKey(Type type, String name, int value) {
            Key key = new Key(name, type, value);
            type.map.put(value, (Object)key);
        }

        private Type(String defaultPrefix, BiFunction<Integer, String, Component> displayTextSupplier) {
            this.defaultPrefix = defaultPrefix;
            this.displayTextSupplier = displayTextSupplier;
        }

        public Key getOrCreate(int value) {
            return (Key)this.map.computeIfAbsent(value, intValue -> {
                String name = this.defaultPrefix + "." + intValue;
                return new Key(name, this, intValue);
            });
        }

        static {
            Type.addKey(KEYBOARD, KEY_KEYBOARD_UNKNOWN, 0);
            Type.addKey(MOUSE, "key.mouse.left", 1);
            Type.addKey(MOUSE, "key.mouse.right", 3);
            Type.addKey(MOUSE, "key.mouse.middle", 2);
            Type.addKey(MOUSE, "key.mouse.4", 4);
            Type.addKey(MOUSE, "key.mouse.5", 5);
            Type.addKey(MOUSE, "key.mouse.6", 6);
            Type.addKey(MOUSE, "key.mouse.7", 7);
            Type.addKey(MOUSE, "key.mouse.8", 8);
            Type.addKey(KEYBOARD, "key.keyboard.a", 4);
            Type.addKey(KEYBOARD, "key.keyboard.b", 5);
            Type.addKey(KEYBOARD, "key.keyboard.c", 6);
            Type.addKey(KEYBOARD, "key.keyboard.d", 7);
            Type.addKey(KEYBOARD, "key.keyboard.e", 8);
            Type.addKey(KEYBOARD, "key.keyboard.f", 9);
            Type.addKey(KEYBOARD, "key.keyboard.g", 10);
            Type.addKey(KEYBOARD, "key.keyboard.h", 11);
            Type.addKey(KEYBOARD, "key.keyboard.i", 12);
            Type.addKey(KEYBOARD, "key.keyboard.j", 13);
            Type.addKey(KEYBOARD, "key.keyboard.k", 14);
            Type.addKey(KEYBOARD, "key.keyboard.l", 15);
            Type.addKey(KEYBOARD, "key.keyboard.m", 16);
            Type.addKey(KEYBOARD, "key.keyboard.n", 17);
            Type.addKey(KEYBOARD, "key.keyboard.o", 18);
            Type.addKey(KEYBOARD, "key.keyboard.p", 19);
            Type.addKey(KEYBOARD, "key.keyboard.q", 20);
            Type.addKey(KEYBOARD, "key.keyboard.r", 21);
            Type.addKey(KEYBOARD, "key.keyboard.s", 22);
            Type.addKey(KEYBOARD, "key.keyboard.t", 23);
            Type.addKey(KEYBOARD, "key.keyboard.u", 24);
            Type.addKey(KEYBOARD, "key.keyboard.v", 25);
            Type.addKey(KEYBOARD, "key.keyboard.w", 26);
            Type.addKey(KEYBOARD, "key.keyboard.x", 27);
            Type.addKey(KEYBOARD, "key.keyboard.y", 28);
            Type.addKey(KEYBOARD, "key.keyboard.z", 29);
            Type.addKey(KEYBOARD, "key.keyboard.1", 30);
            Type.addKey(KEYBOARD, "key.keyboard.2", 31);
            Type.addKey(KEYBOARD, "key.keyboard.3", 32);
            Type.addKey(KEYBOARD, "key.keyboard.4", 33);
            Type.addKey(KEYBOARD, "key.keyboard.5", 34);
            Type.addKey(KEYBOARD, "key.keyboard.6", 35);
            Type.addKey(KEYBOARD, "key.keyboard.7", 36);
            Type.addKey(KEYBOARD, "key.keyboard.8", 37);
            Type.addKey(KEYBOARD, "key.keyboard.9", 38);
            Type.addKey(KEYBOARD, "key.keyboard.0", 39);
            Type.addKey(KEYBOARD, "key.keyboard.enter", 40);
            Type.addKey(KEYBOARD, "key.keyboard.escape", 41);
            Type.addKey(KEYBOARD, "key.keyboard.backspace", 42);
            Type.addKey(KEYBOARD, "key.keyboard.tab", 43);
            Type.addKey(KEYBOARD, "key.keyboard.space", 44);
            Type.addKey(KEYBOARD, "key.keyboard.minus", 45);
            Type.addKey(KEYBOARD, "key.keyboard.equal", 46);
            Type.addKey(KEYBOARD, "key.keyboard.left.bracket", 47);
            Type.addKey(KEYBOARD, "key.keyboard.right.bracket", 48);
            Type.addKey(KEYBOARD, "key.keyboard.backslash", 49);
            Type.addKey(KEYBOARD, "key.keyboard.world.2", 50);
            Type.addKey(KEYBOARD, "key.keyboard.semicolon", 51);
            Type.addKey(KEYBOARD, "key.keyboard.apostrophe", 52);
            Type.addKey(KEYBOARD, "key.keyboard.grave.accent", 53);
            Type.addKey(KEYBOARD, "key.keyboard.comma", 54);
            Type.addKey(KEYBOARD, "key.keyboard.period", 55);
            Type.addKey(KEYBOARD, "key.keyboard.slash", 56);
            Type.addKey(KEYBOARD, "key.keyboard.caps.lock", 57);
            Type.addKey(KEYBOARD, "key.keyboard.f1", 58);
            Type.addKey(KEYBOARD, "key.keyboard.f2", 59);
            Type.addKey(KEYBOARD, "key.keyboard.f3", 60);
            Type.addKey(KEYBOARD, "key.keyboard.f4", 61);
            Type.addKey(KEYBOARD, "key.keyboard.f5", 62);
            Type.addKey(KEYBOARD, "key.keyboard.f6", 63);
            Type.addKey(KEYBOARD, "key.keyboard.f7", 64);
            Type.addKey(KEYBOARD, "key.keyboard.f8", 65);
            Type.addKey(KEYBOARD, "key.keyboard.f9", 66);
            Type.addKey(KEYBOARD, "key.keyboard.f10", 67);
            Type.addKey(KEYBOARD, "key.keyboard.f11", 68);
            Type.addKey(KEYBOARD, "key.keyboard.f12", 69);
            Type.addKey(KEYBOARD, "key.keyboard.print.screen", 70);
            Type.addKey(KEYBOARD, "key.keyboard.scroll.lock", 71);
            Type.addKey(KEYBOARD, "key.keyboard.pause", 72);
            Type.addKey(KEYBOARD, "key.keyboard.insert", 73);
            Type.addKey(KEYBOARD, "key.keyboard.home", 74);
            Type.addKey(KEYBOARD, "key.keyboard.page.up", 75);
            Type.addKey(KEYBOARD, "key.keyboard.delete", 76);
            Type.addKey(KEYBOARD, "key.keyboard.end", 77);
            Type.addKey(KEYBOARD, "key.keyboard.page.down", 78);
            Type.addKey(KEYBOARD, "key.keyboard.right", 79);
            Type.addKey(KEYBOARD, "key.keyboard.left", 80);
            Type.addKey(KEYBOARD, "key.keyboard.down", 81);
            Type.addKey(KEYBOARD, "key.keyboard.up", 82);
            Type.addKey(KEYBOARD, "key.keyboard.num.lock", 83);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.divide", 84);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.multiply", 85);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.subtract", 86);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.add", 87);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.enter", 88);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.1", 89);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.2", 90);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.3", 91);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.4", 92);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.5", 93);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.6", 94);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.7", 95);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.8", 96);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.9", 97);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.0", 98);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.period", 99);
            Type.addKey(KEYBOARD, "key.keyboard.world.1", 100);
            Type.addKey(KEYBOARD, "key.keyboard.application", 101);
            Type.addKey(KEYBOARD, "key.keyboard.power", 102);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.equal", 103);
            Type.addKey(KEYBOARD, "key.keyboard.f13", 104);
            Type.addKey(KEYBOARD, "key.keyboard.f14", 105);
            Type.addKey(KEYBOARD, "key.keyboard.f15", 106);
            Type.addKey(KEYBOARD, "key.keyboard.f16", 107);
            Type.addKey(KEYBOARD, "key.keyboard.f17", 108);
            Type.addKey(KEYBOARD, "key.keyboard.f18", 109);
            Type.addKey(KEYBOARD, "key.keyboard.f19", 110);
            Type.addKey(KEYBOARD, "key.keyboard.f20", 111);
            Type.addKey(KEYBOARD, "key.keyboard.f21", 112);
            Type.addKey(KEYBOARD, "key.keyboard.f22", 113);
            Type.addKey(KEYBOARD, "key.keyboard.f23", 114);
            Type.addKey(KEYBOARD, "key.keyboard.f24", 115);
            Type.addKey(KEYBOARD, "key.keyboard.execute", 116);
            Type.addKey(KEYBOARD, "key.keyboard.help", 117);
            Type.addKey(KEYBOARD, "key.keyboard.menu", 118);
            Type.addKey(KEYBOARD, "key.keyboard.select", 119);
            Type.addKey(KEYBOARD, "key.keyboard.stop", 120);
            Type.addKey(KEYBOARD, "key.keyboard.again", 121);
            Type.addKey(KEYBOARD, "key.keyboard.undo", 122);
            Type.addKey(KEYBOARD, "key.keyboard.cut", 123);
            Type.addKey(KEYBOARD, "key.keyboard.copy", 124);
            Type.addKey(KEYBOARD, "key.keyboard.paste", 125);
            Type.addKey(KEYBOARD, "key.keyboard.find", 126);
            Type.addKey(KEYBOARD, "key.keyboard.mute", 127);
            Type.addKey(KEYBOARD, "key.keyboard.volume.up", 128);
            Type.addKey(KEYBOARD, "key.keyboard.volume.down", 129);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.comma", 133);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.equals.as400", 134);
            Type.addKey(KEYBOARD, "key.keyboard.international1", 135);
            Type.addKey(KEYBOARD, "key.keyboard.international2", 136);
            Type.addKey(KEYBOARD, "key.keyboard.international3", 137);
            Type.addKey(KEYBOARD, "key.keyboard.international4", 138);
            Type.addKey(KEYBOARD, "key.keyboard.international5", 139);
            Type.addKey(KEYBOARD, "key.keyboard.international6", 140);
            Type.addKey(KEYBOARD, "key.keyboard.international7", 141);
            Type.addKey(KEYBOARD, "key.keyboard.international8", 142);
            Type.addKey(KEYBOARD, "key.keyboard.international9", 143);
            Type.addKey(KEYBOARD, "key.keyboard.lang1", 144);
            Type.addKey(KEYBOARD, "key.keyboard.lang2", 145);
            Type.addKey(KEYBOARD, "key.keyboard.lang3", 146);
            Type.addKey(KEYBOARD, "key.keyboard.lang4", 147);
            Type.addKey(KEYBOARD, "key.keyboard.lang5", 148);
            Type.addKey(KEYBOARD, "key.keyboard.lang6", 149);
            Type.addKey(KEYBOARD, "key.keyboard.lang7", 150);
            Type.addKey(KEYBOARD, "key.keyboard.lang8", 151);
            Type.addKey(KEYBOARD, "key.keyboard.lang9", 152);
            Type.addKey(KEYBOARD, "key.keyboard.alternate.erase", 153);
            Type.addKey(KEYBOARD, "key.keyboard.sys.req", 154);
            Type.addKey(KEYBOARD, "key.keyboard.cancel", 155);
            Type.addKey(KEYBOARD, "key.keyboard.clear", 156);
            Type.addKey(KEYBOARD, "key.keyboard.prior", 157);
            Type.addKey(KEYBOARD, "key.keyboard.enter2", 158);
            Type.addKey(KEYBOARD, "key.keyboard.separator", 159);
            Type.addKey(KEYBOARD, "key.keyboard.out", 160);
            Type.addKey(KEYBOARD, "key.keyboard.oper", 161);
            Type.addKey(KEYBOARD, "key.keyboard.clear.again", 162);
            Type.addKey(KEYBOARD, "key.keyboard.crsel", 163);
            Type.addKey(KEYBOARD, "key.keyboard.exsel", 164);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.00", 176);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.000", 177);
            Type.addKey(KEYBOARD, "key.keyboard.thousands.separator", 178);
            Type.addKey(KEYBOARD, "key.keyboard.decimal.separator", 179);
            Type.addKey(KEYBOARD, "key.keyboard.currency.unit", 180);
            Type.addKey(KEYBOARD, "key.keyboard.currency.subunit", 181);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.left.parenthesis", 182);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.right.parenthesis", 183);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.left.brace", 184);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.right.brace", 185);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.tab", 186);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.backspace", 187);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.a", 188);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.b", 189);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.c", 190);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.d", 191);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.e", 192);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.f", 193);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.xor", 194);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.power", 195);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.percent", 196);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.less", 197);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.greater", 198);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.ampersand", 199);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.double.ampersand", 200);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.vertical.bar", 201);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.double.vertical.bar", 202);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.colon", 203);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.hash", 204);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.space", 205);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.at", 206);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.exclamation", 207);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.store", 208);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.recall", 209);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.clear", 210);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.add", 211);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.subtract", 212);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.multiply", 213);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.memory.divide", 214);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.plus.minus", 215);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.clear", 216);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.clear.entry", 217);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.binary", 218);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.octal", 219);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.decimal", 220);
            Type.addKey(KEYBOARD, "key.keyboard.keypad.hexadecimal", 221);
            Type.addKey(KEYBOARD, "key.keyboard.left.control", 224);
            Type.addKey(KEYBOARD, "key.keyboard.left.shift", 225);
            Type.addKey(KEYBOARD, "key.keyboard.left.alt", 226);
            Type.addKey(KEYBOARD, "key.keyboard.left.win", 227);
            Type.addKey(KEYBOARD, "key.keyboard.right.control", 228);
            Type.addKey(KEYBOARD, "key.keyboard.right.shift", 229);
            Type.addKey(KEYBOARD, "key.keyboard.right.alt", 230);
            Type.addKey(KEYBOARD, "key.keyboard.right.win", 231);
            Type.addKey(KEYBOARD, "key.keyboard.mode", 257);
            Type.addKey(KEYBOARD, "key.keyboard.sleep", 258);
            Type.addKey(KEYBOARD, "key.keyboard.wake", 259);
            Type.addKey(KEYBOARD, "key.keyboard.channel.up", 260);
            Type.addKey(KEYBOARD, "key.keyboard.channel.down", 261);
            Type.addKey(KEYBOARD, "key.keyboard.media.play", 262);
            Type.addKey(KEYBOARD, "key.keyboard.media.pause", 263);
            Type.addKey(KEYBOARD, "key.keyboard.media.record", 264);
            Type.addKey(KEYBOARD, "key.keyboard.media.fast.forward", 265);
            Type.addKey(KEYBOARD, "key.keyboard.media.rewind", 266);
            Type.addKey(KEYBOARD, "key.keyboard.media.next.track", 267);
            Type.addKey(KEYBOARD, "key.keyboard.media.previous.track", 268);
            Type.addKey(KEYBOARD, "key.keyboard.media.stop", 269);
            Type.addKey(KEYBOARD, "key.keyboard.media.eject", 270);
            Type.addKey(KEYBOARD, "key.keyboard.media.play.pause", 271);
            Type.addKey(KEYBOARD, "key.keyboard.media.select", 272);
            Type.addKey(KEYBOARD, "key.keyboard.ac.new", 273);
            Type.addKey(KEYBOARD, "key.keyboard.ac.open", 274);
            Type.addKey(KEYBOARD, "key.keyboard.ac.close", 275);
            Type.addKey(KEYBOARD, "key.keyboard.ac.exit", 276);
            Type.addKey(KEYBOARD, "key.keyboard.ac.save", 277);
            Type.addKey(KEYBOARD, "key.keyboard.ac.print", 278);
            Type.addKey(KEYBOARD, "key.keyboard.ac.properties", 279);
            Type.addKey(KEYBOARD, "key.keyboard.ac.search", 280);
            Type.addKey(KEYBOARD, "key.keyboard.ac.home", 281);
            Type.addKey(KEYBOARD, "key.keyboard.ac.back", 282);
            Type.addKey(KEYBOARD, "key.keyboard.ac.forward", 283);
            Type.addKey(KEYBOARD, "key.keyboard.ac.stop", 284);
            Type.addKey(KEYBOARD, "key.keyboard.ac.refresh", 285);
            Type.addKey(KEYBOARD, "key.keyboard.ac.bookmarks", 286);
            Type.addKey(KEYBOARD, "key.keyboard.soft.left", 287);
            Type.addKey(KEYBOARD, "key.keyboard.soft.right", 288);
            Type.addKey(KEYBOARD, "key.keyboard.call", 289);
            Type.addKey(KEYBOARD, "key.keyboard.end.call", 290);
        }
    }

    public static final class Key {
        private final String name;
        private final Type type;
        private final int value;
        private static final Map<String, Key> NAME_MAP = Maps.newHashMap();

        private Key(String name, Type type, int value) {
            this.name = name;
            this.type = type;
            this.value = value;
            NAME_MAP.put(name, this);
        }

        public Type getType() {
            return this.type;
        }

        public int getValue() {
            return this.value;
        }

        public String getName() {
            return this.name;
        }

        public Component getDisplayName() {
            return this.type.displayTextSupplier.apply(this.value, this.name);
        }

        public OptionalInt getNumericKeyValue() {
            return switch (this.value) {
                case 39, 98 -> OptionalInt.of(0);
                case 30, 89 -> OptionalInt.of(1);
                case 31, 90 -> OptionalInt.of(2);
                case 32, 91 -> OptionalInt.of(3);
                case 33, 92 -> OptionalInt.of(4);
                case 34, 93 -> OptionalInt.of(5);
                case 35, 94 -> OptionalInt.of(6);
                case 36, 95 -> OptionalInt.of(7);
                case 37, 96 -> OptionalInt.of(8);
                case 38, 97 -> OptionalInt.of(9);
                default -> OptionalInt.empty();
            };
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key)o;
            return this.value == key.value && this.type == key.type;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.type, this.value});
        }

        public String toString() {
            return this.name;
        }
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Value {
    }
}


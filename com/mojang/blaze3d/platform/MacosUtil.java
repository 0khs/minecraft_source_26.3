/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ca.weblite.objc.Client
 *  ca.weblite.objc.Proxy
 *  org.lwjgl.sdl.SDLHints
 *  org.lwjgl.system.macosx.ObjCRuntime
 */
package com.mojang.blaze3d.platform;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import java.util.Set;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.system.macosx.ObjCRuntime;

public final class MacosUtil {
    public static final boolean IS_MACOS = Util.getPlatform() == Util.OS.OSX;

    private MacosUtil() {
    }

    public static void disableCloseWindowMenuItem() {
        if (!IS_MACOS) {
            return;
        }
        Proxy windowsMenu = Client.getInstance().sendProxy("NSApplication", "sharedApplication", new Object[0]).sendProxy("windowsMenu", new Object[0]);
        int itemCount = windowsMenu.sendInt("numberOfItems", new Object[0]);
        for (int i = 0; i < itemCount; ++i) {
            Proxy item = windowsMenu.sendProxy("itemAtIndex:", new Object[]{i});
            if (!DisabledActions.SELECTORS.contains((Long)item.sendRaw("action", new Object[0]))) continue;
            item.send("setEnabled:", new Object[]{false});
            item.send("setHidden:", new Object[]{true});
            item.send("setKeyEquivalent:", new Object[]{""});
        }
    }

    public static void setFullscreenMenuVisibility(boolean value) {
        if (!IS_MACOS) {
            return;
        }
        SDLHints.SDL_SetHint((CharSequence)"SDL_VIDEO_MAC_FULLSCREEN_MENU_VISIBILITY", (CharSequence)(value ? "1" : "0"));
    }

    public static void setCtrlClickEmulatesRightClick(boolean value) {
        if (!IS_MACOS) {
            return;
        }
        SDLHints.SDL_SetHint((CharSequence)"SDL_MAC_CTRL_CLICK_EMULATE_RIGHT_CLICK", (CharSequence)(value ? "1" : "0"));
    }

    private static final class DisabledActions {
        private static final Set<Long> SELECTORS = Set.of(Long.valueOf(ObjCRuntime.sel_getUid((CharSequence)"performClose:")), Long.valueOf(ObjCRuntime.sel_getUid((CharSequence)"closeAll:")));

        private DisabledActions() {
        }
    }
}


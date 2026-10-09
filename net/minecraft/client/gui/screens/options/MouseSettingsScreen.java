/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.options;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class MouseSettingsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.mouse_settings.title");

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.sensitivity(), options.mouseWheelSensitivity(), options.discreteMouseScroll(), options.invertMouseX(), options.invertMouseY(), options.allowCursorChanges()};
    }

    public MouseSettingsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(MouseSettingsScreen.options(this.options));
    }
}


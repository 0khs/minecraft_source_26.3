/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.dialog.input;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.input.InputControl;

@FunctionalInterface
public interface InputControlHandler<T extends InputControl> {
    public void addControl(T var1, Screen var2, Output var3);

    @FunctionalInterface
    public static interface Output {
        public void accept(LayoutElement var1, Action.ValueGetter var2);
    }
}


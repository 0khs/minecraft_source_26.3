/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;

public abstract class AbstractOptionSliderButton
extends AbstractSliderButton {
    protected final Options options;

    protected AbstractOptionSliderButton(Options options, int x, int y, int width, int height, double initialValue) {
        super(x, y, width, height, CommonComponents.EMPTY, initialValue);
        this.options = options;
    }
}


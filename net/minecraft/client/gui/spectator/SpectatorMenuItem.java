/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.spectator;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.network.chat.Component;

public interface SpectatorMenuItem {
    public void selectItem(SpectatorMenu var1);

    public Component getName();

    public void extractIcon(GuiGraphicsExtractor var1, float var2, float var3);

    public boolean isEnabled();
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;

public class GameRenderState {
    public final LevelRenderState levelRenderState = new LevelRenderState();
    public final LightmapRenderState lightmapRenderState = new LightmapRenderState();
    public final GuiRenderState guiRenderState = new GuiRenderState();
    public final OptionsRenderState optionsRenderState = new OptionsRenderState();
    public final WindowRenderState windowRenderState = new WindowRenderState();
    public final List<Identifier> requestedPostEffects = new ArrayList<Identifier>();
    public boolean shouldRenderLevel;
    public int framerateLimit;
    public boolean readyForLevelRendering;
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.WeatherEffectRenderer;

public class WeatherRenderState {
    public final List<WeatherEffectRenderer.ColumnInstance> rainColumns = new ArrayList<WeatherEffectRenderer.ColumnInstance>();
    public final List<WeatherEffectRenderer.ColumnInstance> snowColumns = new ArrayList<WeatherEffectRenderer.ColumnInstance>();
    public float intensity;
    public int radius;

    public void reset() {
        this.rainColumns.clear();
        this.snowColumns.clear();
        this.intensity = 0.0f;
        this.radius = 0;
    }
}


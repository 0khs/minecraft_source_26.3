/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3fc
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer.state.level;

import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class SkyRenderState {
    public DimensionType.Skybox skybox = DimensionType.Skybox.NONE;
    public boolean shouldRenderDarkDisc;
    public float sunAngle;
    public float moonAngle;
    public float starAngle;
    public float rainBrightness;
    public float starBrightness;
    public Vector4fc sunriseAndSunsetColor;
    public MoonPhase moonPhase = MoonPhase.FULL_MOON;
    public Vector3fc skyColor;
    public float endFlashIntensity;
    public float endFlashXAngle;
    public float endFlashYAngle;

    public void reset() {
        this.skybox = DimensionType.Skybox.NONE;
    }
}


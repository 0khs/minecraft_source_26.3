/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class PowderedSnowFogEnvironment
extends FogEnvironment {
    private static final Vector3fc COLOR = ARGB.vector3fFromRGB24(-6308916);

    @Override
    public Vector3fc getBaseColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks) {
        return COLOR;
    }

    @Override
    public void setupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker) {
        if (camera.entity().isSpectator()) {
            fog.environmentalStart = -8.0f;
            fog.environmentalEnd = renderDistance * 0.5f;
        } else {
            fog.environmentalStart = 0.0f;
            fog.environmentalEnd = 2.0f;
        }
        fog.skyEnd = fog.environmentalEnd;
        fog.cloudEnd = fog.environmentalEnd;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.POWDER_SNOW;
    }
}


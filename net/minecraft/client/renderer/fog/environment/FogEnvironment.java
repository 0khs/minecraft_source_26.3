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
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public abstract class FogEnvironment {
    private static final Vector3fc WHITE = ARGB.vector3fFromRGB24(-1);

    public abstract void setupFog(FogData var1, Camera var2, ClientLevel var3, float var4, DeltaTracker var5);

    public boolean providesColor() {
        return true;
    }

    public Vector3fc getBaseColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks) {
        return WHITE;
    }

    public boolean modifiesDarkness() {
        return false;
    }

    public float getModifiedDarkness(LivingEntity entity, float darkness, float partialTickTime) {
        return darkness;
    }

    public abstract boolean isApplicable(@Nullable FogType var1, Entity var2);
}


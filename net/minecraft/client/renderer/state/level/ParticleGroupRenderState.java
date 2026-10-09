/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.level;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface ParticleGroupRenderState {
    public void submit(SubmitNodeCollector var1, CameraRenderState var2);

    default public void clear() {
    }
}


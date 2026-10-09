/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

public class NoRenderParticleGroup
extends ParticleGroup<NoRenderParticle> {
    private static final ParticleGroupRenderState EMPTY_RENDER_STATE = (ignored, camera) -> {};

    public NoRenderParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        return EMPTY_RENDER_STATE;
    }
}


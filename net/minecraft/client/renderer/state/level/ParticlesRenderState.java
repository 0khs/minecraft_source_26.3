/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

public class ParticlesRenderState {
    public final List<ParticleGroupRenderState> particles = new ArrayList<ParticleGroupRenderState>();

    public void reset() {
        this.particles.forEach(ParticleGroupRenderState::clear);
        this.particles.clear();
    }

    public void add(ParticleGroupRenderState state) {
        this.particles.add(state);
    }

    public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (ParticleGroupRenderState particle : this.particles) {
            particle.submit(submitNodeCollector, camera);
        }
    }
}


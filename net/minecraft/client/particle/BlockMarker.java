/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMarker
extends SingleQuadParticle {
    private final SingleQuadParticle.Layer layer;

    private BlockMarker(ClientLevel level, double x, double y, double z, BlockState state) {
        super(level, x, y, z, Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite());
        this.gravity = 0.0f;
        this.lifetime = 80;
        this.hasPhysics = false;
        this.layer = SingleQuadParticle.Layer.bySprite(this.sprite);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    @Override
    public float getQuadSize(float a) {
        return 0.5f;
    }

    public static class Provider
    implements ParticleProvider<BlockParticleOption> {
        @Override
        public Particle createParticle(BlockParticleOption option, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new BlockMarker(level, x, y, z, option.getState());
        }
    }
}


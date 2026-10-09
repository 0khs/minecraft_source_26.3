/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public interface ParticleProvider<T extends ParticleOptions> {
    public @Nullable Particle createParticle(T var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13, RandomSource var15);

    public static interface Sprite<T extends ParticleOptions> {
        public @Nullable SingleQuadParticle createParticle(T var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13, RandomSource var15);
    }
}


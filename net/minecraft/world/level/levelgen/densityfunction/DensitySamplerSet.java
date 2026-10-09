/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;

@FunctionalInterface
public interface DensitySamplerSet {
    public DensitySampler.Bound get(DensityFunction var1);

    default public float sampleValue(DensityFunction function, int blockX, int blockY, int blockZ) {
        return this.get(function).sampleValue(blockX, blockY, blockZ);
    }
}


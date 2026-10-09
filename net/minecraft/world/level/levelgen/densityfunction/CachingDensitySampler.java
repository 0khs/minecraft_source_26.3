/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record CachingDensitySampler(int id, DensitySampler input) implements DensitySampler
{
    @Override
    public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
        context.sampleVolumeCached(this.id, this.input, outputBuffer, volume);
    }

    @Override
    public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
        return context.sampleValueCached(this.id, this.input, blockX, blockY, blockZ);
    }
}


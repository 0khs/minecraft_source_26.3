/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record ContextBoundSampler(ContextKey<? extends DensitySampler> key, DensitySampler fallbackSampler) implements DensitySampler
{
    @Override
    public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
        DensitySampler sampler = context.getFieldOrDefault(this.key, this.fallbackSampler);
        sampler.sampleVolume(context, outputBuffer, volume);
    }

    @Override
    public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
        DensitySampler sampler = context.getFieldOrDefault(this.key, this.fallbackSampler);
        return sampler.sampleValue(context, blockX, blockY, blockZ);
    }
}


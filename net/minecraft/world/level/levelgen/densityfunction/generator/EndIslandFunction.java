/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.densityfunction.generator;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public record EndIslandFunction() implements DensityFunction
{
    public static final MapCodec<EndIslandFunction> CODEC = MapCodec.unit((Object)new EndIslandFunction());
    private static final float ISLAND_THRESHOLD = -0.9f;

    private static float getHeightValue(SimplexNoise islandNoise, int sectionX, int sectionZ) {
        int chunkX = sectionX / 2;
        int chunkZ = sectionZ / 2;
        int subSectionX = sectionX % 2;
        int subSectionZ = sectionZ % 2;
        float doffs = -100.0f;
        for (int xo = -12; xo <= 12; ++xo) {
            for (int zo = -12; zo <= 12; ++zo) {
                long totalChunkX = chunkX + xo;
                long totalChunkZ = chunkZ + zo;
                if (totalChunkX * totalChunkX + totalChunkZ * totalChunkZ <= 4096L || !(islandNoise.get(totalChunkX, totalChunkZ) < -0.9f)) continue;
                float islandSize = (Mth.abs(totalChunkX) * 3439.0f + Mth.abs(totalChunkZ) * 147.0f) % 13.0f + 9.0f;
                float xd = subSectionX - xo * 2;
                float zd = subSectionZ - zo * 2;
                float newDoffs = 100.0f - Mth.sqrt(xd * xd + zd * zd) * islandSize;
                newDoffs = Mth.clamp(newDoffs, -100.0f, 80.0f);
                doffs = Math.max(doffs, newDoffs);
            }
        }
        return doffs;
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        RandomSource islandRandom = context.createEndIslandRandom();
        islandRandom.consumeCount(17292);
        SimplexNoise islandNoise = new SimplexNoise(islandRandom, true);
        return new Sampler(islandNoise);
    }

    @Override
    public Interval range() {
        return Interval.of(-0.84375f, 0.5625f);
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return 5;
    }

    public MapCodec<EndIslandFunction> codec() {
        return CODEC;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    private record Sampler(SimplexNoise islandNoise) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            for (int z = 0; z < volume.sizeZ(); ++z) {
                int blockZ = volume.blockZ(z);
                for (int x = 0; x < volume.sizeX(); ++x) {
                    int blockX = volume.blockX(x);
                    float value = this.sampleValue(context, blockX, 0, blockZ);
                    int index = volume.indexUnchecked(x, 0, z);
                    outputBuffer.setRange(index, volume.sizeY(), value);
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (EndIslandFunction.getHeightValue(this.islandNoise, blockX / 8, blockZ / 8) - 8.0f) / 128.0f;
        }
    }
}


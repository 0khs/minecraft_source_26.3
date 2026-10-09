/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.densityfunction.generator;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record NoiseFunction(Holder<NormalNoise> noise, @Deprecated double xzScale, double yScale, DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ) implements DensityFunction
{
    public static final MapCodec<NoiseFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)NormalNoise.CODEC.fieldOf("noise").forGetter(NoiseFunction::noise), (App)Codec.DOUBLE.fieldOf("xz_scale").forGetter(NoiseFunction::xzScale), (App)Codec.DOUBLE.fieldOf("y_scale").forGetter(NoiseFunction::yScale), (App)DensityFunction.CODEC.optionalFieldOf("shift_x", (Object)DensityFunctions.zero()).forGetter(NoiseFunction::shiftX), (App)DensityFunction.CODEC.optionalFieldOf("shift_y", (Object)DensityFunctions.zero()).forGetter(NoiseFunction::shiftY), (App)DensityFunction.CODEC.optionalFieldOf("shift_z", (Object)DensityFunctions.zero()).forGetter(NoiseFunction::shiftZ)).apply((Applicative)i, NoiseFunction::new));

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        Noise noise = context.createNoiseSampler(this.noise);
        if (this.shiftX.equals(DensityFunctions.zero()) && this.shiftY.equals(DensityFunctions.zero()) && this.shiftZ.equals(DensityFunctions.zero())) {
            return new Sampler(noise, this.xzScale, this.yScale);
        }
        DensitySampler shiftX = this.shiftX.compileSampler(context);
        DensitySampler shiftZ = this.shiftZ.compileSampler(context);
        if (this.shiftY.equals(DensityFunctions.zero())) {
            return new ShiftedXzSampler(shiftX, shiftZ, noise, this.xzScale, this.yScale);
        }
        DensitySampler shiftY = this.shiftY.compileSampler(context);
        return new ShiftedXyzSampler(shiftX, shiftY, shiftZ, noise, this.xzScale, this.yScale);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction shiftX = rule.rewrite(this.shiftX);
        DensityFunction shiftY = rule.rewrite(this.shiftY);
        DensityFunction shiftZ = rule.rewrite(this.shiftZ);
        if (shiftX == this.shiftX && shiftY == this.shiftY && shiftZ == this.shiftZ) {
            return this;
        }
        return new NoiseFunction(this.noise, this.xzScale, this.yScale, shiftX, shiftY, shiftZ);
    }

    @Override
    public Interval range() {
        return this.noise.value().range();
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        int axes = 7;
        if (this.yScale == 0.0) {
            axes &= 0xFFFFFFFD;
        }
        if (this.xzScale == 0.0) {
            axes &= 0xFFFFFFFA;
        }
        return axes | this.shiftX.domainAxes() | this.shiftY.domainAxes() | this.shiftZ.domainAxes();
    }

    public MapCodec<NoiseFunction> codec() {
        return CODEC;
    }

    public record Sampler(Noise noise, double xzScale, double yScale) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            outputBuffer.fill(0.0f);
            this.noise.addToVolume(outputBuffer, volume, this.xzScale, this.yScale, 1.0f);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.noise.get((double)blockX * this.xzScale, (double)blockY * this.yScale, (double)blockZ * this.xzScale);
        }
    }

    public record ShiftedXzSampler(DensitySampler shiftX, DensitySampler shiftZ, Noise noise, double xzScale, double yScale) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.shiftX.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer shiftZBuffer = context.acquireBuffer(volume);){
                this.shiftZ.sampleVolume(context, shiftZBuffer, volume);
                int index = 0;
                for (int z = 0; z < volume.sizeZ(); ++z) {
                    double baseNoiseZ = (double)volume.blockZ(z) * this.xzScale;
                    for (int x = 0; x < volume.sizeX(); ++x) {
                        double baseNoiseX = (double)volume.blockX(x) * this.xzScale;
                        for (int y = 0; y < volume.sizeY(); ++y) {
                            double noiseX = baseNoiseX + (double)outputBuffer.get(index);
                            double noiseY = (double)volume.blockY(y) * this.yScale;
                            double noiseZ = baseNoiseZ + (double)shiftZBuffer.get(index);
                            outputBuffer.set(index, this.noise.get(noiseX, noiseY, noiseZ));
                            ++index;
                        }
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            double x = (double)blockX * this.xzScale + (double)this.shiftX.sampleValue(context, blockX, blockY, blockZ);
            double y = (double)blockY * this.yScale;
            double z = (double)blockZ * this.xzScale + (double)this.shiftZ.sampleValue(context, blockX, blockY, blockZ);
            return this.noise.get(x, y, z);
        }
    }

    public record ShiftedXyzSampler(DensitySampler shiftX, DensitySampler shiftY, DensitySampler shiftZ, Noise noise, double xzScale, double yScale) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.shiftX.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer shiftYBuffer = context.acquireBuffer(volume);){
                this.shiftY.sampleVolume(context, shiftYBuffer, volume);
                try (ScopedDensityBuffer shiftZBuffer = context.acquireBuffer(volume);){
                    this.shiftZ.sampleVolume(context, shiftZBuffer, volume);
                    int index = 0;
                    for (int z = 0; z < volume.sizeZ(); ++z) {
                        double baseNoiseZ = (double)volume.blockZ(z) * this.xzScale;
                        for (int x = 0; x < volume.sizeX(); ++x) {
                            double baseNoiseX = (double)volume.blockX(x) * this.xzScale;
                            for (int y = 0; y < volume.sizeY(); ++y) {
                                double noiseX = baseNoiseX + (double)outputBuffer.get(index);
                                double noiseY = (double)volume.blockY(y) * this.yScale + (double)shiftYBuffer.get(index);
                                double noiseZ = baseNoiseZ + (double)shiftZBuffer.get(index);
                                outputBuffer.set(index, this.noise.get(noiseX, noiseY, noiseZ));
                                ++index;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            double x = (double)blockX * this.xzScale + (double)this.shiftX.sampleValue(context, blockX, blockY, blockZ);
            double y = (double)blockY * this.yScale + (double)this.shiftY.sampleValue(context, blockX, blockY, blockZ);
            double z = (double)blockZ * this.xzScale + (double)this.shiftZ.sampleValue(context, blockX, blockY, blockZ);
            return this.noise.get(x, y, z);
        }
    }
}


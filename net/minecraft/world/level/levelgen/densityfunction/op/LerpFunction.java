/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.densityfunction.op;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;

public record LerpFunction(DensityFunction alpha, DensityFunction first, DensityFunction second) implements DensityFunction
{
    public static final MapCodec<LerpFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("alpha").forGetter(LerpFunction::alpha), (App)DensityFunction.CODEC.fieldOf("first").forGetter(LerpFunction::first), (App)DensityFunction.CODEC.fieldOf("second").forGetter(LerpFunction::second)).apply((Applicative)i, LerpFunction::new));

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        float f;
        float f2;
        DensityFunction densityFunction;
        DensitySampler second;
        DensitySampler first;
        DensitySampler alpha;
        block4: {
            alpha = this.alpha.compileSampler(context);
            first = this.first.compileSampler(context);
            second = this.second.compileSampler(context);
            densityFunction = this.first;
            if (densityFunction instanceof ConstantFunction) {
                ConstantFunction constantFunction = (ConstantFunction)densityFunction;
                f = f2 = constantFunction.value();
                if (!true) break block4;
                float firstValue = f2;
                return new ConstFirstSampler(alpha, firstValue, second);
            }
        }
        if (!((densityFunction = this.second) instanceof ConstantFunction)) return new Sampler(alpha, first, second);
        ConstantFunction constantFunction = (ConstantFunction)densityFunction;
        try {
            f = f2 = constantFunction.value();
            if (!true) return new Sampler(alpha, first, second);
            float secondValue = f2;
            return new ConstSecondSampler(alpha, first, secondValue);
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction alpha = rule.rewrite(this.alpha);
        DensityFunction first = rule.rewrite(this.first);
        DensityFunction second = rule.rewrite(this.second);
        if (alpha == this.alpha && first == this.first && second == this.second) {
            return this;
        }
        return new LerpFunction(alpha, first, second);
    }

    @Override
    public Interval range() {
        return Interval.lerp(this.alpha.range(), this.first.range(), this.second.range());
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.alpha.domainAxes() | this.first.domainAxes() | this.second.domainAxes();
    }

    public MapCodec<LerpFunction> codec() {
        return CODEC;
    }

    public record ConstFirstSampler(DensitySampler alpha, float first, DensitySampler second) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.alpha.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer secondBuffer = context.acquireBuffer(volume);){
                this.second.sampleVolume(context, secondBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    float alpha = outputBuffer.get(i);
                    float second = secondBuffer.get(i);
                    if (alpha == 0.0f) {
                        outputBuffer.set(i, this.first);
                        continue;
                    }
                    if (alpha == 1.0f) {
                        outputBuffer.set(i, second);
                        continue;
                    }
                    outputBuffer.set(i, Mth.lerp(alpha, this.first, second));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float alpha = this.alpha.sampleValue(context, blockX, blockY, blockZ);
            if (alpha == 0.0f) {
                return this.first;
            }
            if (alpha == 1.0f) {
                return this.second.sampleValue(context, blockX, blockY, blockZ);
            }
            return Mth.lerp(alpha, this.first, this.second.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record ConstSecondSampler(DensitySampler alpha, DensitySampler first, float second) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.alpha.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer firstBuffer = context.acquireBuffer(volume);){
                this.first.sampleVolume(context, firstBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    float alpha = outputBuffer.get(i);
                    float first = firstBuffer.get(i);
                    if (alpha == 0.0f) {
                        outputBuffer.set(i, first);
                        continue;
                    }
                    if (alpha == 1.0f) {
                        outputBuffer.set(i, this.second);
                        continue;
                    }
                    outputBuffer.set(i, Mth.lerp(alpha, first, this.second));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float alpha = this.alpha.sampleValue(context, blockX, blockY, blockZ);
            if (alpha == 0.0f) {
                return this.first.sampleValue(context, blockX, blockY, blockZ);
            }
            if (alpha == 1.0f) {
                return this.second;
            }
            return Mth.lerp(alpha, this.first.sampleValue(context, blockX, blockY, blockZ), this.second);
        }
    }

    public record Sampler(DensitySampler alpha, DensitySampler first, DensitySampler second) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.alpha.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer firstBuffer = context.acquireBuffer(volume);){
                this.first.sampleVolume(context, firstBuffer, volume);
                try (ScopedDensityBuffer secondBuffer = context.acquireBuffer(volume);){
                    this.second.sampleVolume(context, secondBuffer, volume);
                    for (int i = 0; i < outputBuffer.size(); ++i) {
                        float alpha = outputBuffer.get(i);
                        float first = firstBuffer.get(i);
                        float second = secondBuffer.get(i);
                        if (alpha == 0.0f) {
                            outputBuffer.set(i, first);
                            continue;
                        }
                        if (alpha == 1.0f) {
                            outputBuffer.set(i, second);
                            continue;
                        }
                        outputBuffer.set(i, Mth.lerp(alpha, first, second));
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float alpha = this.alpha.sampleValue(context, blockX, blockY, blockZ);
            if (alpha == 0.0f) {
                return this.first.sampleValue(context, blockX, blockY, blockZ);
            }
            if (alpha == 1.0f) {
                return this.second.sampleValue(context, blockX, blockY, blockZ);
            }
            return Mth.lerp(alpha, this.first.sampleValue(context, blockX, blockY, blockZ), this.second.sampleValue(context, blockX, blockY, blockZ));
        }
    }
}


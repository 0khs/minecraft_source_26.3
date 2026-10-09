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
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.UnaryFunction;

public record PowFunction(DensityFunction base, DensityFunction exponent) implements DensityFunction
{
    public static final MapCodec<PowFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("base").forGetter(PowFunction::base), (App)DensityFunction.CODEC.fieldOf("exponent").forGetter(PowFunction::exponent)).apply((Applicative)i, PowFunction::new));

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        float exponentValue;
        float f;
        float f2;
        DensityFunction densityFunction;
        DensitySampler exponent;
        DensitySampler base;
        block4: {
            base = this.base.compileSampler(context);
            exponent = this.exponent.compileSampler(context);
            densityFunction = this.base;
            if (densityFunction instanceof ConstantFunction) {
                ConstantFunction constantFunction = (ConstantFunction)densityFunction;
                f = f2 = constantFunction.value();
                if (!true) break block4;
                float baseValue = f2;
                return new ConstBaseSampler(baseValue, exponent);
            }
        }
        if (!((densityFunction = this.exponent) instanceof ConstantFunction)) return new Sampler(base, exponent);
        ConstantFunction constantFunction = (ConstantFunction)densityFunction;
        try {
            f = f2 = constantFunction.value();
            if (!true) return new Sampler(base, exponent);
            exponentValue = f2;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        return PowFunction.compileConstExponent(base, exponentValue);
    }

    private static DensitySampler compileConstExponent(DensitySampler base, float exponent) {
        DensitySampler specialSampler;
        float absExponent = Math.abs(exponent);
        if (absExponent == 0.5f) {
            specialSampler = new UnaryFunction.SqrtSampler(base);
        } else if (absExponent == 1.0f) {
            specialSampler = base;
        } else if (absExponent == 2.0f) {
            specialSampler = new UnaryFunction.SquareSampler(base);
        } else if (absExponent == 3.0f) {
            specialSampler = new UnaryFunction.CubeSampler(base);
        } else {
            return new ConstExponentSampler(base, exponent);
        }
        return exponent >= 0.0f ? specialSampler : new UnaryFunction.ReciprocalSampler(specialSampler);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction base = rule.rewrite(this.base);
        DensityFunction exponent = rule.rewrite(this.exponent);
        if (base == this.base && exponent == this.exponent) {
            return this;
        }
        return new PowFunction(base, exponent);
    }

    @Override
    public Interval range() {
        return Interval.pow(this.base.range(), this.exponent.range());
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.base.domainAxes() | this.exponent.domainAxes();
    }

    public MapCodec<PowFunction> codec() {
        return CODEC;
    }

    private record ConstBaseSampler(double base, DensitySampler exponent) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.exponent.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, (float)Math.pow(this.base, outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (float)Math.pow(this.base, this.exponent.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    private record Sampler(DensitySampler base, DensitySampler exponent) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.base.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer exponentBuffer = context.acquireBuffer(volume);){
                this.exponent.sampleVolume(context, exponentBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    float base = outputBuffer.get(i);
                    float exponent = exponentBuffer.get(i);
                    outputBuffer.set(i, (float)Math.pow(base, exponent));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (float)Math.pow(this.base.sampleValue(context, blockX, blockY, blockZ), this.exponent.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    private record ConstExponentSampler(DensitySampler base, double exponent) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.base.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, (float)Math.pow(outputBuffer.get(i), this.exponent));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (float)Math.pow(this.base.sampleValue(context, blockX, blockY, blockZ), this.exponent);
        }
    }
}


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
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;

public record RangeChoiceFunction(DensityFunction input, float minInclusive, float maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction
{
    public static final MapCodec<RangeChoiceFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("input").forGetter(RangeChoiceFunction::input), (App)DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(RangeChoiceFunction::minInclusive), (App)DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(RangeChoiceFunction::maxExclusive), (App)DensityFunction.CODEC.fieldOf("when_in_range").forGetter(RangeChoiceFunction::whenInRange), (App)DensityFunction.CODEC.fieldOf("when_out_of_range").forGetter(RangeChoiceFunction::whenOutOfRange)).apply((Applicative)i, RangeChoiceFunction::new));

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        float whenInRangeValue;
        float f2;
        float f;
        DensitySampler input = this.input.compileSampler(context);
        DensityFunction densityFunction = this.whenInRange;
        if (!(densityFunction instanceof ConstantFunction)) return new Sampler(input, this.minInclusive, this.maxExclusive, this.whenInRange.compileSampler(context), this.whenOutOfRange.compileSampler(context));
        ConstantFunction constantFunction = (ConstantFunction)densityFunction;
        try {
            f2 = f = constantFunction.value();
            if (!true) return new Sampler(input, this.minInclusive, this.maxExclusive, this.whenInRange.compileSampler(context), this.whenOutOfRange.compileSampler(context));
            whenInRangeValue = f;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        densityFunction = this.whenOutOfRange;
        if (!(densityFunction instanceof ConstantFunction)) return new Sampler(input, this.minInclusive, this.maxExclusive, this.whenInRange.compileSampler(context), this.whenOutOfRange.compileSampler(context));
        ConstantFunction constantFunction2 = (ConstantFunction)densityFunction;
        f2 = f = constantFunction2.value();
        if (!true) return new Sampler(input, this.minInclusive, this.maxExclusive, this.whenInRange.compileSampler(context), this.whenOutOfRange.compileSampler(context));
        float whenOutOfRangeValue = f;
        return new ConstSampler(input, this.minInclusive, this.maxExclusive, whenInRangeValue, whenOutOfRangeValue);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        DensityFunction whenInRange = rule.rewrite(this.whenInRange);
        DensityFunction whenOutOfRange = rule.rewrite(this.whenOutOfRange);
        if (input == this.input && whenInRange == this.whenInRange && whenOutOfRange == this.whenOutOfRange) {
            return this;
        }
        return new RangeChoiceFunction(input, this.minInclusive, this.maxExclusive, whenInRange, whenOutOfRange);
    }

    @Override
    public Interval range() {
        return Interval.encapsulating(this.whenInRange.range(), this.whenOutOfRange.range());
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.input.domainAxes() | this.whenInRange.domainAxes() | this.whenOutOfRange.domainAxes();
    }

    public MapCodec<RangeChoiceFunction> codec() {
        return CODEC;
    }

    private record ConstSampler(DensitySampler input, float minInclusive, float maxExclusive, float whenInRange, float whenOutOfRange) implements DensitySampler
    {
        private float choose(float input) {
            return input >= this.minInclusive && input < this.maxExclusive ? this.whenInRange : this.whenOutOfRange;
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, this.choose(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.choose(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    private record Sampler(DensitySampler input, float minInclusive, float maxExclusive, DensitySampler whenInRange, DensitySampler whenOutOfRange) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.whenInRange.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer inputBuffer = context.acquireBuffer(volume);){
                this.input.sampleVolume(context, inputBuffer, volume);
                try (ScopedDensityBuffer whenOutOfRangeBuffer = context.acquireBuffer(volume);){
                    this.whenOutOfRange.sampleVolume(context, whenOutOfRangeBuffer, volume);
                    for (int i = 0; i < outputBuffer.size(); ++i) {
                        float input = inputBuffer.get(i);
                        if (input >= this.minInclusive && input < this.maxExclusive) continue;
                        outputBuffer.set(i, whenOutOfRangeBuffer.get(i));
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float inputValue = this.input.sampleValue(context, blockX, blockY, blockZ);
            if (inputValue >= this.minInclusive && inputValue < this.maxExclusive) {
                return this.whenInRange.sampleValue(context, blockX, blockY, blockZ);
            }
            return this.whenOutOfRange.sampleValue(context, blockX, blockY, blockZ);
        }
    }
}


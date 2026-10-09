/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.floats.FloatList
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.densityfunction.op;

import com.google.common.collect.Comparators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;

public record IntervalSelectFunction(DensityFunction input, FloatList thresholds, List<DensityFunction> functions) implements DensityFunction
{
    private static final Codec<FloatList> THRESHOLDS_CODEC = DensityFunctions.NOISE_VALUE_CODEC.listOf().xmap(FloatArrayList::new, Function.identity());
    public static final MapCodec<IntervalSelectFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("input").forGetter(IntervalSelectFunction::input), (App)THRESHOLDS_CODEC.fieldOf("thresholds").forGetter(IntervalSelectFunction::thresholds), (App)DensityFunction.CODEC.listOf(2, Integer.MAX_VALUE).fieldOf("functions").forGetter(IntervalSelectFunction::functions)).apply((Applicative)i, IntervalSelectFunction::new)).validate(IntervalSelectFunction::validate);

    private DataResult<IntervalSelectFunction> validate() {
        if (this.thresholds.size() != this.functions.size() - 1) {
            return DataResult.error(() -> "Expected " + (this.functions.size() - 1) + " thresholds for " + this.functions.size() + " functions, but got " + this.thresholds.size());
        }
        if (!Comparators.isInOrder((Iterable)this.thresholds, Float::compare)) {
            return DataResult.error(() -> "Threshold values must be ordered from smallest to largest");
        }
        return DataResult.success((Object)this);
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        DensitySampler input = this.input.compileSampler(context);
        if (this.thresholds.size() == 1) {
            float threshold = this.thresholds.getFloat(0);
            DensitySampler ifBelow = this.functions.getFirst().compileSampler(context);
            DensitySampler ifAbove = this.functions.getLast().compileSampler(context);
            return new SingleThresholdSampler(input, threshold, ifBelow, ifAbove);
        }
        return new Sampler(input, this.thresholds.toFloatArray(), (DensitySampler[])this.functions.stream().map(function -> function.compileSampler(context)).toArray(DensitySampler[]::new));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        MutableBoolean functionsChanged = new MutableBoolean();
        List<DensityFunction> functions = this.functions.stream().map(function -> {
            DensityFunction newFunction = rule.rewrite((DensityFunction)function);
            if (newFunction != function) {
                functionsChanged.setTrue();
            }
            return newFunction;
        }).toList();
        if (input == this.input && !functionsChanged.booleanValue()) {
            return this;
        }
        return new IntervalSelectFunction(input, this.thresholds, functions);
    }

    @Override
    public Interval range() {
        return Interval.encapsulating(Lists.transform(this.functions, DensityFunction::range));
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        int axes = this.input.domainAxes();
        for (DensityFunction function : this.functions) {
            axes |= function.domainAxes();
        }
        return axes;
    }

    public MapCodec<IntervalSelectFunction> codec() {
        return CODEC;
    }

    private record SingleThresholdSampler(DensitySampler input, float threshold, DensitySampler ifBelow, DensitySampler ifAbove) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer ifBelowBuffer = context.acquireBuffer(volume);){
                this.ifBelow.sampleVolume(context, ifBelowBuffer, volume);
                try (ScopedDensityBuffer ifAboveBuffer = context.acquireBuffer(volume);){
                    this.ifAbove.sampleVolume(context, ifAboveBuffer, volume);
                    for (int i = 0; i < outputBuffer.size(); ++i) {
                        float input = outputBuffer.get(i);
                        outputBuffer.set(i, input < this.threshold ? ifBelowBuffer.get(i) : ifAboveBuffer.get(i));
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float input = this.input.sampleValue(context, blockX, blockY, blockZ);
            if (input < this.threshold) {
                return this.ifBelow.sampleValue(context, blockX, blockY, blockZ);
            }
            return this.ifAbove.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    private record Sampler(DensitySampler input, float[] thresholds, DensitySampler[] samplers) implements DensitySampler
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            @Nullable ScopedDensityBuffer[] buffers = new ScopedDensityBuffer[this.samplers.length];
            try {
                int i;
                for (i = 0; i < this.samplers.length; ++i) {
                    ScopedDensityBuffer buffer;
                    buffers[i] = buffer = context.acquireBuffer(volume);
                    this.samplers[i].sampleVolume(context, buffer, volume);
                }
                for (i = 0; i < outputBuffer.size(); ++i) {
                    int samplerIndex = this.selectSamplerIndex(outputBuffer.get(i));
                    outputBuffer.set(i, buffers[samplerIndex].get(i));
                }
            }
            finally {
                for (ScopedDensityBuffer buffer : buffers) {
                    if (buffer == null) continue;
                    buffer.close();
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float input = this.input.sampleValue(context, blockX, blockY, blockZ);
            return this.samplers[this.selectSamplerIndex(input)].sampleValue(context, blockX, blockY, blockZ);
        }

        private int selectSamplerIndex(float input) {
            for (int i = 0; i < this.thresholds.length; ++i) {
                if (!(input < this.thresholds[i])) continue;
                return i;
            }
            return this.samplers.length - 1;
        }
    }
}


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

public record RoundFunction(Type type, DensityFunction input, DensityFunction multiple) implements DensityFunction
{
    private static float roundToInteger(float input, Type type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> (float)Math.floor(input);
            case 1 -> Math.round(input);
            case 2 -> (float)Math.ceil(input);
            case 3 -> input > 0.0f ? (float)Math.floor(input) : (float)Math.ceil(input);
        };
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        DensitySampler input = this.input.compileSampler(context);
        DensityFunction densityFunction = this.multiple;
        if (!(densityFunction instanceof ConstantFunction)) return new Sampler(this.type, input, this.multiple.compileSampler(context));
        ConstantFunction constantFunction = (ConstantFunction)densityFunction;
        try {
            float multipleValue;
            float f;
            float f2 = f = constantFunction.value();
            if (!true || (multipleValue = f) != 1.0f) return new Sampler(this.type, input, this.multiple.compileSampler(context));
            return new IntegerMultipleSampler(this.type, input);
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        DensityFunction multiple = rule.rewrite(this.multiple);
        if (input == this.input && multiple == this.multiple) {
            return this;
        }
        return new RoundFunction(this.type, input, multiple);
    }

    @Override
    public Interval range() {
        Interval multipleRange = this.multiple.range();
        return Interval.mul(Interval.mapMonotonic(Interval.div(this.input.range(), multipleRange), value -> RoundFunction.roundToInteger(value, this.type)), multipleRange);
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.input.domainAxes() | this.multiple.domainAxes();
    }

    public MapCodec<RoundFunction> codec() {
        return this.type.codec;
    }

    public static enum Type {
        FLOOR("floor"),
        ROUND("round"),
        CEIL("ceil"),
        TRUNCATE("truncate");

        public final String id;
        public final MapCodec<RoundFunction> codec = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("input").forGetter(RoundFunction::input), (App)DensityFunction.CODEC.optionalFieldOf("multiple", (Object)DensityFunctions.constant(1.0f)).forGetter(RoundFunction::multiple)).apply((Applicative)i, (input, multiple) -> new RoundFunction(this, (DensityFunction)input, (DensityFunction)multiple)));

        private Type(String id) {
            this.id = id;
        }
    }

    private record IntegerMultipleSampler(Type type, DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, RoundFunction.roundToInteger(outputBuffer.get(i), this.type));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float input = this.input.sampleValue(context, blockX, blockY, blockZ);
            return RoundFunction.roundToInteger(input, this.type);
        }
    }

    private record Sampler(Type type, DensitySampler input, DensitySampler multiple) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer multipleBuffer = context.acquireBuffer(volume);){
                this.multiple.sampleVolume(context, multipleBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    float input = outputBuffer.get(i);
                    float multiple = multipleBuffer.get(i);
                    outputBuffer.set(i, this.apply(input, multiple));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float input = this.input.sampleValue(context, blockX, blockY, blockZ);
            float multiple = this.multiple.sampleValue(context, blockX, blockY, blockZ);
            return this.apply(input, multiple);
        }

        private float apply(float input, float multiple) {
            if (multiple == 0.0f) {
                return input;
            }
            return RoundFunction.roundToInteger(input / multiple, this.type) * multiple;
        }
    }
}


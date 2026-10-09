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

public record UnaryFunction(Type type, DensityFunction input) implements DensityFunction
{
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        DensitySampler input = this.input.compileSampler(context);
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new AbsSampler(input);
            case 1 -> new SquareSampler(input);
            case 2 -> new CubeSampler(input);
            case 3 -> new SqrtSampler(input);
            case 4 -> new LeakyReLUSampler(input, 0.5f);
            case 5 -> new LeakyReLUSampler(input, 0.25f);
            case 6 -> new ReciprocalSampler(input);
            case 7 -> new NegateSampler(input);
            case 8 -> new SqueezeSampler(input);
            case 9 -> new LogSampler(input);
            case 10 -> new SignSampler(input);
        };
    }

    @Override
    public UnaryFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        if (input == this.input) {
            return this;
        }
        return new UnaryFunction(this.type, input);
    }

    public MapCodec<UnaryFunction> codec() {
        return this.type.codec;
    }

    @Override
    public Interval range() {
        Interval input = this.input.range();
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Interval.abs(input);
            case 1 -> Interval.square(input);
            case 3 -> Interval.pow(input, Interval.ofExact(0.5f));
            case 6 -> Interval.reciprocal(input);
            case 7 -> Interval.sub(Interval.ofExact(0.0f), input);
            case 2 -> Interval.mapMonotonic(input, Mth::cube);
            case 4 -> Interval.mapMonotonic(input, value -> LeakyReLUSampler.apply(0.5f, value));
            case 5 -> Interval.mapMonotonic(input, value -> LeakyReLUSampler.apply(0.25f, value));
            case 8 -> Interval.mapMonotonic(input, SqueezeSampler::apply);
            case 9 -> Interval.log(input);
            case 10 -> Interval.sign(input);
        };
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.input.domainAxes();
    }

    public static enum Type {
        ABS("abs"),
        SQUARE("square"),
        CUBE("cube"),
        SQRT("sqrt"),
        HALF_NEGATIVE("half_negative"),
        QUARTER_NEGATIVE("quarter_negative"),
        RECIPROCAL("reciprocal"),
        NEGATE("negate"),
        SQUEEZE("squeeze"),
        LOG("log"),
        SIGN("sign");

        public final String id;
        public final MapCodec<UnaryFunction> codec = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("input").forGetter(UnaryFunction::input)).apply((Applicative)i, input -> new UnaryFunction(this, (DensityFunction)input)));

        private Type(String id) {
            this.id = id;
        }
    }

    public record AbsSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, Math.abs(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Math.abs(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record SquareSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, Mth.square(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Mth.square(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record CubeSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, Mth.cube(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Mth.cube(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record SqrtSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, Mth.sqrt(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Mth.sqrt(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record LeakyReLUSampler(DensitySampler input, float negativeFactor) implements DensitySampler
    {
        private static float apply(float negativeFactor, float input) {
            return input > 0.0f ? input : input * negativeFactor;
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, LeakyReLUSampler.apply(this.negativeFactor, outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return LeakyReLUSampler.apply(this.negativeFactor, this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record ReciprocalSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, 1.0f / outputBuffer.get(i));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return 1.0f / this.input.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record NegateSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, -outputBuffer.get(i));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return -this.input.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record SqueezeSampler(DensitySampler input) implements DensitySampler
    {
        private static float apply(float input) {
            float clampedInput = Mth.clamp(input, -1.0f, 1.0f);
            return clampedInput / 2.0f - Mth.cube(clampedInput) / 24.0f;
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, SqueezeSampler.apply(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return SqueezeSampler.apply(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record LogSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, (float)Math.log(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (float)Math.log(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record SignSampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, Math.signum(outputBuffer.get(i)));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Math.signum(this.input.sampleValue(context, blockX, blockY, blockZ));
        }
    }
}


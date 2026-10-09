/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Interval;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.generator.NoiseFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.BinaryFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.ClampFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.LerpFunction;
import net.minecraft.world.level.levelgen.synth.NoiseStack;
import net.minecraft.world.level.levelgen.synth.SmearedPerlinNoise;

public record BlendedNoise(double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) implements DensityFunction
{
    private static final Codec<Double> SCALE_RANGE = Codec.doubleRange((double)0.001, (double)1000.0);
    public static final MapCodec<BlendedNoise> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)SCALE_RANGE.fieldOf("xz_scale").forGetter(BlendedNoise::xzScale), (App)SCALE_RANGE.fieldOf("y_scale").forGetter(BlendedNoise::yScale), (App)SCALE_RANGE.fieldOf("xz_factor").forGetter(BlendedNoise::xzFactor), (App)SCALE_RANGE.fieldOf("y_factor").forGetter(BlendedNoise::yFactor), (App)Codec.doubleRange((double)1.0, (double)8.0).fieldOf("smear_scale_multiplier").forGetter(BlendedNoise::smearScaleMultiplier)).apply((Applicative)i, BlendedNoise::new));
    private static final double BASE_SCALE = 684.412;
    private static final double LIMIT_FACTOR = 0.9999847412109375;
    private static final double MAIN_FACTOR = 12.75;
    private static final int LIMIT_FIRST_OCTAVE = -15;
    private static final int MAIN_FIRST_OCTAVE = -7;
    public static final Identifier NOISE_SEED = Identifier.withDefaultNamespace("terrain");

    private double xzMultiplier() {
        return 684.412 * this.xzScale;
    }

    private double yMultiplier() {
        return 684.412 * this.yScale;
    }

    public FbmSet createFbmSet(RandomSource random) {
        double limitSmearScaleY = this.yMultiplier() * this.smearScaleMultiplier;
        double mainSmearScaleY = limitSmearScaleY / this.yFactor;
        return new FbmSet(BlendedNoise.createFbm(random, -15, limitSmearScaleY, 0.9999847412109375), BlendedNoise.createFbm(random, -15, limitSmearScaleY, 0.9999847412109375), BlendedNoise.createFbm(random, -7, mainSmearScaleY, 12.75));
    }

    @VisibleForTesting
    public static NoiseStack createFbm(RandomSource random, int firstOctave, double smearScaleY, double valueFactor) {
        if (firstOctave > 0) {
            throw new IllegalArgumentException("firstOctave>0");
        }
        int octaves = -firstOctave + 1;
        double factor = 1.0;
        valueFactor /= Math.pow(2.0, octaves) - 1.0;
        NoiseStack.Builder stack = NoiseStack.builder();
        for (int i = octaves - 1; i >= 0; --i) {
            stack.add(new SmearedPerlinNoise(random, smearScaleY * factor), factor, (float)valueFactor);
            factor /= 2.0;
            valueFactor *= 2.0;
        }
        return stack.build();
    }

    private static Interval computeFbmRange(int firstOctave, double smearScaleY, double valueFactor) {
        int octaves = -firstOctave + 1;
        double factor = 1.0;
        valueFactor /= Math.pow(2.0, octaves) - 1.0;
        Interval range = Interval.ofExact(0.0f);
        for (int i = octaves - 1; i >= 0; --i) {
            Interval layerRange = Interval.mul(SmearedPerlinNoise.range(smearScaleY * factor), Interval.ofExact((float)valueFactor));
            range = Interval.add(range, layerRange);
            factor /= 2.0;
            valueFactor *= 2.0;
        }
        return range;
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        return this.compileSampler(context.createRandom(NOISE_SEED));
    }

    @VisibleForTesting
    public DensitySampler compileSampler(RandomSource random) {
        FbmSet fbms = this.createFbmSet(random);
        double xzMultiplier = this.xzMultiplier();
        double yMultiplier = this.yMultiplier();
        NoiseFunction.Sampler minLimitNoise = new NoiseFunction.Sampler(fbms.minLimitNoise(), xzMultiplier, yMultiplier);
        NoiseFunction.Sampler maxLimitNoise = new NoiseFunction.Sampler(fbms.maxLimitNoise(), xzMultiplier, yMultiplier);
        NoiseFunction.Sampler mainNoise = new NoiseFunction.Sampler(fbms.mainNoise(), xzMultiplier / this.xzFactor, yMultiplier / this.yFactor);
        ClampFunction.Sampler choice = new ClampFunction.Sampler(new BinaryFunction.ConstAddSampler(mainNoise, 0.5f), 0.0f, 1.0f);
        return new LerpFunction.Sampler(choice, minLimitNoise, maxLimitNoise);
    }

    @Override
    public Interval range() {
        return BlendedNoise.computeFbmRange(-15, this.yMultiplier() * this.smearScaleMultiplier, 0.9999847412109375);
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return 7;
    }

    public MapCodec<BlendedNoise> codec() {
        return CODEC;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    public record FbmSet(NoiseStack minLimitNoise, NoiseStack maxLimitNoise, NoiseStack mainNoise) {
        @VisibleForTesting
        public void parityConfigString(StringBuilder sb) {
            sb.append("BlendedNoise{minLimitNoise=");
            this.parityConfigString(sb, this.minLimitNoise);
            sb.append(", maxLimitNoise=");
            this.parityConfigString(sb, this.maxLimitNoise);
            sb.append(", mainNoise=");
            this.parityConfigString(sb, this.mainNoise);
            sb.append(String.format(Locale.ROOT, ", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=4, cellHeight=8", 684.412, 684.412, 8.555150000000001, 4.277575000000001)).append('}');
        }

        private void parityConfigString(StringBuilder output, NoiseStack noise) {
            int octaves = noise.layers.length;
            output.append("PerlinNoise{");
            List<String> amplitudeStrings = IntStream.range(0, octaves).mapToObj(n -> "1.00").toList();
            output.append("first octave: ").append(-octaves + 1).append(", amplitudes: ").append(amplitudeStrings).append(", noise levels: [");
            for (int i = 0; i < octaves; ++i) {
                output.append(i).append(": ");
                ((SmearedPerlinNoise)noise.getLayer(octaves - 1 - i)).parityConfigString(output);
                output.append(", ");
            }
            output.append("]");
            output.append("}");
        }
    }
}


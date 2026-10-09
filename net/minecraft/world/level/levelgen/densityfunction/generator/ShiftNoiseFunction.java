/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.densityfunction.generator;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.generator.NoiseFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.BinaryFunction;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface ShiftNoiseFunction
extends DensityFunction {
    public static final double COORDINATE_FACTOR = 0.25;
    public static final float VALUE_FACTOR = 4.0f;

    public Holder<NormalNoise> offsetNoise();

    @Override
    default public Interval range() {
        return Interval.mul(this.offsetNoise().value().range(), Interval.ofExact(4.0f));
    }

    public MapCodec<? extends ShiftNoiseFunction> codec();

    public record ShiftB(Holder<NormalNoise> offsetNoise) implements ShiftNoiseFunction
    {
        public static final MapCodec<ShiftB> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)NormalNoise.CODEC.fieldOf("noise").forGetter(ShiftB::offsetNoise)).apply((Applicative)i, ShiftB::new));

        @Override
        public DensitySampler compileSampler(DensityFunction.CompileContext context) {
            final Noise noise = context.createNoiseSampler(this.offsetNoise);
            return new DensitySampler(){
                {
                    Objects.requireNonNull(this$0);
                }

                @Override
                public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
                    DensityVolume transposedVolume = new DensityVolume(volume.sizeZ(), volume.sizeX(), 1, volume.minBlockZ(), volume.minBlockX(), 0, volume.stepBlockZ(), volume.stepBlockX(), 1);
                    try (ScopedDensityBuffer transposedBuffer = context.acquireBuffer(transposedVolume);){
                        transposedBuffer.fill(0.0f);
                        noise.addToVolume(transposedBuffer, transposedVolume, 0.25, 0.25, 4.0f);
                        for (int z = 0; z < volume.sizeZ(); ++z) {
                            for (int x = 0; x < volume.sizeX(); ++x) {
                                float value = transposedBuffer.get(transposedVolume.indexUnchecked(z, x, 0));
                                outputBuffer.setRange(volume.indexUnchecked(x, 0, z), volume.sizeY(), value);
                            }
                        }
                    }
                }

                @Override
                public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
                    return noise.get((double)blockZ * 0.25, (double)blockX * 0.25, 0.0) * 4.0f;
                }
            };
        }

        @Override
        public DensityFunction rewriteChildren(DfRewriteRule rule) {
            return new ShiftB(this.offsetNoise);
        }

        @Override
        public @DensityFunction.Axes int domainAxes() {
            return 5;
        }

        public MapCodec<ShiftB> codec() {
            return CODEC;
        }
    }

    public record ShiftA(Holder<NormalNoise> offsetNoise) implements ShiftNoiseFunction
    {
        public static final MapCodec<ShiftA> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)NormalNoise.CODEC.fieldOf("noise").forGetter(ShiftA::offsetNoise)).apply((Applicative)i, ShiftA::new));

        @Override
        public DensitySampler compileSampler(DensityFunction.CompileContext context) {
            Noise noise = context.createNoiseSampler(this.offsetNoise);
            return new BinaryFunction.ConstMulSampler(new NoiseFunction.Sampler(noise, 0.25, 0.0), 4.0f);
        }

        @Override
        public DensityFunction rewriteChildren(DfRewriteRule rule) {
            return new ShiftA(this.offsetNoise);
        }

        @Override
        public @DensityFunction.Axes int domainAxes() {
            return 5;
        }

        public MapCodec<ShiftA> codec() {
            return CODEC;
        }
    }

    public record Shift(Holder<NormalNoise> offsetNoise) implements ShiftNoiseFunction
    {
        public static final MapCodec<Shift> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)NormalNoise.CODEC.fieldOf("noise").forGetter(Shift::offsetNoise)).apply((Applicative)i, Shift::new));

        @Override
        public DensitySampler compileSampler(DensityFunction.CompileContext context) {
            Noise noise = context.createNoiseSampler(this.offsetNoise);
            return new BinaryFunction.ConstMulSampler(new NoiseFunction.Sampler(noise, 0.25, 0.25), 4.0f);
        }

        @Override
        public DensityFunction rewriteChildren(DfRewriteRule rule) {
            return new Shift(this.offsetNoise);
        }

        @Override
        public @DensityFunction.Axes int domainAxes() {
            return 7;
        }

        public MapCodec<Shift> codec() {
            return CODEC;
        }
    }
}


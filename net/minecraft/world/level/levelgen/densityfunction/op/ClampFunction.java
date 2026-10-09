/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.densityfunction.op;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record ClampFunction(DensityFunction input, float min, float max) implements DensityFunction
{
    public static final MapCodec<ClampFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("input").forGetter(ClampFunction::input), (App)DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(ClampFunction::min), (App)DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(ClampFunction::max)).apply((Applicative)i, ClampFunction::new)).validate(ClampFunction::validate);

    private static DataResult<ClampFunction> validate(ClampFunction clamp) {
        if (clamp.max < clamp.min) {
            return DataResult.error(() -> "min (" + clamp.min + ") must be less than or equal to max (" + clamp.max + ")");
        }
        return DataResult.success((Object)clamp);
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        return new Sampler(this.input.compileSampler(context), this.min, this.max);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        if (input == this.input) {
            return this;
        }
        return new ClampFunction(input, this.min, this.max);
    }

    public MapCodec<ClampFunction> codec() {
        return CODEC;
    }

    @Override
    public Interval range() {
        return Interval.clamp(this.input.range(), this.min, this.max);
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.input.domainAxes();
    }

    public record Sampler(DensitySampler input, float min, float max) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, Mth.clamp(outputBuffer.get(i), this.min, this.max));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Mth.clamp(this.input.sampleValue(context, blockX, blockY, blockZ), this.min, this.max);
        }
    }
}


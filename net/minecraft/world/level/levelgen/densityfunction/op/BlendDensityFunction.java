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
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record BlendDensityFunction(DensityFunction input) implements DensityFunction
{
    public static final MapCodec<BlendDensityFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("input").forGetter(BlendDensityFunction::input)).apply((Applicative)i, BlendDensityFunction::new));

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        return new Sampler(this.input.compileSampler(context));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        if (input == this.input) {
            return this;
        }
        return new BlendDensityFunction(input);
    }

    @Override
    public Interval range() {
        return this.input.range();
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.input.domainAxes();
    }

    public MapCodec<BlendDensityFunction> codec() {
        return CODEC;
    }

    private record Sampler(DensitySampler input) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.input.sampleVolume(context, outputBuffer, volume);
            Blender blender = context.getField(Blender.CONTEXT_KEY);
            if (blender == null || blender.isEmpty()) {
                return;
            }
            int index = 0;
            for (int z = 0; z < volume.sizeZ(); ++z) {
                int blockZ = volume.blockZ(z);
                for (int x = 0; x < volume.sizeX(); ++x) {
                    int blockX = volume.blockX(x);
                    for (int y = 0; y < volume.sizeY(); ++y) {
                        int blockY = volume.blockY(y);
                        outputBuffer.set(index, blender.blendDensity(blockX, blockY, blockZ, outputBuffer.get(index)));
                        ++index;
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float input = this.input.sampleValue(context, blockX, blockY, blockZ);
            Blender blender = context.getField(Blender.CONTEXT_KEY);
            if (blender == null || blender.isEmpty()) {
                return input;
            }
            return blender.blendDensity(blockX, blockY, blockZ, input);
        }
    }
}


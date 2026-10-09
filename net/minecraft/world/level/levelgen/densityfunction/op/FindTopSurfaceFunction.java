/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.densityfunction.op;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.op.SliceFunction;

public record FindTopSurfaceFunction(DensityFunction density, DensityFunction upperBound, int lowerBound, int cellHeight) implements DensityFunction
{
    public static final MapCodec<FindTopSurfaceFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("density").forGetter(FindTopSurfaceFunction::density), (App)DensityFunction.CODEC.fieldOf("upper_bound").forGetter(FindTopSurfaceFunction::upperBound), (App)Codec.intRange((int)(DimensionType.MIN_Y * 2), (int)(DimensionType.MAX_Y * 2)).fieldOf("lower_bound").forGetter(FindTopSurfaceFunction::lowerBound), (App)ExtraCodecs.POSITIVE_INT.fieldOf("cell_height").forGetter(FindTopSurfaceFunction::cellHeight)).apply((Applicative)i, FindTopSurfaceFunction::new));

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        Sampler sampler = new Sampler(this.density.compileSampler(context), this.upperBound.compileSampler(context), this.lowerBound, this.cellHeight);
        return new SliceFunction.YSampler(sampler, 0);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction density = rule.rewrite(this.density);
        DensityFunction upperBound = rule.rewrite(this.upperBound);
        if (density == this.density && upperBound == this.upperBound) {
            return this;
        }
        return new FindTopSurfaceFunction(density, upperBound, this.lowerBound, this.cellHeight);
    }

    @Override
    public Interval range() {
        return Interval.of(this.lowerBound, Math.max((float)this.lowerBound, this.upperBound.range().max()));
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return (this.density.domainAxes() | this.upperBound.domainAxes()) & 0xFFFFFFFD;
    }

    public MapCodec<FindTopSurfaceFunction> codec() {
        return CODEC;
    }

    private record Sampler(DensitySampler density, DensitySampler upperBound, int lowerBound, int cellHeight) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            if (volume.sizeY() != 1) {
                throw new IllegalArgumentException("Cannot sample with sizeY=" + volume.sizeY());
            }
            this.upperBound.sampleVolume(context, outputBuffer, volume);
            int index = 0;
            for (int z = 0; z < volume.sizeZ(); ++z) {
                int blockZ = volume.blockZ(z);
                for (int x = 0; x < volume.sizeX(); ++x) {
                    int blockX = volume.blockX(x);
                    float upperBound = outputBuffer.get(index);
                    outputBuffer.set(index, this.findSurfaceFrom(context, blockX, blockZ, upperBound));
                    ++index;
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float upperBound = this.upperBound.sampleValue(context, blockX, blockY, blockZ);
            return this.findSurfaceFrom(context, blockX, blockZ, upperBound);
        }

        private int findSurfaceFrom(SamplerContext context, int x, int z, float upperBound) {
            int topY = Mth.floor(upperBound / (float)this.cellHeight) * this.cellHeight;
            if (topY <= this.lowerBound) {
                return this.lowerBound;
            }
            for (int probeY = topY; probeY >= this.lowerBound; probeY -= this.cellHeight) {
                if (!(this.density.sampleValue(context, x, probeY, z) > 0.0f)) continue;
                return probeY;
            }
            return this.lowerBound;
        }
    }
}


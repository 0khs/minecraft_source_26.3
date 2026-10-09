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
import net.minecraft.core.Direction;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;

public record SliceFunction(Direction.Axis axis, int coordinate, DensityFunction input) implements DensityFunction
{
    public static final MapCodec<SliceFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Direction.Axis.CODEC.fieldOf("axis").forGetter(SliceFunction::axis), (App)Codec.INT.fieldOf("coordinate").forGetter(SliceFunction::coordinate), (App)DensityFunction.CODEC.fieldOf("input").forGetter(SliceFunction::input)).apply((Applicative)i, SliceFunction::new));

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        Record record;
        DensityFunction densityFunction = this.input;
        if (densityFunction instanceof SliceFunction) {
            SliceFunction sliceFunction = (SliceFunction)densityFunction;
            try {
                DensityFunction densityFunction2;
                int n;
                Direction.Axis axis;
                Direction.Axis innerAxis = axis = sliceFunction.axis();
                int n2 = n = sliceFunction.coordinate();
                int innerCoordinate = n;
                DensityFunction innerInput = densityFunction2 = sliceFunction.input();
                if (this.axis == Direction.Axis.X && innerAxis == Direction.Axis.Z || this.axis == Direction.Axis.Z && innerAxis == Direction.Axis.X) {
                    int z;
                    int x;
                    if (this.axis == Direction.Axis.X) {
                        x = this.coordinate;
                        z = innerCoordinate;
                        return new XzSampler(innerInput.compileSampler(context), x, z);
                    }
                    x = innerCoordinate;
                    z = this.coordinate;
                    return new XzSampler(innerInput.compileSampler(context), x, z);
                }
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        }
        DensitySampler input = this.input.compileSampler(context);
        switch (this.axis) {
            default: {
                throw new MatchException(null, null);
            }
            case X: {
                record = new XSampler(input, this.coordinate);
                return record;
            }
            case Y: {
                record = new YSampler(input, this.coordinate);
                return record;
            }
            case Z: 
        }
        record = new ZSampler(input, this.coordinate);
        return record;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction input = rule.rewrite(this.input);
        if (input == this.input) {
            return this;
        }
        return new SliceFunction(this.axis, this.coordinate, input);
    }

    @Override
    public Interval range() {
        return this.input.range();
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.input.domainAxes() & ~DensityFunction.axesFrom(this.axis);
    }

    public MapCodec<SliceFunction> codec() {
        return CODEC;
    }

    public record XzSampler(DensitySampler input, int x, int z) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            if (volume.sizeX() == 1 && volume.sizeZ() == 1 && volume.minBlockX() == this.x && volume.minBlockZ() == this.z) {
                this.input.sampleVolume(context, outputBuffer, volume);
                return;
            }
            DensityVolume inputVolume = new DensityVolume(1, volume.sizeY(), 1, this.x, volume.minBlockY(), this.z, volume.stepBlockX(), volume.stepBlockY(), volume.stepBlockZ());
            try (ScopedDensityBuffer inputBuffer = context.acquireBuffer(inputVolume);){
                this.input.sampleVolume(context, inputBuffer, inputVolume);
                for (int y = 0; y < volume.sizeY(); ++y) {
                    float input = inputBuffer.get(inputVolume.indexUnchecked(0, y, 0));
                    int index = volume.indexUnchecked(0, y, 0);
                    for (int z = 0; z < volume.sizeZ(); ++z) {
                        for (int x = 0; x < volume.sizeX(); ++x) {
                            outputBuffer.set(index, input);
                            index += volume.sizeY();
                        }
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.input.sampleValue(context, this.x, blockY, this.z);
        }
    }

    public record XSampler(DensitySampler input, int x) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            if (volume.sizeX() == 1 && volume.minBlockX() == this.x) {
                this.input.sampleVolume(context, outputBuffer, volume);
                return;
            }
            DensityVolume inputVolume = new DensityVolume(1, volume.sizeY(), volume.sizeZ(), this.x, volume.minBlockY(), volume.minBlockZ(), volume.stepBlockX(), volume.stepBlockY(), volume.stepBlockZ());
            try (ScopedDensityBuffer inputBuffer = context.acquireBuffer(inputVolume);){
                this.input.sampleVolume(context, inputBuffer, inputVolume);
                int index = 0;
                for (int z = 0; z < volume.sizeZ(); ++z) {
                    for (int x = 0; x < volume.sizeX(); ++x) {
                        for (int y = 0; y < volume.sizeY(); ++y) {
                            outputBuffer.set(index, inputBuffer.get(inputVolume.indexUnchecked(0, y, z)));
                            ++index;
                        }
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.input.sampleValue(context, this.x, blockY, blockZ);
        }
    }

    public record YSampler(DensitySampler input, int y) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            if (volume.sizeY() == 1 && volume.minBlockY() == this.y) {
                this.input.sampleVolume(context, outputBuffer, volume);
                return;
            }
            DensityVolume inputVolume = new DensityVolume(volume.sizeX(), 1, volume.sizeZ(), volume.minBlockX(), this.y, volume.minBlockZ(), volume.stepBlockX(), volume.stepBlockY(), volume.stepBlockZ());
            try (ScopedDensityBuffer inputBuffer = context.acquireBuffer(inputVolume);){
                this.input.sampleVolume(context, inputBuffer, inputVolume);
                for (int z = 0; z < volume.sizeZ(); ++z) {
                    for (int x = 0; x < volume.sizeX(); ++x) {
                        float input = inputBuffer.get(inputVolume.indexUnchecked(x, 0, z));
                        outputBuffer.setRange(volume.indexUnchecked(x, 0, z), volume.sizeY(), input);
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.input.sampleValue(context, blockX, this.y, blockZ);
        }
    }

    public record ZSampler(DensitySampler input, int z) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            if (volume.sizeZ() == 1 && volume.minBlockZ() == this.z) {
                this.input.sampleVolume(context, outputBuffer, volume);
                return;
            }
            DensityVolume inputVolume = new DensityVolume(volume.sizeX(), volume.sizeY(), 1, volume.minBlockX(), volume.minBlockY(), this.z, volume.stepBlockX(), volume.stepBlockY(), volume.stepBlockZ());
            try (ScopedDensityBuffer inputBuffer = context.acquireBuffer(inputVolume);){
                this.input.sampleVolume(context, inputBuffer, inputVolume);
                int index = 0;
                for (int z = 0; z < volume.sizeZ(); ++z) {
                    for (int x = 0; x < volume.sizeX(); ++x) {
                        for (int y = 0; y < volume.sizeY(); ++y) {
                            outputBuffer.set(index, inputBuffer.get(inputVolume.indexUnchecked(x, y, 0)));
                            ++index;
                        }
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.input.sampleValue(context, blockX, blockY, this.z);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.densityfunction.generator;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.TilingMode;

public record GradientFunction(Direction.Axis axis, TilingMode tiling, int fromCoordinate, int toCoordinate, float fromValue, float toValue) implements DensityFunction
{
    public static final MapCodec<GradientFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Direction.Axis.CODEC.fieldOf("axis").forGetter(GradientFunction::axis), (App)TilingMode.CODEC.optionalFieldOf("tiling", (Object)TilingMode.CLAMP_TO_EDGE).forGetter(GradientFunction::tiling), (App)Codec.INT.fieldOf("from_coordinate").forGetter(GradientFunction::fromCoordinate), (App)Codec.INT.fieldOf("to_coordinate").forGetter(GradientFunction::toCoordinate), (App)DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(GradientFunction::fromValue), (App)DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(GradientFunction::toValue)).apply((Applicative)i, GradientFunction::new)).validate(GradientFunction::validate);

    private static DataResult<GradientFunction> validate(GradientFunction gradient) {
        if (gradient.fromCoordinate == gradient.toCoordinate) {
            return DataResult.error(() -> "from_coordinate cannot be equal to to_coordinate");
        }
        return DataResult.success((Object)gradient);
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        int coordinateRange = this.toCoordinate - this.fromCoordinate;
        float coordinateFactor = (this.toValue - this.fromValue) / (float)coordinateRange;
        return switch (this.tiling) {
            default -> throw new MatchException(null, null);
            case TilingMode.CLAMP_TO_EDGE -> {
                int minCoordinate = Math.min(this.fromCoordinate, this.toCoordinate);
                int maxCoordinate = Math.max(this.fromCoordinate, this.toCoordinate);
                yield new ClampedSampler(this.axis, this.fromCoordinate, minCoordinate, maxCoordinate, this.fromValue, coordinateFactor);
            }
            case TilingMode.REPEAT -> new RepeatSampler(this.axis, this.fromCoordinate, coordinateRange, this.fromValue, coordinateFactor);
            case TilingMode.MIRRORED_REPEAT -> new MirroredRepeatSampler(this.axis, this.fromCoordinate, coordinateRange, this.fromValue, coordinateFactor);
        };
    }

    @Override
    public Interval range() {
        return Interval.encapsulating(this.fromValue, this.toValue);
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return DensityFunction.axesFrom(this.axis);
    }

    public MapCodec<GradientFunction> codec() {
        return CODEC;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    private record ClampedSampler(Direction.Axis axis, int fromCoordinate, int minCoordinate, int maxCoordinate, float fromValue, float coordinateFactor) implements GradientSampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            GradientSampler.sampleVolume(this, outputBuffer, volume);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.compute(this.axis.choose(blockX, blockY, blockZ));
        }

        @Override
        public float compute(int coordinate) {
            int relativeCoordinate = Mth.clamp(coordinate, this.minCoordinate, this.maxCoordinate) - this.fromCoordinate;
            return this.fromValue + (float)relativeCoordinate * this.coordinateFactor;
        }
    }

    private record RepeatSampler(Direction.Axis axis, int fromCoordinate, int coordinateRange, float fromValue, float coordinateFactor) implements GradientSampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            GradientSampler.sampleVolume(this, outputBuffer, volume);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.compute(this.axis.choose(blockX, blockY, blockZ));
        }

        @Override
        public float compute(int coordinate) {
            int relativeCoordinate = coordinate - this.fromCoordinate;
            return this.fromValue + (float)Math.floorMod(relativeCoordinate, this.coordinateRange) * this.coordinateFactor;
        }
    }

    private record MirroredRepeatSampler(Direction.Axis axis, int fromCoordinate, int coordinateRange, float fromValue, float coordinateFactor) implements GradientSampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            GradientSampler.sampleVolume(this, outputBuffer, volume);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.compute(this.axis.choose(blockX, blockY, blockZ));
        }

        @Override
        public float compute(int coordinate) {
            int relativeCoordinate = coordinate - this.fromCoordinate;
            int tileIndex = Math.floorDiv(relativeCoordinate, this.coordinateRange);
            int localCoordinate = relativeCoordinate - tileIndex * this.coordinateRange;
            if ((tileIndex & 1) == 0) {
                return this.fromValue + (float)localCoordinate * this.coordinateFactor;
            }
            return this.fromValue + (float)(this.coordinateRange - localCoordinate) * this.coordinateFactor;
        }
    }

    private static interface GradientSampler
    extends DensitySampler {
        public static void sampleVolume(GradientSampler sampler, DensityBuffer output, DensityVolume volume) {
            switch (sampler.axis()) {
                case X: {
                    for (int x = 0; x < volume.sizeX(); ++x) {
                        float value = sampler.compute(volume.blockX(x));
                        for (int z = 0; z < volume.sizeZ(); ++z) {
                            output.setRange(volume.indexUnchecked(x, 0, z), volume.sizeY(), value);
                        }
                    }
                    break;
                }
                case Y: {
                    for (int y = 0; y < volume.sizeY(); ++y) {
                        float value = sampler.compute(volume.blockY(y));
                        for (int z = 0; z < volume.sizeZ(); ++z) {
                            for (int x = 0; x < volume.sizeX(); ++x) {
                                output.set(volume.indexUnchecked(x, y, z), value);
                            }
                        }
                    }
                    break;
                }
                case Z: {
                    for (int z = 0; z < volume.sizeZ(); ++z) {
                        float value = sampler.compute(volume.blockZ(z));
                        output.setRange(volume.indexUnchecked(0, 0, z), volume.sizeX() * volume.sizeY(), value);
                    }
                    break;
                }
            }
        }

        public Direction.Axis axis();

        public float compute(int var1);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.floats.FloatList
 */
package net.minecraft.world.level.levelgen.densityfunction;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.DistanceMetric;
import net.minecraft.world.level.levelgen.densityfunction.TilingMode;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.DistanceToPointFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.EndIslandFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.GradientFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.NoiseFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.ShiftNoiseFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.SimpleDensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.BinaryFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.BlendDensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.CacheFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.ClampFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.FindTopSurfaceFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.InterpolatedFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.IntervalSelectFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.LerpFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.PowFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.RangeChoiceFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.RoundFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.SliceFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.SplineFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.UnaryFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class DensityFunctions {
    public static final float MAX_REASONABLE_NOISE_VALUE = 1000000.0f;
    public static final Codec<Float> NOISE_VALUE_CODEC = Codec.floatRange((float)-1000000.0f, (float)1000000.0f);
    private static final Codec<DensityFunction> FULL_DIRECT_CODEC = BuiltInRegistries.DENSITY_FUNCTION_TYPE.byNameCodec().dispatch(DensityFunction::codec, Function.identity());
    public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, FULL_DIRECT_CODEC).xmap(either -> (DensityFunction)either.map(DensityFunctions::constant, Function.identity()), function -> {
        Either either;
        DensityFunction densityFunction = function;
        Objects.requireNonNull(densityFunction);
        DensityFunction selector1$temp = densityFunction;
        int index$2 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ConstantFunction.class}, (DensityFunction)selector1$temp, index$2)) {
            case 0: {
                float patt3$temp;
                ConstantFunction $b$0 = (ConstantFunction)selector1$temp;
                float tmp0$ = patt3$temp = $b$0.value();
                float value = patt3$temp;
                either = Either.left((Object)Float.valueOf(value));
                return either;
            }
        }
        either = Either.right((Object)function);
        return either;
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    });
    private static final ConstantFunction ZERO = new ConstantFunction(0.0f);

    public static MapCodec<? extends DensityFunction> bootstrap(Registry<MapCodec<? extends DensityFunction>> registry) {
        MapCodec<? extends DensityFunction> constant = DensityFunctions.register(registry, "constant", ConstantFunction.CODEC);
        for (SimpleDensityFunction simpleDensityFunction : SimpleDensityFunction.values()) {
            DensityFunctions.register(registry, simpleDensityFunction.id(), simpleDensityFunction.codec());
        }
        DensityFunctions.register(registry, "noise", NoiseFunction.CODEC);
        DensityFunctions.register(registry, "end_outer_islands", EndIslandFunction.CODEC);
        DensityFunctions.register(registry, "distance_to_point", DistanceToPointFunction.CODEC);
        DensityFunctions.register(registry, "gradient", GradientFunction.CODEC);
        DensityFunctions.register(registry, "shift_a", ShiftNoiseFunction.ShiftA.CODEC);
        DensityFunctions.register(registry, "shift_b", ShiftNoiseFunction.ShiftB.CODEC);
        DensityFunctions.register(registry, "shift", ShiftNoiseFunction.Shift.CODEC);
        for (Enum enum_ : UnaryFunction.Type.values()) {
            DensityFunctions.register(registry, ((UnaryFunction.Type)enum_).id, ((UnaryFunction.Type)enum_).codec);
        }
        for (Enum enum_ : RoundFunction.Type.values()) {
            DensityFunctions.register(registry, ((RoundFunction.Type)enum_).id, ((RoundFunction.Type)enum_).codec);
        }
        for (Enum enum_ : BinaryFunction.Type.values()) {
            DensityFunctions.register(registry, ((BinaryFunction.Type)enum_).id, ((BinaryFunction.Type)enum_).codec);
        }
        DensityFunctions.register(registry, "pow", PowFunction.CODEC);
        DensityFunctions.register(registry, "spline", SplineFunction.CODEC);
        DensityFunctions.register(registry, "lerp", LerpFunction.CODEC);
        DensityFunctions.register(registry, "clamp", ClampFunction.CODEC);
        DensityFunctions.register(registry, "range_choice", RangeChoiceFunction.CODEC);
        DensityFunctions.register(registry, "interval_select", IntervalSelectFunction.CODEC);
        DensityFunctions.register(registry, "cache", CacheFunction.CODEC);
        DensityFunctions.register(registry, "blend_density", BlendDensityFunction.CODEC);
        DensityFunctions.register(registry, "interpolated", InterpolatedFunction.CODEC);
        DensityFunctions.register(registry, "slice", SliceFunction.CODEC);
        DensityFunctions.register(registry, "find_top_surface", FindTopSurfaceFunction.CODEC);
        DensityFunctions.register(registry, "old_blended_noise", BlendedNoise.CODEC);
        return constant;
    }

    private static MapCodec<? extends DensityFunction> register(Registry<MapCodec<? extends DensityFunction>> registry, String name, MapCodec<? extends DensityFunction> codec) {
        return Registry.register(registry, name, codec);
    }

    private DensityFunctions() {
    }

    public static DensityFunction zero() {
        return ZERO;
    }

    public static DensityFunction constant(float value) {
        return new ConstantFunction(value);
    }

    public static DensityFunction abs(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.ABS, input);
    }

    public static DensityFunction square(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.SQUARE, input);
    }

    public static DensityFunction cube(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.CUBE, input);
    }

    public static DensityFunction sqrt(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.SQRT, input);
    }

    public static DensityFunction halfNegative(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.HALF_NEGATIVE, input);
    }

    public static DensityFunction quarterNegative(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.QUARTER_NEGATIVE, input);
    }

    public static DensityFunction reciprocal(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.RECIPROCAL, input);
    }

    public static DensityFunction negate(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.NEGATE, input);
    }

    public static DensityFunction squeeze(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.SQUEEZE, input);
    }

    public static DensityFunction log(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.LOG, input);
    }

    public static DensityFunction sign(DensityFunction input) {
        return new UnaryFunction(UnaryFunction.Type.SIGN, input);
    }

    public static DensityFunction interpolated(DensityFunction function, int cellSizeXz, int cellSizeY) {
        return new InterpolatedFunction(function, cellSizeXz, cellSizeY);
    }

    public static DensityFunction cache(DensityFunction function) {
        return new CacheFunction(function);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise> noiseData, @Deprecated double xzScale, double yScale, float minTarget, float maxTarget) {
        NoiseFunction noise = new NoiseFunction(noiseData, xzScale, yScale, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
        return DensityFunctions.remap(noise, -1.0f, 1.0f, minTarget, maxTarget);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise> noiseData, double yScale, float minTarget, float maxTarget) {
        return DensityFunctions.mappedNoise(noiseData, 1.0, yScale, minTarget, maxTarget);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise> noiseData, float minTarget, float maxTarget) {
        return DensityFunctions.mappedNoise(noiseData, 1.0, 1.0, minTarget, maxTarget);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction shiftX, DensityFunction shiftZ, double xzScale, Holder<NormalNoise> noiseData) {
        return new NoiseFunction(noiseData, xzScale, 0.0, shiftX, DensityFunctions.zero(), shiftZ);
    }

    public static DensityFunction noise(Holder<NormalNoise> noiseData) {
        return DensityFunctions.noise(noiseData, 1.0, 1.0);
    }

    public static DensityFunction noise(Holder<NormalNoise> noiseData, double xzScale, double yScale) {
        return new NoiseFunction(noiseData, xzScale, yScale, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    public static DensityFunction noise(Holder<NormalNoise> noiseData, double yScale) {
        return DensityFunctions.noise(noiseData, 1.0, yScale);
    }

    public static DensityFunction rangeChoice(DensityFunction input, float minInclusive, float maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) {
        return new RangeChoiceFunction(input, minInclusive, maxExclusive, whenInRange, whenOutOfRange);
    }

    public static DensityFunction intervalSelect(DensityFunction input, FloatList thresholds, List<DensityFunction> functions) {
        return new IntervalSelectFunction(input, thresholds, functions);
    }

    public static DensityFunction shiftA(Holder<NormalNoise> noiseData) {
        return new ShiftNoiseFunction.ShiftA(noiseData);
    }

    public static DensityFunction shiftB(Holder<NormalNoise> noiseData) {
        return new ShiftNoiseFunction.ShiftB(noiseData);
    }

    public static DensityFunction shift(Holder<NormalNoise> noiseData) {
        return new ShiftNoiseFunction.Shift(noiseData);
    }

    public static DensityFunction blendDensity(DensityFunction input) {
        return new BlendDensityFunction(input);
    }

    public static DensityFunction endOuterIslands() {
        return new EndIslandFunction();
    }

    public static DensityFunction distanceToPoint(Vec3i point, DistanceMetric metric) {
        return new DistanceToPointFunction(point, metric);
    }

    public static DensityFunction add(DensityFunction left, DensityFunction right) {
        return new BinaryFunction(BinaryFunction.Type.ADD, left, right);
    }

    public static DensityFunction sub(DensityFunction left, DensityFunction right) {
        return new BinaryFunction(BinaryFunction.Type.SUB, left, right);
    }

    public static DensityFunction mul(DensityFunction left, DensityFunction right) {
        return new BinaryFunction(BinaryFunction.Type.MUL, left, right);
    }

    public static DensityFunction div(DensityFunction left, DensityFunction right) {
        return new BinaryFunction(BinaryFunction.Type.DIV, left, right);
    }

    public static DensityFunction pow(DensityFunction base, DensityFunction exponent) {
        return new PowFunction(base, exponent);
    }

    public static DensityFunction clamp(DensityFunction input, float min, float max) {
        return new ClampFunction(input, min, max);
    }

    public static DensityFunction min(DensityFunction left, DensityFunction right) {
        return new BinaryFunction(BinaryFunction.Type.MIN, left, right);
    }

    public static DensityFunction max(DensityFunction left, DensityFunction right) {
        return new BinaryFunction(BinaryFunction.Type.MAX, left, right);
    }

    public static DensityFunction spline(CubicSpline<SplineFunction.Coordinate> spline) {
        return new SplineFunction(spline);
    }

    public static DensityFunction sliceY(DensityFunction input, int y) {
        return DensityFunctions.slice(Direction.Axis.Y, y, input);
    }

    public static DensityFunction slice(Direction.Axis axis, int coordinate, DensityFunction input) {
        return new SliceFunction(axis, coordinate, input);
    }

    public static DensityFunction yClampedGradient(int fromY, int toY, float fromValue, float toValue) {
        return DensityFunctions.gradient(Direction.Axis.Y, TilingMode.CLAMP_TO_EDGE, fromY, toY, fromValue, toValue);
    }

    public static DensityFunction gradient(Direction.Axis axis, TilingMode tiling, int fromCoordinate, int toCoordinate, float fromValue, float toValue) {
        return new GradientFunction(axis, tiling, fromCoordinate, toCoordinate, fromValue, toValue);
    }

    public static DensityFunction blendAlpha() {
        return SimpleDensityFunction.BLEND_ALPHA;
    }

    public static DensityFunction blendOffset() {
        return SimpleDensityFunction.BLEND_OFFSET;
    }

    public static DensityFunction beardifier() {
        return SimpleDensityFunction.BEARDIFIER;
    }

    public static DensityFunction lerp(DensityFunction alpha, DensityFunction first, DensityFunction second) {
        return new LerpFunction(alpha, first, second);
    }

    public static DensityFunction lerp(DensityFunction factor, float first, DensityFunction second) {
        return DensityFunctions.lerp(factor, DensityFunctions.constant(first), second);
    }

    public static DensityFunction round(RoundFunction.Type type, DensityFunction input, DensityFunction multiple) {
        return new RoundFunction(type, input, multiple);
    }

    public static DensityFunction round(RoundFunction.Type type, DensityFunction input) {
        return DensityFunctions.round(type, input, DensityFunctions.constant(1.0f));
    }

    public static DensityFunction floor(DensityFunction input, DensityFunction multiple) {
        return DensityFunctions.round(RoundFunction.Type.FLOOR, input, multiple);
    }

    public static DensityFunction floor(DensityFunction input) {
        return DensityFunctions.floor(input, DensityFunctions.constant(1.0f));
    }

    public static DensityFunction round(DensityFunction input, DensityFunction multiple) {
        return DensityFunctions.round(RoundFunction.Type.ROUND, input, multiple);
    }

    public static DensityFunction round(DensityFunction input) {
        return DensityFunctions.round(input, DensityFunctions.constant(1.0f));
    }

    public static DensityFunction ceil(DensityFunction input, DensityFunction multiple) {
        return DensityFunctions.round(RoundFunction.Type.CEIL, input, multiple);
    }

    public static DensityFunction ceil(DensityFunction input) {
        return DensityFunctions.ceil(input, DensityFunctions.constant(1.0f));
    }

    public static DensityFunction truncate(DensityFunction input, DensityFunction multiple) {
        return DensityFunctions.round(RoundFunction.Type.TRUNCATE, input, multiple);
    }

    public static DensityFunction truncate(DensityFunction input) {
        return DensityFunctions.truncate(input, DensityFunctions.constant(1.0f));
    }

    public static DensityFunction findTopSurface(DensityFunction density, DensityFunction upperBound, int lowerBound, int stepSize) {
        return new FindTopSurfaceFunction(density, upperBound, lowerBound, stepSize);
    }

    public static DensityFunction remap(DensityFunction input, float fromMin, float fromMax, float toMin, float toMax) {
        float factor = (toMax - toMin) / (fromMax - fromMin);
        float offset = toMin - fromMin * factor;
        if (offset == 0.0f) {
            return input.mul(factor);
        }
        return input.mul(factor).add(offset);
    }

    public static DensityFunction clampedMap(DensityFunction input, float fromMin, float fromMax, float toMin, float toMax) {
        return DensityFunctions.remap(input.clamp(fromMin, fromMax), fromMin, fromMax, toMin, toMax);
    }

    public record HolderHolder(Holder<DensityFunction> function) implements DensityFunction
    {
        @Override
        public DensitySampler compileSampler(DensityFunction.CompileContext context) {
            return this.function.value().compileSampler(context);
        }

        @Override
        public DensityFunction rewriteChildren(DfRewriteRule rule) {
            DensityFunction newFunction = rule.rewrite(this.function.value());
            if (newFunction == this.function.value()) {
                return this;
            }
            return newFunction;
        }

        @Override
        public Interval range() {
            return this.function.value().range();
        }

        @Override
        public @DensityFunction.Axes int domainAxes() {
            return this.function.value().domainAxes();
        }

        public MapCodec<HolderHolder> codec() {
            throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
        }
    }
}


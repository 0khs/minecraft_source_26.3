/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Interval;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface DensityFunction {
    public static final Codec<Holder<DensityFunction>> REFERENCE_CODEC = RegistryCodecs.holder(Registries.DENSITY_FUNCTION);
    public static final Codec<DensityFunction> CODEC = RegistryCodecs.holder(Registries.DENSITY_FUNCTION, DensityFunctions.DIRECT_CODEC).xmap(holder -> {
        Holder holder2 = holder;
        Objects.requireNonNull(holder2);
        Holder selector0$temp = holder2;
        int index$1 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Holder.Direct.class, Holder.Reference.class}, (Holder)selector0$temp, index$1)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Holder.Direct direct = (Holder.Direct)selector0$temp;
                yield (DensityFunction)direct.value();
            }
            case 1 -> {
                Holder.Reference reference = (Holder.Reference)selector0$temp;
                yield new DensityFunctions.HolderHolder(reference);
            }
        };
    }, value -> {
        Holder<DensityFunction> holder;
        DensityFunction densityFunction = value;
        Objects.requireNonNull(densityFunction);
        DensityFunction selector1$temp = densityFunction;
        int index$2 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DensityFunctions.HolderHolder.class}, (DensityFunction)selector1$temp, index$2)) {
            case 0: {
                DensityFunctions.HolderHolder $b$0 = (DensityFunctions.HolderHolder)selector1$temp;
                try {
                    Holder<DensityFunction> patt3$temp;
                    Holder<DensityFunction> function = patt3$temp = $b$0.function();
                    holder = function;
                    return holder;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
        }
        holder = Holder.direct(value);
        return holder;
    });
    public static final int AXIS_X = 1;
    public static final int AXIS_Y = 2;
    public static final int AXIS_Z = 4;
    public static final @Axes int NO_AXES = 0;
    public static final @Axes int ALL_AXES = 7;

    public static @Axes int axesFrom(Direction.Axis axis) {
        return switch (axis) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> 1;
            case Direction.Axis.Y -> 2;
            case Direction.Axis.Z -> 4;
        };
    }

    public DensitySampler compileSampler(CompileContext var1);

    public DensityFunction rewriteChildren(DfRewriteRule var1);

    public Interval range();

    public @Axes int domainAxes();

    public MapCodec<? extends DensityFunction> codec();

    default public DensityFunction clamp(float min, float max) {
        return DensityFunctions.clamp(this, min, max);
    }

    default public DensityFunction abs() {
        return DensityFunctions.abs(this);
    }

    default public DensityFunction square() {
        return DensityFunctions.square(this);
    }

    default public DensityFunction cube() {
        return DensityFunctions.cube(this);
    }

    default public DensityFunction sqrt() {
        return DensityFunctions.sqrt(this);
    }

    default public DensityFunction halfNegative() {
        return DensityFunctions.halfNegative(this);
    }

    default public DensityFunction quarterNegative() {
        return DensityFunctions.quarterNegative(this);
    }

    default public DensityFunction reciprocal() {
        return DensityFunctions.reciprocal(this);
    }

    default public DensityFunction negate() {
        return DensityFunctions.negate(this);
    }

    default public DensityFunction squeeze() {
        return DensityFunctions.squeeze(this);
    }

    default public DensityFunction log() {
        return DensityFunctions.log(this);
    }

    default public DensityFunction sign() {
        return DensityFunctions.sign(this);
    }

    default public DensityFunction add(DensityFunction right) {
        return DensityFunctions.add(this, right);
    }

    default public DensityFunction add(float right) {
        return DensityFunctions.add(this, DensityFunctions.constant(right));
    }

    default public DensityFunction sub(DensityFunction right) {
        return DensityFunctions.sub(this, right);
    }

    default public DensityFunction sub(float right) {
        return DensityFunctions.sub(this, DensityFunctions.constant(right));
    }

    default public DensityFunction mul(DensityFunction right) {
        return DensityFunctions.mul(this, right);
    }

    default public DensityFunction mul(float right) {
        return DensityFunctions.mul(this, DensityFunctions.constant(right));
    }

    default public DensityFunction div(DensityFunction right) {
        return DensityFunctions.div(this, right);
    }

    default public DensityFunction div(float right) {
        return DensityFunctions.div(this, DensityFunctions.constant(right));
    }

    default public DensityFunction pow(DensityFunction exponent) {
        return DensityFunctions.pow(this, exponent);
    }

    default public DensityFunction pow(float exponent) {
        if (exponent == 0.5f) {
            return this.sqrt();
        }
        if (exponent == 2.0f) {
            return this.square();
        }
        if (exponent == 3.0f) {
            return this.cube();
        }
        return DensityFunctions.pow(this, DensityFunctions.constant(exponent));
    }

    public static interface CompileContext {
        public Noise createNoiseSampler(Holder<NormalNoise> var1);

        public RandomSource createRandom(Identifier var1);

        @Deprecated
        public RandomSource createEndIslandRandom();
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Axes {
    }
}


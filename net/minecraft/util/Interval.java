/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatUnaryOperator
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import java.util.List;
import net.minecraft.util.Mth;

public final class Interval {
    public static final Interval NaI = new Interval(Float.NaN, Float.NaN);
    public static final Interval INFINITE = new Interval(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    private static final Interval NEGATIVE_ONE_TO_ONE = new Interval(-1.0f, 1.0f);
    private static final Interval ZERO_TO_ONE = new Interval(0.0f, 1.0f);
    private final float min;
    private final float max;

    private Interval(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public static Interval of(float min, float max) {
        if (max < min) {
            throw new IllegalArgumentException("max (" + max + ") < min (" + min + ")");
        }
        if (Float.isNaN(min) || Float.isNaN(max)) {
            throw new IllegalArgumentException("Bounds cannot include NaN [" + min + "; " + max + "]: use Interval.NaI explicitly");
        }
        if (min == Float.NEGATIVE_INFINITY && max == Float.POSITIVE_INFINITY) {
            return INFINITE;
        }
        if (max == 1.0f) {
            if (min == 0.0f) {
                return ZERO_TO_ONE;
            }
            if (min == -1.0f) {
                return NEGATIVE_ONE_TO_ONE;
            }
        }
        return new Interval(min, max);
    }

    public static Interval ofSymmetric(float range) {
        return Interval.of(-range, range);
    }

    public static Interval ofExact(float value) {
        return Interval.of(value, value);
    }

    public static Interval encapsulating(List<Interval> intervals) {
        if (intervals.isEmpty()) {
            throw new IllegalArgumentException("At least one interval required");
        }
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (Interval interval : intervals) {
            if (interval.isNaI()) continue;
            min = Math.min(interval.min, min);
            max = Math.max(interval.max, max);
        }
        if (max < min) {
            return NaI;
        }
        return Interval.of(min, max);
    }

    public static Interval encapsulating(Interval ... intervals) {
        return Interval.encapsulating(List.of(intervals));
    }

    public static Interval encapsulating(float first, float second) {
        if (Float.isNaN(first) && Float.isNaN(second)) {
            return NaI;
        }
        if (Float.isNaN(first)) {
            return Interval.ofExact(second);
        }
        if (Float.isNaN(second)) {
            return Interval.ofExact(first);
        }
        return Interval.of(Math.min(first, second), Math.max(first, second));
    }

    private static Interval encapsulating(Interval first, float second) {
        if (Float.isNaN(second)) {
            return first;
        }
        if (first.isNaI()) {
            return Interval.ofExact(second);
        }
        return Interval.of(Math.min(first.min(), second), Math.max(first.max(), second));
    }

    public static Interval add(Interval left, Interval right) {
        float min = left.min + right.min;
        float max = left.max + right.max;
        if (Float.isNaN(min) || Float.isNaN(max)) {
            return NaI;
        }
        return Interval.of(min, max);
    }

    public static Interval sub(Interval left, Interval right) {
        float min = left.min - right.max;
        float max = left.max - right.min;
        if (Float.isNaN(min) || Float.isNaN(max)) {
            return NaI;
        }
        return Interval.of(min, max);
    }

    public static Interval mul(Interval left, Interval right) {
        if (left.isNaI() || right.isNaI()) {
            return NaI;
        }
        float minMin = Interval.mulBound(left.min, right.min);
        float minMax = Interval.mulBound(left.min, right.max);
        float maxMin = Interval.mulBound(left.max, right.min);
        float maxMax = Interval.mulBound(left.max, right.max);
        return Interval.of(Math.min(Math.min(minMin, minMax), Math.min(maxMin, maxMax)), Math.max(Math.max(minMin, minMax), Math.max(maxMin, maxMax)));
    }

    private static float mulBound(float left, float right) {
        return left == 0.0f || right == 0.0f ? 0.0f : left * right;
    }

    public static Interval reciprocal(Interval input) {
        if (input.isNaI() || input.min == 0.0f && input.max == 0.0f) {
            return NaI;
        }
        if (!input.contains(0.0f)) {
            return Interval.of(1.0f / input.max, 1.0f / input.min);
        }
        if (input.max == 0.0f) {
            return Interval.of(Float.NEGATIVE_INFINITY, 1.0f / input.min);
        }
        if (input.min == 0.0f) {
            return Interval.of(1.0f / input.max, Float.POSITIVE_INFINITY);
        }
        return INFINITE;
    }

    public static Interval div(Interval left, Interval right) {
        return Interval.mul(left, Interval.reciprocal(right));
    }

    public static Interval min(Interval left, Interval right) {
        if (left.isNaI() || right.isNaI()) {
            return NaI;
        }
        return Interval.of(Math.min(left.min, right.min), Math.min(left.max, right.max));
    }

    public static Interval max(Interval left, Interval right) {
        if (left.isNaI() || right.isNaI()) {
            return NaI;
        }
        return Interval.of(Math.max(left.min, right.min), Math.max(left.max, right.max));
    }

    public static Interval clamp(Interval input, float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
        }
        if (input.isNaI()) {
            return NaI;
        }
        if (input.min >= max) {
            return Interval.of(max, max);
        }
        if (input.max <= min) {
            return Interval.of(min, min);
        }
        return Interval.of(Math.max(input.min, min), Math.min(input.max, max));
    }

    public static Interval abs(Interval input) {
        if (input.isNaI()) {
            return NaI;
        }
        float max = Math.max(Math.abs(input.min), Math.abs(input.max));
        if (input.contains(0.0f)) {
            return Interval.of(0.0f, max);
        }
        return Interval.of(Math.min(Math.abs(input.min), Math.abs(input.max)), max);
    }

    public static Interval square(Interval input) {
        if (input.isNaI()) {
            return NaI;
        }
        float max = Math.max(Mth.square(input.min), Mth.square(input.max));
        if (input.contains(0.0f)) {
            return Interval.of(0.0f, max);
        }
        return Interval.of(Math.min(Mth.square(input.min), Mth.square(input.max)), max);
    }

    public static Interval pow(Interval base, Interval exponent) {
        if (base.isNaI() || exponent.isNaI()) {
            return NaI;
        }
        if (base.min() == base.max()) {
            return Interval.pow(base.min(), exponent);
        }
        Interval result = Interval.encapsulating(Interval.pow(base.min(), exponent), Interval.pow(base.max(), exponent));
        if (base.contains(0.0f)) {
            if (base.max() > 0.0f) {
                result = Interval.encapsulating(result, Interval.pow(0.0f, exponent));
            }
            if (base.min() < 0.0f) {
                result = Interval.encapsulating(result, Interval.pow(-0.0f, exponent));
            }
        }
        return result;
    }

    private static Interval pow(float base, Interval exponent) {
        if (Float.isNaN(base) || exponent.isNaI()) {
            return NaI;
        }
        if (exponent.min() == exponent.max()) {
            float value = (float)Math.pow(base, exponent.min());
            return Float.isNaN(value) ? NaI : Interval.ofExact(value);
        }
        if (base == 0.0f) {
            return Interval.mul(Interval.powZeroBase(exponent), Interval.ofExact(Math.copySign(1.0f, base)));
        }
        if (base == 1.0f) {
            return Interval.ofExact(1.0f);
        }
        if (base > 0.0f) {
            return Interval.powPositiveBase(base, exponent);
        }
        return Interval.powNegativeBase(base, exponent);
    }

    private static Interval powPositiveBase(float base, Interval exponent) {
        if (Float.isFinite(exponent.min()) && Float.isFinite(exponent.max())) {
            return Interval.encapsulating((float)Math.pow(base, exponent.min()), (float)Math.pow(base, exponent.max()));
        }
        return Interval.powInfiniteExponent(base, exponent);
    }

    private static Interval powZeroBase(Interval exponent) {
        if (exponent.contains(0.0f)) {
            if (exponent.max() == 0.0f) {
                return Interval.of(1.0f, Float.POSITIVE_INFINITY);
            }
            if (exponent.min() == 0.0f) {
                return ZERO_TO_ONE;
            }
            return Interval.of(0.0f, Float.POSITIVE_INFINITY);
        }
        if (exponent.max() < 0.0f) {
            return Interval.ofExact(Float.POSITIVE_INFINITY);
        }
        return Interval.ofExact(0.0f);
    }

    private static Interval powInfiniteExponent(float base, Interval exponent) {
        if (Float.isInfinite(exponent.min()) && Float.isInfinite(exponent.max())) {
            return Interval.of(0.0f, Float.POSITIVE_INFINITY);
        }
        if (Float.isInfinite(exponent.min())) {
            if (base < 1.0f) {
                return Interval.of((float)Math.pow(base, exponent.max()), Float.POSITIVE_INFINITY);
            }
            return Interval.of(0.0f, (float)Math.pow(base, exponent.max()));
        }
        if (base < 1.0f) {
            return Interval.of(0.0f, (float)Math.pow(base, exponent.min()));
        }
        return Interval.of((float)Math.pow(base, exponent.min()), Float.POSITIVE_INFINITY);
    }

    private static Interval powNegativeBase(float base, Interval exponent) {
        float exponentMinInt = (float)Math.ceil(exponent.min());
        float exponentMaxInt = (float)Math.floor(exponent.max());
        if (exponentMaxInt < exponentMinInt) {
            return NaI;
        }
        float baseToMinInt = (float)Math.pow(base, exponentMinInt);
        float baseToMaxInt = (float)Math.pow(base, exponentMaxInt);
        Interval result = Interval.encapsulating(baseToMinInt, baseToMaxInt);
        if (Float.isInfinite(exponentMinInt)) {
            result = Interval.encapsulating(result, -baseToMinInt);
        } else if (exponentMinInt + 1.0f < exponentMaxInt) {
            result = Interval.encapsulating(result, (float)Math.pow(base, exponentMinInt + 1.0f));
        }
        if (Float.isInfinite(exponentMaxInt)) {
            result = Interval.encapsulating(result, -baseToMaxInt);
        } else if (exponentMaxInt - 1.0f > exponentMinInt) {
            result = Interval.encapsulating(result, (float)Math.pow(base, exponentMaxInt - 1.0f));
        }
        return result;
    }

    public static Interval log(Interval input) {
        if (input.max() < 0.0f) {
            return NaI;
        }
        Interval clippedInput = Interval.max(input, Interval.ofExact(0.0f));
        return Interval.mapMonotonic(clippedInput, x -> (float)Math.log(x));
    }

    public static Interval mapMonotonic(Interval input, FloatUnaryOperator monotonicOp) {
        if (input.isNaI()) {
            return NaI;
        }
        float mappedMin = monotonicOp.apply(input.min);
        float mappedMax = monotonicOp.apply(input.max);
        if (Float.isNaN(mappedMin) || Float.isNaN(mappedMax)) {
            throw new IllegalStateException("Monotonic operator should not produce NaN");
        }
        return Interval.of(Math.min(mappedMin, mappedMax), Math.max(mappedMin, mappedMax));
    }

    public static Interval lerp(Interval alpha, Interval first, Interval second) {
        if (alpha.isNaI() || first.isNaI() || second.isNaI()) {
            return NaI;
        }
        return Interval.encapsulating(Interval.lerp(alpha, first.min, second.min), Interval.lerp(alpha, first.max, second.min), Interval.lerp(alpha, first.min, second.max), Interval.lerp(alpha, first.max, second.max));
    }

    public static Interval lerp(Interval alpha, float first, float second) {
        if (alpha.isNaI() || Float.isNaN(first) || Float.isNaN(second)) {
            return NaI;
        }
        if (Float.isFinite(first) && Float.isFinite(second)) {
            return Interval.lerpFiniteBounds(alpha, first, second);
        }
        return Interval.lerpInfiniteBounds(alpha, first, second);
    }

    private static Interval lerpFiniteBounds(Interval alpha, float first, float second) {
        return Interval.encapsulating(Interval.lerpFiniteBound(alpha.min, first, second), Interval.lerpFiniteBound(alpha.max, first, second));
    }

    private static float lerpFiniteBound(float alpha, float first, float second) {
        return first + Interval.mulBound(alpha, second - first);
    }

    private static Interval lerpInfiniteBounds(Interval alpha, float first, float second) {
        if (first == second) {
            return Interval.ofExact(first);
        }
        float newMin = Interval.lerpInfiniteBound(alpha.min, first, second);
        float newMax = Interval.lerpInfiniteBound(alpha.max, first, second);
        if (Float.isNaN(newMin) || Float.isNaN(newMax)) {
            return NaI;
        }
        return Interval.encapsulating(newMin, newMax);
    }

    private static float lerpInfiniteBound(float alpha, float first, float second) {
        float firstPart = Interval.mulBound(1.0f - alpha, first);
        float secondPart = Interval.mulBound(alpha, second);
        if (Float.isInfinite(firstPart) && Float.isInfinite(secondPart)) {
            if (alpha <= 0.0f) {
                return second > first ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            }
            if (alpha >= 1.0f) {
                return second > first ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
            }
            return Float.NaN;
        }
        return firstPart + secondPart;
    }

    public static Interval sign(Interval input) {
        if (input.isNaI()) {
            return NaI;
        }
        if (input.min() == input.max()) {
            return Interval.ofExact(Math.signum(input.min()));
        }
        if (input.contains(0.0f)) {
            if (input.min() == 0.0f) {
                return Interval.of(0.0f, 1.0f);
            }
            if (input.max() == 0.0f) {
                return Interval.of(-1.0f, 0.0f);
            }
            return Interval.of(-1.0f, 1.0f);
        }
        return Interval.ofExact(input.min() > 0.0f ? 1.0f : -1.0f);
    }

    public boolean contains(float value) {
        return value >= this.min && value <= this.max;
    }

    public boolean intersects(Interval other) {
        return this.min <= other.max && this.max >= other.min;
    }

    public boolean isNaI() {
        return this == NaI;
    }

    public float min() {
        return this.min;
    }

    public float max() {
        return this.max;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Interval)) return false;
        Interval interval = (Interval)obj;
        if (this.min != interval.min) return false;
        if (this.max != interval.max) return false;
        return true;
    }

    public int hashCode() {
        int hash = Float.hashCode(this.min);
        hash = hash * 31 + Float.hashCode(this.max);
        return hash;
    }

    public String toString() {
        if (this.isNaI()) {
            return "[NaN]";
        }
        return "[" + this.min + "; " + this.max + "]";
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  org.joml.Vector3fc
 *  org.joml.Vector4fc
 */
package net.minecraft.world.attribute;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public interface LerpFunction<T> {
    public static final LerpFunction<?> CONSTANT = LerpFunction.ofStep(1.0f);

    public static LerpFunction<Float> ofFloat() {
        return Mth::lerp;
    }

    public static LerpFunction<Integer> ofInteger() {
        return Mth::lerpInt;
    }

    public static LerpFunction<Float> ofDegrees(float maxDelta) {
        return (alpha, from, to) -> {
            float delta = Mth.wrapDegrees(to.floatValue() - from.floatValue());
            if (Math.abs(delta) >= maxDelta) {
                return to;
            }
            return Float.valueOf(from.floatValue() + alpha * delta);
        };
    }

    public static <T> LerpFunction<T> ofConstant() {
        return CONSTANT;
    }

    public static <T> LerpFunction<T> ofStep(float threshold) {
        return (alpha, from, to) -> alpha >= threshold ? to : from;
    }

    public static LerpFunction<Integer> ofColor() {
        return ARGB::srgbLerp;
    }

    public static LerpFunction<Vector3fc> ofColorVec3() {
        return ARGB::srgbLerp;
    }

    public static LerpFunction<Vector4fc> ofColorVec4() {
        return ARGB::srgbLerp;
    }

    public static <T> LerpFunction<List<T>> ofListCrossFade(AlphaScaler<T> scaler) {
        return (alpha, from, to) -> {
            if (alpha == 0.0f) {
                return from;
            }
            if (alpha == 1.0f) {
                return to;
            }
            ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)(from.size() + to.size()));
            for (Object element : from) {
                builder.add(scaler.apply(element, 1.0f - alpha));
            }
            for (Object element : to) {
                builder.add(scaler.apply(element, alpha));
            }
            return builder.build();
        };
    }

    public T apply(float var1, T var2, T var3);

    @FunctionalInterface
    public static interface AlphaScaler<T> {
        public T apply(T var1, float var2);
    }
}


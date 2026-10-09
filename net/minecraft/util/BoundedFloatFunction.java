/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.Interval;

public interface BoundedFloatFunction<C> {
    public static final BoundedFloatFunction<Float> IDENTITY = new BoundedFloatFunction<Float>(){

        @Override
        public float apply(Float value) {
            return value.floatValue();
        }

        @Override
        public Interval range() {
            return Interval.INFINITE;
        }
    };

    public float apply(C var1);

    public Interval range();

    public static <C> BoundedFloatFunction<C> constant(final float value) {
        final Interval range = Interval.ofExact(value);
        return new BoundedFloatFunction<C>(){

            @Override
            public float apply(C c) {
                return value;
            }

            @Override
            public Interval range() {
                return range;
            }
        };
    }

    default public <C2> BoundedFloatFunction<C2> comap(final Function<C2, C> function) {
        final BoundedFloatFunction outer = this;
        return new BoundedFloatFunction<C2>(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public float apply(C2 c2) {
                return outer.apply(function.apply(c2));
            }

            @Override
            public Interval range() {
                return outer.range();
            }
        };
    }
}


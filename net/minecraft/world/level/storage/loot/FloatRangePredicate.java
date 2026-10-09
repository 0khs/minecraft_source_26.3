/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import org.jspecify.annotations.Nullable;

public sealed interface FloatRangePredicate
extends Validatable {
    public static final Codec<Point> POINT_CODEC = ContextFloatProviders.CODEC.xmap(Point::new, Point::value);
    public static final Codec<Line> LINE_CODEC = RecordCodecBuilder.create(i -> i.group((App)ContextFloatProviders.CODEC.optionalFieldOf("min").forGetter(r -> r.min), (App)ContextFloatProviders.CODEC.optionalFieldOf("max").forGetter(r -> r.max)).apply((Applicative)i, Line::new));
    public static final Codec<FloatRangePredicate> CODEC = Codec.either(POINT_CODEC, LINE_CODEC).xmap(Either::unwrap, range -> {
        FloatRangePredicate floatRangePredicate = range;
        Objects.requireNonNull(floatRangePredicate);
        FloatRangePredicate selector0$temp = floatRangePredicate;
        int index$1 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Point.class, Line.class}, (FloatRangePredicate)selector0$temp, index$1)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Point point = (Point)selector0$temp;
                yield Either.left((Object)point);
            }
            case 1 -> {
                Line line = (Line)selector0$temp;
                yield Either.right((Object)line);
            }
        };
    });

    public static FloatRangePredicate exact(float value) {
        return new Point(ContextFloatProviders.exactly(value));
    }

    public static FloatRangePredicate range(float min, float max) {
        return new Line(Optional.of(ContextFloatProviders.exactly(min)), Optional.of(ContextFloatProviders.exactly(max)));
    }

    public static FloatRangePredicate lowerBound(float value) {
        return new Line(Optional.of(ContextFloatProviders.exactly(value)), Optional.empty());
    }

    public static FloatRangePredicate upperBound(float value) {
        return new Line(Optional.empty(), Optional.of(ContextFloatProviders.exactly(value)));
    }

    public boolean test(LootContext var1, float var2);

    default public boolean test(LootContext context, ContextFloatProvider input) {
        return this.test(context, input.getFloat(context));
    }

    public record Point(Holder<ContextFloatProvider> value) implements FloatRangePredicate
    {
        @Override
        public void validate(ValidationContext context) {
            Validatable.validateHolder(context, this.value);
        }

        private float computeValue(LootContext context) {
            return this.value.value().getFloat(context);
        }

        @Override
        public boolean test(LootContext context, float input) {
            return this.computeValue(context) == input;
        }
    }

    public static final class Line
    implements FloatRangePredicate {
        private final Optional<Holder<ContextFloatProvider>> min;
        private final Optional<Holder<ContextFloatProvider>> max;
        private final @Nullable FloatChecker predicate;

        private Line(Optional<Holder<ContextFloatProvider>> min, Optional<Holder<ContextFloatProvider>> max) {
            this.min = min;
            this.max = max;
            this.predicate = Line.createPredicate(min.orElse(null), max.orElse(null));
        }

        private static @Nullable FloatChecker createPredicate(@Nullable Holder<ContextFloatProvider> min, @Nullable Holder<ContextFloatProvider> max) {
            if (min == null) {
                if (max == null) {
                    return null;
                }
                return (context, input) -> input <= ((ContextFloatProvider)max.value()).getFloat(context);
            }
            if (max == null) {
                return (context, input) -> input >= ((ContextFloatProvider)min.value()).getFloat(context);
            }
            return (context, input) -> input >= ((ContextFloatProvider)min.value()).getFloat(context) && input <= ((ContextFloatProvider)max.value()).getFloat(context);
        }

        @Override
        public void validate(ValidationContext context) {
            Validatable.validateHolder(context, "min", this.min);
            Validatable.validateHolder(context, "max", this.max);
        }

        @Override
        public boolean test(LootContext context, float input) {
            return this.predicate == null || this.predicate.test(context, input);
        }

        @Override
        public boolean test(LootContext context, ContextFloatProvider input) {
            return this.predicate == null || this.predicate.test(context, input.getFloat(context));
        }

        @FunctionalInterface
        private static interface FloatChecker {
            public boolean test(LootContext var1, float var2);
        }
    }
}


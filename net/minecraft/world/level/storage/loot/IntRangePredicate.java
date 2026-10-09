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
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jspecify.annotations.Nullable;

public sealed interface IntRangePredicate
extends Validatable {
    public static final Codec<Point> POINT_CODEC = ContextIntProviders.CODEC.xmap(Point::new, Point::value);
    public static final Codec<Line> LINE_CODEC = RecordCodecBuilder.create(i -> i.group((App)ContextIntProviders.CODEC.optionalFieldOf("min").forGetter(r -> r.min), (App)ContextIntProviders.CODEC.optionalFieldOf("max").forGetter(r -> r.max)).apply((Applicative)i, Line::new));
    public static final Codec<IntRangePredicate> CODEC = Codec.either(POINT_CODEC, LINE_CODEC).xmap(Either::unwrap, range -> {
        IntRangePredicate intRangePredicate = range;
        Objects.requireNonNull(intRangePredicate);
        IntRangePredicate selector0$temp = intRangePredicate;
        int index$1 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Point.class, Line.class}, (IntRangePredicate)selector0$temp, index$1)) {
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

    public static IntRangePredicate exact(int value) {
        return new Point(ContextIntProviders.exactly(value));
    }

    public static IntRangePredicate range(int min, int max) {
        return new Line(Optional.of(ContextIntProviders.exactly(min)), Optional.of(ContextIntProviders.exactly(max)));
    }

    public static IntRangePredicate lowerBound(int value) {
        return new Line(Optional.of(ContextIntProviders.exactly(value)), Optional.empty());
    }

    public static IntRangePredicate upperBound(int value) {
        return new Line(Optional.empty(), Optional.of(ContextIntProviders.exactly(value)));
    }

    public boolean test(LootContext var1, int var2);

    default public boolean test(LootContext context, ContextIntProvider input) {
        return this.test(context, input.getInt(context));
    }

    public record Point(Holder<ContextIntProvider> value) implements IntRangePredicate
    {
        @Override
        public void validate(ValidationContext context) {
            Validatable.validateHolder(context, this.value);
        }

        private int computeValue(LootContext context) {
            return this.value.value().getInt(context);
        }

        @Override
        public boolean test(LootContext context, int input) {
            return this.computeValue(context) == input;
        }
    }

    public static final class Line
    implements IntRangePredicate {
        private final Optional<Holder<ContextIntProvider>> min;
        private final Optional<Holder<ContextIntProvider>> max;
        private final @Nullable IntChecker predicate;

        private Line(Optional<Holder<ContextIntProvider>> min, Optional<Holder<ContextIntProvider>> max) {
            this.min = min;
            this.max = max;
            this.predicate = Line.createPredicate(min.orElse(null), max.orElse(null));
        }

        private static @Nullable IntChecker createPredicate(@Nullable Holder<ContextIntProvider> min, @Nullable Holder<ContextIntProvider> max) {
            if (min == null) {
                if (max == null) {
                    return null;
                }
                return (context, input) -> input <= ((ContextIntProvider)max.value()).getInt(context);
            }
            if (max == null) {
                return (context, input) -> input >= ((ContextIntProvider)min.value()).getInt(context);
            }
            return (context, input) -> input >= ((ContextIntProvider)min.value()).getInt(context) && input <= ((ContextIntProvider)max.value()).getInt(context);
        }

        @Override
        public void validate(ValidationContext context) {
            Validatable.validateHolder(context, "min", this.min);
            Validatable.validateHolder(context, "max", this.max);
        }

        @Override
        public boolean test(LootContext context, int input) {
            return this.predicate == null || this.predicate.test(context, input);
        }

        @Override
        public boolean test(LootContext context, ContextIntProvider input) {
            return this.predicate == null || this.predicate.test(context, input.getInt(context));
        }

        @FunctionalInterface
        private static interface IntChecker {
            public boolean test(LootContext var1, int var2);
        }
    }
}


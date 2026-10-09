/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jspecify.annotations.Nullable;

public class IntLimit
implements Validatable {
    public static final Codec<IntLimit> CODEC = RecordCodecBuilder.create(i -> i.group((App)ContextIntProviders.CODEC.optionalFieldOf("min").forGetter(r -> r.min), (App)ContextIntProviders.CODEC.optionalFieldOf("max").forGetter(r -> r.max)).apply((Applicative)i, IntLimit::new));
    private final Optional<Holder<ContextIntProvider>> min;
    private final Optional<Holder<ContextIntProvider>> max;
    private final IntLimiter limiter;

    private IntLimit(Optional<Holder<ContextIntProvider>> min, Optional<Holder<ContextIntProvider>> max) {
        this.min = min;
        this.max = max;
        this.limiter = IntLimit.createLimiter(min.orElse(null), max.orElse(null));
    }

    private static IntLimiter createLimiter(@Nullable Holder<ContextIntProvider> min, @Nullable Holder<ContextIntProvider> max) {
        if (min == null) {
            if (max == null) {
                return (lootContext, input) -> input;
            }
            return (context, input) -> Math.min(((ContextIntProvider)max.value()).getInt(context), input);
        }
        if (max == null) {
            return (context, input) -> Math.max(((ContextIntProvider)min.value()).getInt(context), input);
        }
        return (context, input) -> Mth.clamp(input, ((ContextIntProvider)min.value()).getInt(context), ((ContextIntProvider)max.value()).getInt(context));
    }

    public static IntLimit range(int min, int max) {
        return new IntLimit(Optional.of(ContextIntProviders.exactly(min)), Optional.of(ContextIntProviders.exactly(max)));
    }

    public static IntLimit lowerBound(int value) {
        return new IntLimit(Optional.of(ContextIntProviders.exactly(value)), Optional.empty());
    }

    public static IntLimit upperBound(int value) {
        return new IntLimit(Optional.empty(), Optional.of(ContextIntProviders.exactly(value)));
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "min", this.min);
        Validatable.validateHolder(context, "max", this.max);
    }

    public int clamp(LootContext context, int input) {
        return this.limiter.apply(context, input);
    }

    @FunctionalInterface
    private static interface IntLimiter {
        public int apply(LootContext var1, int var2);
    }
}


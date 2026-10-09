/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.FloatRangePredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record FloatValueCheck(Holder<ContextFloatProvider> value, FloatRangePredicate range) implements LootItemCondition
{
    public static final MapCodec<FloatValueCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)ContextFloatProviders.CODEC.fieldOf("value").forGetter(FloatValueCheck::value), (App)FloatRangePredicate.CODEC.fieldOf("test").forGetter(FloatValueCheck::range)).apply((Applicative)i, FloatValueCheck::new));

    public MapCodec<FloatValueCheck> codec() {
        return MAP_CODEC;
    }

    @Override
    public void validate(ValidationContext context) {
        LootItemCondition.super.validate(context);
        Validatable.validateHolder(context, "value", this.value);
        Validatable.validate(context, "range", this.range);
    }

    @Override
    public boolean test(LootContext context) {
        return this.range.test(context, this.value.value());
    }

    public static LootItemCondition.Builder hasValue(Holder<ContextFloatProvider> value, FloatRangePredicate range) {
        return () -> new FloatValueCheck(value, range);
    }
}


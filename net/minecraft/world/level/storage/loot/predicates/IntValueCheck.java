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
import net.minecraft.world.level.storage.loot.IntRangePredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record IntValueCheck(Holder<ContextIntProvider> value, IntRangePredicate range) implements LootItemCondition
{
    public static final MapCodec<IntValueCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)ContextIntProviders.CODEC.fieldOf("value").forGetter(IntValueCheck::value), (App)IntRangePredicate.CODEC.fieldOf("test").forGetter(IntValueCheck::range)).apply((Applicative)i, IntValueCheck::new));

    public MapCodec<IntValueCheck> codec() {
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

    public static LootItemCondition.Builder hasValue(Holder<ContextIntProvider> value, IntRangePredicate range) {
        return () -> new IntValueCheck(value, range);
    }
}


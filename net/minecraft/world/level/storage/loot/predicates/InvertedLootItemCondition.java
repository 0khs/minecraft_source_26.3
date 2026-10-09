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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record InvertedLootItemCondition(Holder<LootItemCondition> term) implements LootItemCondition
{
    public static final MapCodec<InvertedLootItemCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)LootItemCondition.CODEC.fieldOf("term").forGetter(InvertedLootItemCondition::term)).apply((Applicative)i, InvertedLootItemCondition::new));

    public MapCodec<InvertedLootItemCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        return !this.term.value().test(context);
    }

    @Override
    public void validate(ValidationContext output) {
        LootItemCondition.super.validate(output);
        Validatable.validateHolder(output, "term", this.term);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder term) {
        return InvertedLootItemCondition.invert(Holder.direct(term.build()));
    }

    public static LootItemCondition.Builder invert(Holder<LootItemCondition> term) {
        InvertedLootItemCondition result = new InvertedLootItemCondition(term);
        return () -> result;
    }
}


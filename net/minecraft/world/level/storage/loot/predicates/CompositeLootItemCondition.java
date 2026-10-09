/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeLootItemCondition
implements LootItemCondition {
    protected final HolderSet<LootItemCondition> terms;
    private final Predicate<LootContext> composedPredicate;

    protected CompositeLootItemCondition(HolderSet<LootItemCondition> terms, Predicate<LootContext> composedPredicate) {
        this.terms = terms;
        this.composedPredicate = composedPredicate;
    }

    protected static List<Predicate<LootContext>> holdersToLazyPredicates(HolderSet<LootItemCondition> terms) {
        return terms.stream().map(CompositeLootItemCondition::holderToLazyPredicate).toList();
    }

    private static Predicate<LootContext> holderToLazyPredicate(Holder<LootItemCondition> h) {
        return context -> ((LootItemCondition)h.value()).test(context);
    }

    public abstract MapCodec<? extends CompositeLootItemCondition> codec();

    protected static <T extends CompositeLootItemCondition> MapCodec<T> createCodec(Function<HolderSet<LootItemCondition>, T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)LootItemCondition.LIST_CODEC.fieldOf("terms").forGetter(condition -> condition.terms)).apply((Applicative)i, factory));
    }

    @Override
    public final boolean test(LootContext context) {
        return this.composedPredicate.test(context);
    }

    @Override
    public void validate(ValidationContext output) {
        LootItemCondition.super.validate(output);
        Validatable.validateHolderSet(output, "terms", this.terms);
    }

    public static abstract class Builder
    implements LootItemCondition.Builder {
        private final ImmutableList.Builder<Holder<LootItemCondition>> terms = ImmutableList.builder();

        protected Builder(LootItemCondition.Builder ... terms) {
            for (LootItemCondition.Builder term : terms) {
                this.terms.add(Holder.direct(term.build()));
            }
        }

        public void addTerm(LootItemCondition.Builder term) {
            this.addTerm(Holder.direct(term.build()));
        }

        public void addTerm(Holder<LootItemCondition> term) {
            this.terms.add(term);
        }

        @Override
        public LootItemCondition build() {
            return this.create(HolderSet.direct(this.terms.build()));
        }

        protected abstract LootItemCondition create(HolderSet<LootItemCondition> var1);
    }
}


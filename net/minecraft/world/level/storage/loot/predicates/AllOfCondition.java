/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AllOfCondition
extends CompositeLootItemCondition {
    public static final MapCodec<AllOfCondition> MAP_CODEC = AllOfCondition.createCodec(AllOfCondition::new);

    private AllOfCondition(HolderSet<LootItemCondition> terms) {
        super(terms, AllOfCondition.combine(terms));
    }

    private static Predicate<LootContext> combine(HolderSet<LootItemCondition> terms) {
        if (!terms.isBound()) {
            return context -> {
                for (Holder entry : terms) {
                    if (((LootItemCondition)entry.value()).test(context)) continue;
                    return false;
                }
                return true;
            };
        }
        return Util.allOf(AllOfCondition.holdersToLazyPredicates(terms));
    }

    public static AllOfCondition allOf(HolderSet<LootItemCondition> terms) {
        return new AllOfCondition(terms);
    }

    public MapCodec<AllOfCondition> codec() {
        return MAP_CODEC;
    }

    public static Builder allOf(LootItemCondition.Builder ... terms) {
        return new Builder(terms);
    }

    public static class Builder
    extends CompositeLootItemCondition.Builder {
        public Builder(LootItemCondition.Builder ... terms) {
            super(terms);
        }

        @Override
        public Builder and(LootItemCondition.Builder term) {
            this.addTerm(term);
            return this;
        }

        @Override
        protected LootItemCondition create(HolderSet<LootItemCondition> terms) {
            return new AllOfCondition(terms);
        }
    }
}


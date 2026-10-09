/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AlternativesEntry
extends CompositeEntryBase {
    public static final MapCodec<AlternativesEntry> MAP_CODEC = AlternativesEntry.createCodec(AlternativesEntry::new);
    public static final ProblemReporter.Problem UNREACHABLE_PROBLEM = new ProblemReporter.Problem(){

        @Override
        public String description() {
            return "Unreachable entry!";
        }
    };

    public AlternativesEntry(List<LootPoolEntryContainer> children, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(children, condition, modifier);
    }

    public MapCodec<AlternativesEntry> codec() {
        return MAP_CODEC;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> entries) {
        return switch (entries.size()) {
            case 0 -> ALWAYS_FALSE;
            case 1 -> entries.get(0);
            case 2 -> entries.get(0).or(entries.get(1));
            default -> (context, output) -> {
                for (ComposableEntryContainer entry : entries) {
                    if (!entry.expand(context, output)) continue;
                    return true;
                }
                return false;
            };
        };
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        for (int i = 0; i < this.children.size() - 1; ++i) {
            if (!((LootPoolEntryContainer)this.children.get((int)i)).condition.isEmpty()) continue;
            context.reportProblem(UNREACHABLE_PROBLEM);
        }
    }

    public static Builder alternatives(LootPoolEntryContainer.Builder<?> ... entries) {
        return new Builder(entries);
    }

    public static <E> Builder alternatives(Collection<E> items, Function<E, LootPoolEntryContainer.Builder<?>> provider) {
        return new Builder((LootPoolEntryContainer.Builder[])items.stream().map(provider::apply).toArray(LootPoolEntryContainer.Builder[]::new));
    }

    public static class Builder
    extends CompositeEntryBase.Builder<AlternativesEntry, Builder> {
        public Builder(LootPoolEntryContainer.Builder<?> ... entries) {
            super(entries);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder otherwise(LootPoolEntryContainer.Builder<?> other) {
            this.addEntry(other);
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.build(AlternativesEntry::new);
        }
    }
}


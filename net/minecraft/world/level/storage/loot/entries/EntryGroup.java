/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup
extends CompositeEntryBase {
    public static final MapCodec<EntryGroup> MAP_CODEC = EntryGroup.createCodec(EntryGroup::new);

    public EntryGroup(List<LootPoolEntryContainer> children, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(children, condition, modifier);
    }

    public MapCodec<EntryGroup> codec() {
        return MAP_CODEC;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> entries) {
        return switch (entries.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> entries.get(0);
            case 2 -> {
                ComposableEntryContainer first = entries.get(0);
                ComposableEntryContainer second = entries.get(1);
                yield (context, output) -> {
                    first.expand(context, output);
                    second.expand(context, output);
                    return true;
                };
            }
            default -> (context, output) -> {
                for (ComposableEntryContainer entry : entries) {
                    entry.expand(context, output);
                }
                return true;
            };
        };
    }

    public static Builder list(LootPoolEntryContainer.Builder<?> ... entries) {
        return new Builder(entries);
    }

    public static class Builder
    extends CompositeEntryBase.Builder<EntryGroup, Builder> {
        public Builder(LootPoolEntryContainer.Builder<?> ... entries) {
            super(entries);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder append(LootPoolEntryContainer.Builder<?> other) {
            this.addEntry(other);
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.build(EntryGroup::new);
        }
    }
}


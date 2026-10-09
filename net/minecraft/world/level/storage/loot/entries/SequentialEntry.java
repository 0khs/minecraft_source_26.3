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

public class SequentialEntry
extends CompositeEntryBase {
    public static final MapCodec<SequentialEntry> MAP_CODEC = SequentialEntry.createCodec(SequentialEntry::new);

    public SequentialEntry(List<LootPoolEntryContainer> children, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(children, condition, modifier);
    }

    public MapCodec<SequentialEntry> codec() {
        return MAP_CODEC;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> entries) {
        return switch (entries.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> entries.get(0);
            case 2 -> entries.get(0).and(entries.get(1));
            default -> (context, output) -> {
                for (ComposableEntryContainer entry : entries) {
                    if (entry.expand(context, output)) continue;
                    return false;
                }
                return true;
            };
        };
    }

    public static Builder sequential(LootPoolEntryContainer.Builder<?> ... entries) {
        return new Builder(entries);
    }

    public static class Builder
    extends CompositeEntryBase.Builder<SequentialEntry, Builder> {
        public Builder(LootPoolEntryContainer.Builder<?> ... entries) {
            super(entries);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder then(LootPoolEntryContainer.Builder<?> other) {
            this.addEntry(other);
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.build(SequentialEntry::new);
        }
    }
}


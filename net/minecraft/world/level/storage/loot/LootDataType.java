/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot;

import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public record LootDataType<T extends Validatable>(ResourceKey<Registry<T>> registryKey, ContextGetter<T> contextGetter) {
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType(Registries.PREDICATE, ContextGetter.constant(LootContextParamSets.ALL_PARAMS));
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType(Registries.ITEM_MODIFIER, ContextGetter.constant(LootContextParamSets.ALL_PARAMS));
    public static final LootDataType<SlotSource> SLOT_SOURCE = new LootDataType(Registries.SLOT_SOURCE, ContextGetter.constant(LootContextParamSets.ALL_PARAMS));
    public static final LootDataType<LootTable> TABLE = new LootDataType<LootTable>(Registries.LOOT_TABLE, LootTable::getParamSet);
    public static final LootDataType<ContextFloatProvider> FLOAT_PROVIDER = new LootDataType(Registries.CONTEXT_FLOAT_PROVIDER, ContextGetter.constant(LootContextParamSets.ALL_PARAMS));
    public static final LootDataType<ContextIntProvider> INT_PROVIDER = new LootDataType(Registries.CONTEXT_INT_PROVIDER, ContextGetter.constant(LootContextParamSets.ALL_PARAMS));

    public void runValidation(ValidationContextSource contextSource, ResourceKey<T> key, T value) {
        ContextKeySet contextKeys = this.contextGetter.context(value);
        ValidationContext rootContext = contextSource.context(contextKeys).enterElement(new ProblemReporter.RootElementPathElement(key), key);
        value.validate(rootContext);
    }

    public void runValidation(ValidationContextSource contextSource, HolderLookup<T> lookup) {
        lookup.listElements().forEach(holder -> this.runValidation(contextSource, holder.key(), (Validatable)holder.value()));
    }

    public void runValidation(ValidationContextSource contextSource, HolderLookup.Provider registries) {
        HolderGetter registry = registries.lookupOrThrow(this.registryKey());
        this.runValidation(contextSource, (HolderLookup<T>)registry);
    }

    public void runValidationIfPresent(ValidationContextSource contextSource, HolderLookup.Provider registries) {
        registries.lookup(this.registryKey()).ifPresent(registry -> this.runValidation(contextSource, (HolderLookup<T>)registry));
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(PREDICATE, MODIFIER, SLOT_SOURCE, TABLE, FLOAT_PROVIDER, INT_PROVIDER);
    }

    @FunctionalInterface
    public static interface ContextGetter<T> {
        public ContextKeySet context(T var1);

        public static <T> ContextGetter<T> constant(ContextKeySet v) {
            return value -> v;
        }
    }
}


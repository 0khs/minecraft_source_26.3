/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.predicates;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
    default public T when(LootItemCondition.Builder builder) {
        return this.when(Holder.direct(builder.build()));
    }

    public T when(Holder<LootItemCondition> var1);

    default public <E> T when(Iterable<E> collection, Function<E, LootItemCondition.Builder> conditionProvider) {
        T result = this.unwrap();
        for (E value : collection) {
            result = result.when(conditionProvider.apply(value));
        }
        return result;
    }

    public T unwrap();

    public static Optional<Holder<LootItemCondition>> buildCondition(List<Holder<LootItemCondition>> conditions) {
        if (conditions.isEmpty()) {
            return Optional.empty();
        }
        if (conditions.size() == 1) {
            return Optional.of(conditions.getFirst());
        }
        return Optional.of(Holder.direct(AllOfCondition.allOf(HolderSet.direct(conditions))));
    }
}


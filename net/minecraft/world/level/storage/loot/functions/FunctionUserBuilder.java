/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;

public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
    default public T apply(LootItemFunction.Builder builder) {
        return this.apply(Holder.direct(builder.build()));
    }

    public T apply(Holder<LootItemFunction> var1);

    default public <E> T apply(Iterable<E> collection, Function<E, LootItemFunction.Builder> functionProvider) {
        T result = this.unwrap();
        for (E value : collection) {
            result = result.apply(functionProvider.apply(value));
        }
        return result;
    }

    default public <E> T apply(E[] collection, Function<E, LootItemFunction.Builder> functionProvider) {
        return this.apply(Arrays.asList(collection), functionProvider);
    }

    public T unwrap();

    public static Optional<Holder<LootItemFunction>> buildFunction(List<Holder<LootItemFunction>> conditions) {
        if (conditions.isEmpty()) {
            return Optional.empty();
        }
        if (conditions.size() == 1) {
            return Optional.of(conditions.getFirst());
        }
        return Optional.of(Holder.direct(SequenceFunction.of(conditions)));
    }
}


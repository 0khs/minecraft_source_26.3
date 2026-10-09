/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.Products$P1
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootItemConditionalFunction
implements LootItemFunction {
    protected final Optional<Holder<LootItemCondition>> condition;

    protected LootItemConditionalFunction(Optional<Holder<LootItemCondition>> condition) {
        this.condition = condition;
    }

    public abstract MapCodec<? extends LootItemConditionalFunction> codec();

    protected static <T extends LootItemConditionalFunction> Products.P1<RecordCodecBuilder.Mu<T>, Optional<Holder<LootItemCondition>>> commonFields(RecordCodecBuilder.Instance<T> i) {
        return i.group((App)LootItemCondition.CODEC.optionalFieldOf("condition").forGetter(f -> f.condition));
    }

    @Override
    public final ItemStack apply(ItemStack itemStack, LootContext context) {
        return this.condition.isEmpty() || this.condition.get().value().test(context) ? this.run(itemStack, context) : itemStack;
    }

    protected abstract ItemStack run(ItemStack var1, LootContext var2);

    @Override
    public void validate(ValidationContext context) {
        LootItemFunction.super.validate(context);
        Validatable.validateHolder(context, "condition", this.condition);
    }

    protected static Builder<?> simpleBuilder(Function<Optional<Holder<LootItemCondition>>, LootItemFunction> constructor) {
        return new DummyBuilder(constructor);
    }

    private static final class DummyBuilder
    extends Builder<DummyBuilder> {
        private final Function<Optional<Holder<LootItemCondition>>, LootItemFunction> constructor;

        public DummyBuilder(Function<Optional<Holder<LootItemCondition>>, LootItemFunction> constructor) {
            this.constructor = constructor;
        }

        @Override
        protected DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return this.constructor.apply(this.getCondition());
        }
    }

    public static abstract class Builder<T extends Builder<T>>
    implements LootItemFunction.Builder,
    ConditionUserBuilder<T> {
        private final ImmutableList.Builder<Holder<LootItemCondition>> conditions = ImmutableList.builder();

        @Override
        public T when(Holder<LootItemCondition> condition) {
            this.conditions.add(condition);
            return this.getThis();
        }

        @Override
        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected Optional<Holder<LootItemCondition>> getCondition() {
            return ConditionUserBuilder.buildCondition((List<Holder<LootItemCondition>>)this.conditions.build());
        }
    }
}


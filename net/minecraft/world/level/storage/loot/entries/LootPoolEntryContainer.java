/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.Products$P2
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolEntryContainer
implements ComposableEntryContainer,
Validatable {
    protected final Optional<Holder<LootItemCondition>> condition;
    protected final Optional<Holder<LootItemFunction>> modifier;

    protected LootPoolEntryContainer(Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        this.condition = condition;
        this.modifier = modifier;
    }

    protected static <T extends LootPoolEntryContainer> Products.P2<RecordCodecBuilder.Mu<T>, Optional<Holder<LootItemCondition>>, Optional<Holder<LootItemFunction>>> commonFields(RecordCodecBuilder.Instance<T> i) {
        return i.group((App)LootItemCondition.CODEC.optionalFieldOf("condition").forGetter(e -> e.condition), (App)LootItemFunctions.CODEC.optionalFieldOf("modifier").forGetter(e -> e.modifier));
    }

    @Override
    public void validate(ValidationContext output) {
        Validatable.validateHolder(output, "condition", this.condition);
        Validatable.validateHolder(output, "modifier", this.modifier);
    }

    protected abstract boolean expandRaw(LootContext var1, Consumer<LootPoolEntry> var2);

    @Override
    public final boolean expand(LootContext context, Consumer<LootPoolEntry> output) {
        if (!this.canRun(context)) {
            return false;
        }
        return this.expandRaw(context, this.adjustOutput(output));
    }

    private Consumer<LootPoolEntry> adjustOutput(Consumer<LootPoolEntry> output) {
        if (this.modifier.isEmpty()) {
            return output;
        }
        return rawEntry -> output.accept(new LootPoolEntry(){
            final /* synthetic */ LootPoolEntry val$rawEntry;
            final /* synthetic */ LootPoolEntryContainer this$0;
            {
                this.val$rawEntry = lootPoolEntry;
                LootPoolEntryContainer lootPoolEntryContainer = this$0;
                Objects.requireNonNull(lootPoolEntryContainer);
                this.this$0 = lootPoolEntryContainer;
            }

            @Override
            public int getWeight(float luck) {
                return this.val$rawEntry.getWeight(luck);
            }

            @Override
            public void createItemStack(Consumer<ItemStack> output, LootContext context) {
                this.val$rawEntry.createItemStack(LootItemFunction.decorate(this.this$0.modifier, output, context), context);
            }
        });
    }

    private boolean canRun(LootContext context) {
        return this.condition.isEmpty() || this.condition.get().value().test(context);
    }

    public abstract MapCodec<? extends LootPoolEntryContainer> codec();

    public static abstract class Builder<T extends Builder<T>>
    implements ConditionUserBuilder<T>,
    FunctionUserBuilder<T> {
        private final ImmutableList.Builder<Holder<LootItemCondition>> conditions = ImmutableList.builder();
        private final ImmutableList.Builder<Holder<LootItemFunction>> modifiers = ImmutableList.builder();

        protected abstract T getThis();

        @Override
        public T when(Holder<LootItemCondition> condition) {
            this.conditions.add(condition);
            return this.getThis();
        }

        @Override
        public T apply(Holder<LootItemFunction> function) {
            this.modifiers.add(function);
            return this.getThis();
        }

        @Override
        public final T unwrap() {
            return this.getThis();
        }

        protected Optional<Holder<LootItemCondition>> getCondition() {
            return ConditionUserBuilder.buildCondition((List<Holder<LootItemCondition>>)this.conditions.build());
        }

        protected Optional<Holder<LootItemFunction>> getModifier() {
            return FunctionUserBuilder.buildFunction((List<Holder<LootItemFunction>>)this.modifiers.build());
        }

        public AlternativesEntry.Builder otherwise(Builder<?> other) {
            return new AlternativesEntry.Builder(this, other);
        }

        public EntryGroup.Builder append(Builder<?> other) {
            return new EntryGroup.Builder(this, other);
        }

        public SequentialEntry.Builder then(Builder<?> other) {
            return new SequentialEntry.Builder(this, other);
        }

        public abstract LootPoolEntryContainer build();
    }
}


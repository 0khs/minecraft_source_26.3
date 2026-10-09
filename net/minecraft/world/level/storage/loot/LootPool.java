/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool
implements Validatable {
    public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(i -> i.group((App)LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(p -> p.entries), (App)LootItemCondition.CODEC.optionalFieldOf("condition").forGetter(p -> p.condition), (App)LootItemFunctions.CODEC.optionalFieldOf("modifier").forGetter(p -> p.modifier), (App)ContextIntProviders.CODEC.fieldOf("rolls").forGetter(p -> p.rolls), (App)ContextFloatProviders.CODEC.optionalFieldOf("bonus_rolls", ContextFloatProviders.exactly(0.0f)).forGetter(p -> p.bonusRolls)).apply((Applicative)i, LootPool::new));
    private final List<LootPoolEntryContainer> entries;
    private final Optional<Holder<LootItemCondition>> condition;
    private final Optional<Holder<LootItemFunction>> modifier;
    private final Holder<ContextIntProvider> rolls;
    private final Holder<ContextFloatProvider> bonusRolls;

    private LootPool(List<LootPoolEntryContainer> entries, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier, Holder<ContextIntProvider> rolls, Holder<ContextFloatProvider> bonusRolls) {
        this.entries = entries;
        this.condition = condition;
        this.modifier = modifier;
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
    }

    private void addRandomItem(Consumer<ItemStack> result, LootContext context) {
        RandomSource random = context.getRandom();
        ArrayList validEntries = Lists.newArrayList();
        MutableInt totalWeight = new MutableInt();
        for (LootPoolEntryContainer entry : this.entries) {
            entry.expand(context, e -> {
                int weight = e.getWeight(context.getLuck());
                if (weight > 0) {
                    validEntries.add(e);
                    totalWeight.add(weight);
                }
            });
        }
        int entryCount = validEntries.size();
        if (totalWeight.intValue() == 0 || entryCount == 0) {
            return;
        }
        if (entryCount == 1) {
            ((LootPoolEntry)validEntries.get(0)).createItemStack(result, context);
            return;
        }
        int index = random.nextInt(totalWeight.intValue());
        for (LootPoolEntry entry : validEntries) {
            if ((index -= entry.getWeight(context.getLuck())) >= 0) continue;
            entry.createItemStack(result, context);
            return;
        }
    }

    public void addRandomItems(Consumer<ItemStack> result, LootContext context) {
        if (this.condition.isPresent() && !this.condition.get().value().test(context)) {
            return;
        }
        Consumer<ItemStack> decoratedConsumer = LootItemFunction.decorate(this.modifier, result, context);
        int count = this.rolls.value().getInt(context) + Mth.floor(this.bonusRolls.value().getFloat(context) * context.getLuck());
        for (int i = 0; i < count; ++i) {
            this.addRandomItem(decoratedConsumer, context);
        }
    }

    @Override
    public void validate(ValidationContext output) {
        Validatable.validateHolder(output, "condition", this.condition);
        Validatable.validateHolder(output, "modifier", this.modifier);
        Validatable.validate(output, "entries", this.entries);
        Validatable.validateHolder(output, "rolls", this.rolls);
        Validatable.validateHolder(output, "bonus_rolls", this.bonusRolls);
    }

    public static Builder lootPool() {
        return new Builder();
    }

    public static class Builder
    implements FunctionUserBuilder<Builder>,
    ConditionUserBuilder<Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
        private final ImmutableList.Builder<Holder<LootItemCondition>> conditions = ImmutableList.builder();
        private final ImmutableList.Builder<Holder<LootItemFunction>> functions = ImmutableList.builder();
        private Holder<ContextIntProvider> rolls = ContextIntProviders.exactly(1);
        private Holder<ContextFloatProvider> bonusRolls = ContextFloatProviders.exactly(0.0f);

        public Builder setRolls(Holder<ContextIntProvider> rolls) {
            this.rolls = rolls;
            return this;
        }

        @Override
        public Builder unwrap() {
            return this;
        }

        public Builder setBonusRolls(Holder<ContextFloatProvider> bonusRolls) {
            this.bonusRolls = bonusRolls;
            return this;
        }

        public Builder add(LootPoolEntryContainer.Builder<?> entry) {
            this.entries.add((Object)entry.build());
            return this;
        }

        public Builder addAll(List<? extends UniformContainerBase.Builder<?>> entries) {
            for (LootPoolEntryContainer.Builder builder : entries) {
                this.add(builder);
            }
            return this;
        }

        @Override
        public Builder when(Holder<LootItemCondition> condition) {
            this.conditions.add(condition);
            return this;
        }

        @Override
        public Builder apply(Holder<LootItemFunction> function) {
            this.functions.add(function);
            return this;
        }

        public LootPool build() {
            return new LootPool((List<LootPoolEntryContainer>)this.entries.build(), ConditionUserBuilder.buildCondition((List<Holder<LootItemCondition>>)this.conditions.build()), FunctionUserBuilder.buildFunction((List<Holder<LootItemFunction>>)this.functions.build()), this.rolls, this.bonusRolls);
        }
    }
}


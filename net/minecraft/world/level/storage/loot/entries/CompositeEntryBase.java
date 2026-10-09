/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase
extends LootPoolEntryContainer {
    public static final ProblemReporter.Problem NO_CHILDREN_PROBLEM = new ProblemReporter.Problem(){

        @Override
        public String description() {
            return "Empty children list";
        }
    };
    protected final List<LootPoolEntryContainer> children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(List<LootPoolEntryContainer> children, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(condition, modifier);
        this.children = children;
        this.composedChildren = this.compose(children);
    }

    public abstract MapCodec<? extends CompositeEntryBase> codec();

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        if (this.children.isEmpty()) {
            context.reportProblem(NO_CHILDREN_PROBLEM);
        }
        Validatable.validate(context, "children", this.children);
    }

    protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> var1);

    @Override
    public final boolean expandRaw(LootContext context, Consumer<LootPoolEntry> output) {
        return this.composedChildren.expand(context, output);
    }

    public static <T extends CompositeEntryBase> MapCodec<T> createCodec(CompositeEntryConstructor<T> constructor) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)LootPoolEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(e -> e.children)).and(CompositeEntryBase.commonFields(i)).apply((Applicative)i, constructor::create));
    }

    @FunctionalInterface
    public static interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        public T create(List<LootPoolEntryContainer> var1, Optional<Holder<LootItemCondition>> var2, Optional<Holder<LootItemFunction>> var3);
    }

    public static abstract class Builder<T extends CompositeEntryBase, B extends Builder<T, B>>
    extends LootPoolEntryContainer.Builder<B> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

        public Builder(LootPoolEntryContainer.Builder<?> ... entries) {
            for (LootPoolEntryContainer.Builder<?> entry : entries) {
                this.entries.add((Object)entry.build());
            }
        }

        protected ImmutableList.Builder<LootPoolEntryContainer> addEntry(LootPoolEntryContainer.Builder<?> entry) {
            return this.entries.add((Object)entry.build());
        }

        protected T build(CompositeEntryConstructor<T> constructor) {
            return constructor.create((List<LootPoolEntryContainer>)this.entries.build(), this.getCondition(), this.getModifier());
        }
    }
}


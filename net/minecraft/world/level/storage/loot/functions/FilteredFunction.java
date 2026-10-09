/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction
extends LootItemConditionalFunction {
    public static final MapCodec<FilteredFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> FilteredFunction.commonFields(i).and(i.group((App)ItemPredicate.CODEC.fieldOf("item_filter").forGetter(f -> f.filter), (App)LootItemFunctions.CODEC.optionalFieldOf("on_pass").forGetter(f -> f.onPass), (App)LootItemFunctions.CODEC.optionalFieldOf("on_fail").forGetter(f -> f.onFail))).apply((Applicative)i, FilteredFunction::new));
    private final ItemPredicate filter;
    private final Optional<Holder<LootItemFunction>> onPass;
    private final Optional<Holder<LootItemFunction>> onFail;

    private FilteredFunction(Optional<Holder<LootItemCondition>> condition, ItemPredicate filter, Optional<Holder<LootItemFunction>> onPass, Optional<Holder<LootItemFunction>> onFail) {
        super(condition);
        this.filter = filter;
        this.onPass = onPass;
        this.onFail = onFail;
    }

    public MapCodec<FilteredFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        Optional<Holder<LootItemFunction>> function;
        Optional<Holder<LootItemFunction>> optional = function = this.filter.test(itemStack) ? this.onPass : this.onFail;
        if (function.isPresent()) {
            return (ItemStack)function.get().value().apply(itemStack, context);
        }
        return itemStack;
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validateHolder(context, "on_pass", this.onPass);
        Validatable.validateHolder(context, "on_fail", this.onFail);
    }

    public static Builder filtered(ItemPredicate predicate) {
        return new Builder(predicate);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ItemPredicate itemPredicate;
        private Optional<Holder<LootItemFunction>> onPass = Optional.empty();
        private Optional<Holder<LootItemFunction>> onFail = Optional.empty();

        private Builder(ItemPredicate itemPredicate) {
            this.itemPredicate = itemPredicate;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder onPass(LootItemFunction onPass) {
            this.onPass = Optional.of(Holder.direct(onPass));
            return this;
        }

        public Builder onFail(LootItemFunction onFail) {
            this.onFail = Optional.of(Holder.direct(onFail));
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new FilteredFunction(this.getCondition(), this.itemPredicate, this.onPass, this.onFail);
        }
    }
}


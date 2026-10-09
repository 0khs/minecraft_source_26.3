/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.trading;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jspecify.annotations.Nullable;

public class VillagerTrade
implements Validatable {
    public static final Codec<VillagerTrade> CODEC = RecordCodecBuilder.create(i -> i.group((App)TradeCost.CODEC.fieldOf("wants").forGetter(villagerTrade -> villagerTrade.wants), (App)TradeCost.CODEC.optionalFieldOf("additional_wants").forGetter(villagerTrade -> villagerTrade.additionalWants), (App)ItemStackTemplate.CODEC.fieldOf("gives").forGetter(villagerTrade -> villagerTrade.gives), (App)ContextIntProviders.CODEC.optionalFieldOf("max_uses", ContextIntProviders.exactly(4)).forGetter(villagerTrade -> villagerTrade.maxUses), (App)ContextIntProviders.CODEC.optionalFieldOf("xp", ContextIntProviders.exactly(1)).forGetter(villagerTrade -> villagerTrade.xp), (App)ContextFloatProviders.CODEC.optionalFieldOf("reputation_discount", ContextFloatProviders.exactly(0.0f)).forGetter(villagerTrade -> villagerTrade.reputationDiscount), (App)LootItemCondition.CODEC.optionalFieldOf("merchant_predicate").forGetter(villagerTrade -> villagerTrade.merchantPredicate), (App)LootItemFunctions.CODEC.optionalFieldOf("given_item_modifier").forGetter(villagerTrade -> villagerTrade.givenItemModifier), (App)RegistryCodecs.holderSet(Registries.ENCHANTMENT).optionalFieldOf("double_trade_price_enchantments").forGetter(villagerTrade -> villagerTrade.doubleTradePriceEnchantments)).apply((Applicative)i, VillagerTrade::new)).validate(Validatable.validatorForContext(LootContextParamSets.VILLAGER_TRADE));
    private final TradeCost wants;
    private final Optional<TradeCost> additionalWants;
    private final ItemStackTemplate gives;
    private final Optional<Holder<LootItemCondition>> merchantPredicate;
    private final Optional<Holder<LootItemFunction>> givenItemModifier;
    private final Holder<ContextIntProvider> maxUses;
    private final Holder<ContextIntProvider> xp;
    private final Holder<ContextFloatProvider> reputationDiscount;
    private final Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments;

    private VillagerTrade(TradeCost wants, Optional<TradeCost> additionalWants, ItemStackTemplate gives, Holder<ContextIntProvider> maxUses, Holder<ContextIntProvider> xp, Holder<ContextFloatProvider> reputationDiscount, Optional<Holder<LootItemCondition>> merchantPredicate, Optional<Holder<LootItemFunction>> givenItemModifier, Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments) {
        this.wants = wants;
        this.additionalWants = additionalWants;
        this.gives = gives;
        this.maxUses = maxUses;
        this.xp = xp;
        this.reputationDiscount = reputationDiscount;
        this.merchantPredicate = merchantPredicate;
        this.givenItemModifier = givenItemModifier;
        this.doubleTradePriceEnchantments = doubleTradePriceEnchantments;
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validate(context, "wants", this.wants);
        Validatable.validate(context, "additional_wants", this.additionalWants);
        Validatable.validateHolder(context, "max_uses", this.maxUses);
        Validatable.validateHolder(context, "reputation_discount", this.reputationDiscount);
        Validatable.validateHolder(context, "xp", this.xp);
        Validatable.validateHolder(context, "merchant_predicate", this.merchantPredicate);
        Validatable.validateHolder(context, "given_item_modifier", this.givenItemModifier);
    }

    public @Nullable MerchantOffer getOffer(LootContext lootContext) {
        ItemCost itemCost;
        if (this.merchantPredicate.isPresent() && !this.merchantPredicate.get().value().test(lootContext)) {
            return null;
        }
        ItemStack result = this.gives.create();
        int additionalCost = 0;
        if (this.givenItemModifier.isPresent() && (result = (ItemStack)this.givenItemModifier.get().value().apply(result, lootContext)).isEmpty()) {
            return null;
        }
        Integer additionalTradeCost = result.remove(DataComponents.ADDITIONAL_TRADE_COST);
        if (additionalTradeCost != null) {
            additionalCost += additionalTradeCost.intValue();
        }
        if (this.doubleTradePriceEnchantments.isPresent()) {
            HolderSet<Enchantment> enchantments = this.doubleTradePriceEnchantments.get();
            ItemEnchantments itemEnchantments = result.get(DataComponents.STORED_ENCHANTMENTS);
            if (itemEnchantments != null) {
                if (itemEnchantments.keySet().stream().anyMatch(enchantments::contains)) {
                    additionalCost *= 2;
                }
            }
        }
        if ((itemCost = this.wants.toItemCost(lootContext, additionalCost)).count() < 1) {
            return null;
        }
        Optional<ItemCost> additionalItemCost = this.additionalWants.map(tradeCost -> tradeCost.toItemCost(lootContext, 0));
        if (additionalItemCost.isPresent() && additionalItemCost.get().count() < 1) {
            return null;
        }
        return new MerchantOffer(itemCost, additionalItemCost, result, Math.max(this.maxUses.value().getInt(lootContext), 1), Math.max(this.xp.value().getInt(lootContext), 0), Math.max(this.reputationDiscount.value().getFloat(lootContext), 0.0f));
    }

    public static Builder builder(TradeCost wants, ItemStackTemplate gives, int maxUses, int xp, float reputationDiscount) {
        return new Builder(wants, gives, ContextIntProviders.exactly(maxUses), ContextIntProviders.exactly(xp), ContextFloatProviders.exactly(reputationDiscount));
    }

    public static Builder builder(TradeCost wants, TradeCost additionalWants, ItemStackTemplate gives, int maxUses, int xp, float reputationDiscount) {
        return new Builder(wants, gives, ContextIntProviders.exactly(maxUses), ContextIntProviders.exactly(xp), ContextFloatProviders.exactly(reputationDiscount)).additionalWants(additionalWants);
    }

    public static class Builder {
        private final TradeCost wants;
        private final ItemStackTemplate gives;
        private final Holder<ContextIntProvider> maxUses;
        private final Holder<ContextIntProvider> xp;
        private final Holder<ContextFloatProvider> reputationDiscount;
        private Optional<TradeCost> additionalWants = Optional.empty();
        private Optional<Holder<LootItemCondition>> merchantPredicate = Optional.empty();
        private Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments = Optional.empty();
        private final ImmutableList.Builder<Holder<LootItemFunction>> givenItemModifiers = ImmutableList.builder();

        public Builder(TradeCost wants, ItemStackTemplate gives, Holder<ContextIntProvider> maxUses, Holder<ContextIntProvider> xp, Holder<ContextFloatProvider> reputationDiscount) {
            this.wants = wants;
            this.gives = gives;
            this.maxUses = maxUses;
            this.xp = xp;
            this.reputationDiscount = reputationDiscount;
        }

        public Builder additionalWants(TradeCost additionalWants) {
            this.additionalWants = Optional.of(additionalWants);
            return this;
        }

        public Builder merchantPredicate(Holder<LootItemCondition> merchantPredicate) {
            this.merchantPredicate = Optional.of(merchantPredicate);
            return this;
        }

        public Builder addModifier(LootItemFunction.Builder function) {
            this.givenItemModifiers.add(Holder.direct(function.build()));
            return this;
        }

        @SafeVarargs
        public final Builder addModifiers(Holder<LootItemFunction> ... functions) {
            return this.addModifiers(Arrays.asList(functions));
        }

        public Builder addModifiers(Collection<Holder<LootItemFunction>> function) {
            this.givenItemModifiers.addAll(function);
            return this;
        }

        public Builder doubleTradePriceEnchantments(HolderSet<Enchantment> doubleTradePriceEnchantments) {
            this.doubleTradePriceEnchantments = Optional.of(doubleTradePriceEnchantments);
            return this;
        }

        public VillagerTrade build() {
            return new VillagerTrade(this.wants, this.additionalWants, this.gives, this.maxUses, this.xp, this.reputationDiscount, this.merchantPredicate, FunctionUserBuilder.buildFunction((List<Holder<LootItemFunction>>)this.givenItemModifiers.build()), this.doubleTradePriceEnchantments);
        }
    }
}


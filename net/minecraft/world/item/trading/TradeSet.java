/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.trading;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record TradeSet(HolderSet<VillagerTrade> trades, Holder<ContextIntProvider> amount, boolean allowDuplicates, Optional<Identifier> randomSequence) {
    public static final Codec<TradeSet> CODEC = RecordCodecBuilder.create(i -> i.group((App)RegistryCodecs.holderSet(Registries.VILLAGER_TRADE).fieldOf("trades").forGetter(TradeSet::trades), (App)ContextIntProviders.CODEC.fieldOf("amount").forGetter(TradeSet::amount), (App)Codec.BOOL.optionalFieldOf("allow_duplicates", (Object)false).forGetter(TradeSet::allowDuplicates), (App)Identifier.CODEC.optionalFieldOf("random_sequence").forGetter(TradeSet::randomSequence)).apply((Applicative)i, TradeSet::new));

    public int calculateNumberOfTrades(LootContext lootContext) {
        return this.amount.value().getInt(lootContext);
    }
}


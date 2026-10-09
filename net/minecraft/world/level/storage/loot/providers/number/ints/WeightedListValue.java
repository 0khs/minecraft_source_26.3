/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.DistributionProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record WeightedListValue(WeightedList<Holder<ContextIntProvider>> distribution) implements ContextIntProvider,
DistributionProvider<ContextIntProvider>
{
    public static final MapCodec<WeightedListValue> MAP_CODEC = DistributionProvider.mapCodec(ContextIntProviders.CODEC, WeightedListValue::new);

    @Override
    public int getIntUnsafe(LootContext context) {
        return this.distribution().getRandomOrThrow(context.getRandom()).value().getIntUnsafe(context);
    }

    public MapCodec<WeightedListValue> codec() {
        return MAP_CODEC;
    }
}


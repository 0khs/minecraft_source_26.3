/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.DistributionProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record WeightedListValue(WeightedList<Holder<ContextFloatProvider>> distribution) implements ContextFloatProvider,
DistributionProvider<ContextFloatProvider>
{
    public static final MapCodec<WeightedListValue> MAP_CODEC = DistributionProvider.mapCodec(ContextFloatProviders.CODEC, WeightedListValue::new);

    @Override
    public float getFloatUnsafe(LootContext context) {
        return this.distribution().getRandomOrThrow(context.getRandom()).value().getFloatUnsafe(context);
    }

    public MapCodec<WeightedListValue> codec() {
        return MAP_CODEC;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.AggregateProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record Average(HolderSet<ContextIntProvider> inputs) implements ContextIntProvider,
AggregateProvider<ContextIntProvider>
{
    public static final MapCodec<Average> MAP_CODEC = AggregateProvider.mapCodec(ContextIntProviders.LIST_CODEC, Average::new);

    public MapCodec<Average> codec() {
        return MAP_CODEC;
    }

    @Override
    public int getIntUnsafe(LootContext context) throws ArithmeticException {
        long sum = 0L;
        long count = 0L;
        for (Holder holder : this.inputs()) {
            sum += (long)((ContextIntProvider)holder.value()).getIntUnsafe(context);
            ++count;
        }
        return ContextIntProvider.longToIntSafe(sum / count);
    }
}


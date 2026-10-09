/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.AggregateProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record Average(HolderSet<ContextFloatProvider> inputs) implements ContextFloatProvider,
AggregateProvider<ContextFloatProvider>
{
    public static final MapCodec<Average> MAP_CODEC = AggregateProvider.mapCodec(ContextFloatProviders.LIST_CODEC, Average::new);

    public MapCodec<Average> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        float sum = 0.0f;
        int count = 0;
        for (Holder holder : this.inputs()) {
            sum += ((ContextFloatProvider)holder.value()).getFloatUnsafe(context);
            ++count;
        }
        return sum / (float)count;
    }
}


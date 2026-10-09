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

public record Minimum(HolderSet<ContextIntProvider> inputs) implements ContextIntProvider,
AggregateProvider<ContextIntProvider>
{
    public static final MapCodec<Minimum> MAP_CODEC = AggregateProvider.mapCodec(ContextIntProviders.LIST_CODEC, Minimum::new);

    public MapCodec<Minimum> codec() {
        return MAP_CODEC;
    }

    @Override
    public int getIntUnsafe(LootContext context) {
        int value = Integer.MAX_VALUE;
        for (Holder holder : this.inputs()) {
            value = Math.min(value, ((ContextIntProvider)holder.value()).getIntUnsafe(context));
        }
        return value;
    }
}


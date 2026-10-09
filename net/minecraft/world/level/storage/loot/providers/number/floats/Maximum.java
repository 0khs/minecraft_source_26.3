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

public record Maximum(HolderSet<ContextFloatProvider> inputs) implements ContextFloatProvider,
AggregateProvider<ContextFloatProvider>
{
    public static final MapCodec<Maximum> MAP_CODEC = AggregateProvider.mapCodec(ContextFloatProviders.LIST_CODEC, Maximum::new);

    public MapCodec<Maximum> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        float value = -3.4028235E38f;
        for (Holder holder : this.inputs()) {
            value = Math.max(value, ((ContextFloatProvider)holder.value()).getFloatUnsafe(context));
        }
        return value;
    }
}


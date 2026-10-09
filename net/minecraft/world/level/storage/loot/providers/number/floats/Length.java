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
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.AggregateProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record Length(HolderSet<ContextFloatProvider> inputs) implements ContextFloatProvider,
AggregateProvider<ContextFloatProvider>
{
    public static final MapCodec<Length> MAP_CODEC = AggregateProvider.mapCodec(ContextFloatProviders.LIST_CODEC, Length::new);

    public MapCodec<Length> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        float sumOfSquares = 0.0f;
        for (Holder holder : this.inputs()) {
            float value = ((ContextFloatProvider)holder.value()).getFloatUnsafe(context);
            sumOfSquares += value * value;
        }
        return Mth.sqrt(sumOfSquares);
    }
}


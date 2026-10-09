/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.RangeProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record UniformGenerator(Holder<ContextFloatProvider> min, Holder<ContextFloatProvider> max) implements ContextFloatProvider,
RangeProvider<ContextFloatProvider>
{
    public static final MapCodec<UniformGenerator> MAP_CODEC = RangeProvider.mapCodec(ContextFloatProviders.CODEC, UniformGenerator::new);

    public MapCodec<UniformGenerator> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        return Mth.nextFloat(context.getRandom(), this.min().value().getFloatUnsafe(context), this.max().value().getFloatUnsafe(context));
    }
}


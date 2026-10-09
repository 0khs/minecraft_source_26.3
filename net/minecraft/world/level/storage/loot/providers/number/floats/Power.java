/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.PowerProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record Power(Holder<ContextFloatProvider> base, Holder<ContextFloatProvider> exponent) implements ContextFloatProvider,
PowerProvider<ContextFloatProvider>
{
    public static final MapCodec<Power> MAP_CODEC = PowerProvider.mapCodec(ContextFloatProviders.CODEC, Power::new);

    public MapCodec<Power> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        float base = this.base.value().getFloatUnsafe(context);
        float exponent = this.exponent.value().getFloatUnsafe(context);
        if (base == 0.0f && exponent == 0.0f) {
            return Float.NaN;
        }
        return (float)Math.pow(base, exponent);
    }
}


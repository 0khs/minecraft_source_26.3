/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.PowerProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record Power(Holder<ContextIntProvider> base, Holder<ContextIntProvider> exponent) implements ContextIntProvider,
PowerProvider<ContextIntProvider>
{
    public static final MapCodec<Power> MAP_CODEC = PowerProvider.mapCodec(ContextIntProviders.CODEC, Power::new);

    public MapCodec<Power> codec() {
        return MAP_CODEC;
    }

    @Override
    public int getIntUnsafe(LootContext context) throws ArithmeticException {
        int base = this.base.value().getIntUnsafe(context);
        int exponent = this.exponent.value().getIntUnsafe(context);
        if (base == 0 && exponent == 0) {
            throw new ArithmeticException("Result of 0 to the power of 0 is undefined");
        }
        return Math.powExact((int)base, (int)exponent);
    }
}


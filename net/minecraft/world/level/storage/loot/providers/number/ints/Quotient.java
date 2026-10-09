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
import net.minecraft.world.level.storage.loot.providers.number.BinaryProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record Quotient(Holder<ContextIntProvider> left, Holder<ContextIntProvider> right) implements ContextIntProvider,
BinaryProvider<ContextIntProvider>
{
    public static final MapCodec<Quotient> MAP_CODEC = BinaryProvider.mapCodec(ContextIntProviders.CODEC, Quotient::new);

    public MapCodec<Quotient> codec() {
        return MAP_CODEC;
    }

    @Override
    public int getIntUnsafe(LootContext context) throws ArithmeticException {
        return this.left().value().getIntUnsafe(context) / this.right().value().getIntUnsafe(context);
    }
}


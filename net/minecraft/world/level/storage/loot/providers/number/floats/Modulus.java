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
import net.minecraft.world.level.storage.loot.providers.number.BinaryProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record Modulus(Holder<ContextFloatProvider> left, Holder<ContextFloatProvider> right) implements ContextFloatProvider,
BinaryProvider<ContextFloatProvider>
{
    public static final MapCodec<Modulus> MAP_CODEC = BinaryProvider.mapCodec(ContextFloatProviders.CODEC, Modulus::new);

    public MapCodec<Modulus> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        float rightValue = this.right().value().getFloatUnsafe(context);
        if (rightValue == 0.0f) {
            return Float.NaN;
        }
        return this.left().value().getFloatUnsafe(context) % rightValue;
    }
}


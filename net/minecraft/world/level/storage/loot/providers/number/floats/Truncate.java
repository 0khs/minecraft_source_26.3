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
import net.minecraft.world.level.storage.loot.providers.number.UnaryProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record Truncate(Holder<ContextFloatProvider> input) implements ContextFloatProvider,
UnaryProvider<ContextFloatProvider>
{
    public static final MapCodec<Truncate> MAP_CODEC = UnaryProvider.codec(ContextFloatProviders.CODEC, Truncate::new);

    public MapCodec<Truncate> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        float value = this.input().value().getFloatUnsafe(context);
        return value > 0.0f ? (float)Math.floor(value) : (float)Math.ceil(value);
    }
}


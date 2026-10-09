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
import net.minecraft.world.level.storage.loot.providers.number.UnaryProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record Absolute(Holder<ContextIntProvider> input) implements ContextIntProvider,
UnaryProvider<ContextIntProvider>
{
    public static final MapCodec<Absolute> MAP_CODEC = UnaryProvider.codec(ContextIntProviders.CODEC, Absolute::new);

    public MapCodec<Absolute> codec() {
        return MAP_CODEC;
    }

    @Override
    public int getIntUnsafe(LootContext context) {
        return Math.absExact(this.input().value().getIntUnsafe(context));
    }
}


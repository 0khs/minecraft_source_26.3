/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.DispatcherProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record NumberDispatcher(List<DispatcherProvider.Case<ContextIntProvider>> cases, Holder<ContextIntProvider> defaultValue) implements ContextIntProvider,
DispatcherProvider<ContextIntProvider>
{
    public static final MapCodec<NumberDispatcher> MAP_CODEC = DispatcherProvider.mapCodec(ContextIntProviders.CODEC, ContextIntProviders.exactly(0), NumberDispatcher::new);

    @Override
    public int getIntUnsafe(LootContext context) {
        return ((ContextIntProvider)this.selectValue(context).value()).getIntUnsafe(context);
    }

    public MapCodec<NumberDispatcher> codec() {
        return MAP_CODEC;
    }
}


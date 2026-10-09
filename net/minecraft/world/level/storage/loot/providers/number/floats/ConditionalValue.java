/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConditionalProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record ConditionalValue(Holder<LootItemCondition> condition, Holder<ContextFloatProvider> onTrue, Holder<ContextFloatProvider> onFalse) implements ContextFloatProvider,
ConditionalProvider<ContextFloatProvider>
{
    public static final MapCodec<ConditionalValue> MAP_CODEC = ConditionalProvider.mapCodec(ContextFloatProviders.CODEC, ContextFloatProviders.exactly(0.0f), ConditionalValue::new);
    public static final Codec<ConditionalValue> CODEC = MAP_CODEC.codec();

    @Override
    public float getFloatUnsafe(LootContext context) {
        return ((ContextFloatProvider)this.selectValue(context).value()).getFloatUnsafe(context);
    }

    public MapCodec<ConditionalValue> codec() {
        return MAP_CODEC;
    }
}


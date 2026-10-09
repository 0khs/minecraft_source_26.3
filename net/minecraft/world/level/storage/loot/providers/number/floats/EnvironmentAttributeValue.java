/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.EnvironmentAttributeProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;

public record EnvironmentAttributeValue(EnvironmentAttribute<?> attribute) implements ContextFloatProvider,
EnvironmentAttributeProvider
{
    private static final Codec<EnvironmentAttribute<?>> ATTRIBUTE_CODEC = EnvironmentAttributes.CODEC.validate(attribute -> {
        if (attribute.type().toFloat() == null) {
            return DataResult.error(() -> String.valueOf(attribute) + " cannot be converted to a float");
        }
        return DataResult.success((Object)attribute);
    });
    public static final MapCodec<EnvironmentAttributeValue> MAP_CODEC = EnvironmentAttributeProvider.mapCodec(ATTRIBUTE_CODEC, EnvironmentAttributeValue::new);

    public MapCodec<EnvironmentAttributeValue> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloatUnsafe(LootContext context) {
        return EnvironmentAttributeValue.getAsFloat(context, this.attribute);
    }

    private static <Value> float getAsFloat(LootContext context, EnvironmentAttribute<Value> attribute) {
        Value value = context.getLevel().environmentAttributes().getValue(context, attribute);
        return attribute.type().toFloat(value);
    }
}


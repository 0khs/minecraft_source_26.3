/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public record ConstantValue(int value) implements ContextIntProvider
{
    public static final MapCodec<ConstantValue> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.fieldOf("value").forGetter(ConstantValue::value)).apply((Applicative)i, ConstantValue::new));
    public static final Codec<ConstantValue> INLINE_CODEC = Codec.INT.xmap(ConstantValue::new, ConstantValue::value);

    public MapCodec<ConstantValue> codec() {
        return MAP_CODEC;
    }

    @Override
    public void validate(ValidationContext context) {
    }

    @Override
    public int getIntUnsafe(LootContext random) {
        return this.value;
    }
}


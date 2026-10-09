/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.StoredNumberAccess;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public record StorageValue(StoredNumberAccess access, Holder<ContextFloatProvider> fallback) implements ContextFloatProvider
{
    public static final MapCodec<StorageValue> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)StoredNumberAccess.MAP_CODEC.forGetter(StorageValue::access), (App)ContextFloatProviders.CODEC.optionalFieldOf("fallback", ContextFloatProviders.exactly(0.0f)).forGetter(StorageValue::fallback)).apply((Applicative)i, StorageValue::new));

    @Override
    public float getFloatUnsafe(LootContext context) {
        Number value = this.access.getNumericTag(context);
        return value != null ? value.floatValue() : this.fallback.value().getFloatUnsafe(context);
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "fallback", this.fallback);
    }

    public MapCodec<StorageValue> codec() {
        return MAP_CODEC;
    }
}


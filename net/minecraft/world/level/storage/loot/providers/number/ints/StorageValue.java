/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.StoredNumberAccess;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record StorageValue(StoredNumberAccess access, Holder<ContextIntProvider> fallback) implements ContextIntProvider
{
    public static final MapCodec<StorageValue> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)StoredNumberAccess.MAP_CODEC.forGetter(StorageValue::access), (App)ContextIntProviders.CODEC.optionalFieldOf("fallback", ContextIntProviders.exactly(0)).forGetter(StorageValue::fallback)).apply((Applicative)i, StorageValue::new));

    @Override
    public int getIntUnsafe(LootContext context) {
        Number value = this.access.getNumericTag(context);
        return value != null ? value.intValue() : this.fallback.value().getIntUnsafe(context);
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "fallback", this.fallback);
    }

    public MapCodec<StorageValue> codec() {
        return MAP_CODEC;
    }
}


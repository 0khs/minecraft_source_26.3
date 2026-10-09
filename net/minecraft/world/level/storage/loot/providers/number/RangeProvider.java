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
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;

public interface RangeProvider<Value extends Validatable>
extends Validatable {
    public static <Value extends Validatable, Self extends RangeProvider<Value>> MapCodec<Self> mapCodec(Codec<Holder<Value>> valueCodec, Factory<Value, Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)valueCodec.fieldOf("min").forGetter(RangeProvider::min), (App)valueCodec.fieldOf("max").forGetter(RangeProvider::max)).apply((Applicative)i, factory::create));
    }

    public Holder<Value> min();

    public Holder<Value> max();

    @Override
    default public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "min", this.min());
        Validatable.validateHolder(context, "max", this.max());
    }

    @FunctionalInterface
    public static interface Factory<Value extends Validatable, Self extends RangeProvider<Value>> {
        public Self create(Holder<Value> var1, Holder<Value> var2);
    }
}


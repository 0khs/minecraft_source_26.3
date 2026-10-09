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
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;

public interface AggregateProvider<Value extends Validatable>
extends Validatable {
    public static <Value extends Validatable, Self extends AggregateProvider<Value>> MapCodec<Self> mapCodec(Codec<HolderSet<Value>> valueCodec, Factory<Value, Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)valueCodec.fieldOf("inputs").forGetter(AggregateProvider::inputs)).apply((Applicative)i, factory::create));
    }

    public HolderSet<Value> inputs();

    @Override
    default public void validate(ValidationContext context) {
        Validatable.validateHolderSet(context, "inputs", this.inputs(), 1);
    }

    @FunctionalInterface
    public static interface Factory<Value extends Validatable, Self extends AggregateProvider<Value>> {
        public Self create(HolderSet<Value> var1);
    }
}


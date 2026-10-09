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
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;

public interface DistributionProvider<Value extends Validatable>
extends Validatable {
    public WeightedList<Holder<Value>> distribution();

    public static <Value extends Validatable, Self extends DistributionProvider<Value>> MapCodec<Self> mapCodec(Codec<Holder<Value>> valueCodec, Factory<Value, Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)WeightedList.nonEmptyCodec(valueCodec).fieldOf("distribution").forGetter(DistributionProvider::distribution)).apply((Applicative)i, factory::create));
    }

    @Override
    default public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "distribution", this.distribution().unwrap().stream().map(Weighted::value).toList());
    }

    @FunctionalInterface
    public static interface Factory<Value extends Validatable, Self extends DistributionProvider<Value>> {
        public Self create(WeightedList<Holder<Value>> var1);
    }
}


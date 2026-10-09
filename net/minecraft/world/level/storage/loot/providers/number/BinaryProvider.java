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

public interface BinaryProvider<Value extends Validatable>
extends Validatable {
    public static <Value extends Validatable, Self extends BinaryProvider<Value>> MapCodec<Self> mapCodec(Codec<Holder<Value>> valueCodec, Factory<Value, Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)valueCodec.fieldOf("left").forGetter(BinaryProvider::left), (App)valueCodec.fieldOf("right").forGetter(BinaryProvider::right)).apply((Applicative)i, factory::create));
    }

    @Override
    default public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "left", this.left());
        Validatable.validateHolder(context, "right", this.right());
    }

    public Holder<Value> left();

    public Holder<Value> right();

    @FunctionalInterface
    public static interface Factory<Value extends Validatable, Self extends BinaryProvider<Value>> {
        public Self create(Holder<Value> var1, Holder<Value> var2);
    }
}


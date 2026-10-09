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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ConditionalProvider<Value extends Validatable>
extends Validatable {
    public static <Value extends Validatable, Self extends ConditionalProvider<Value>> MapCodec<Self> mapCodec(Codec<Holder<Value>> valueCodec, Holder<Value> defaultIfFalse, Factory<Value, Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)LootItemCondition.CODEC.fieldOf("condition").forGetter(ConditionalProvider::condition), (App)valueCodec.fieldOf("on_true").forGetter(ConditionalProvider::onTrue), (App)valueCodec.optionalFieldOf("on_false", (Object)defaultIfFalse).forGetter(ConditionalProvider::onFalse)).apply((Applicative)i, factory::create));
    }

    public Holder<LootItemCondition> condition();

    public Holder<Value> onTrue();

    public Holder<Value> onFalse();

    default public Holder<Value> selectValue(LootContext context) {
        if (!this.condition().value().test(context)) {
            return this.onFalse();
        }
        return this.onTrue();
    }

    @Override
    default public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "condition", this.condition());
        Validatable.validateHolder(context, "on_true", this.onTrue());
        Validatable.validateHolder(context, "on_false", this.onFalse());
    }

    @FunctionalInterface
    public static interface Factory<Value extends Validatable, Self extends ConditionalProvider<Value>> {
        public Self create(Holder<LootItemCondition> var1, Holder<Value> var2, Holder<Value> var3);
    }
}


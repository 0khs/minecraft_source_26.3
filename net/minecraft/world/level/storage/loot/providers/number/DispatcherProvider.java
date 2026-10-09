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
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface DispatcherProvider<Value extends Validatable>
extends Validatable {
    public static <Value extends Validatable, Self extends DispatcherProvider<Value>> MapCodec<Self> mapCodec(Codec<Holder<Value>> valueCodec, Holder<Value> defaultCase, Factory<Value, Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)Case.codec(valueCodec).listOf().fieldOf("cases").forGetter(DispatcherProvider::cases), (App)valueCodec.optionalFieldOf("default", (Object)defaultCase).forGetter(DispatcherProvider::defaultValue)).apply((Applicative)i, factory::create));
    }

    default public Holder<Value> selectValue(LootContext context) {
        for (Case<Value> aCase : this.cases()) {
            if (!aCase.test(context)) continue;
            return aCase.value;
        }
        return this.defaultValue();
    }

    public List<Case<Value>> cases();

    public Holder<Value> defaultValue();

    @Override
    default public void validate(ValidationContext context) {
        Validatable.validate(context, "cases", this.cases());
        Validatable.validateHolder(context, "default", this.defaultValue());
    }

    @FunctionalInterface
    public static interface Factory<Value extends Validatable, Self extends DispatcherProvider<Value>> {
        public Self create(List<Case<Value>> var1, Holder<Value> var2);
    }

    public record Case<Value extends Validatable>(Holder<LootItemCondition> condition, Holder<Value> value) implements Validatable
    {
        public static <Value extends Validatable> Codec<Case<Value>> codec(Codec<Holder<Value>> valueCodec) {
            return RecordCodecBuilder.create(i -> i.group((App)LootItemCondition.CODEC.fieldOf("condition").forGetter(Case::condition), (App)valueCodec.fieldOf("value").forGetter(Case::value)).apply((Applicative)i, Case::new));
        }

        public boolean test(LootContext context) {
            return this.condition.value().test(context);
        }

        @Override
        public void validate(ValidationContext context) {
            Validatable.validateHolder(context, "condition", this.condition);
            Validatable.validateHolder(context, "value", this.value);
        }
    }
}


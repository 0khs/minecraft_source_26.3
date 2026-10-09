/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.material.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;

public interface MaterialCondition {
    public static final Codec<MaterialCondition> DIRECT_CODEC = BuiltInRegistries.MATERIAL_CONDITION_TYPE.byNameCodec().dispatch(MaterialCondition::codec, Function.identity());
    public static final Codec<MaterialCondition> CODEC = RegistryCodecs.holder(Registries.MATERIAL_CONDITION, DIRECT_CODEC).xmap(holder -> {
        Holder holder2 = holder;
        Objects.requireNonNull(holder2);
        Holder selector0$temp = holder2;
        int index$1 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Holder.Direct.class, Holder.Reference.class}, (Holder)selector0$temp, index$1)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Holder.Direct direct = (Holder.Direct)selector0$temp;
                yield (MaterialCondition)direct.value();
            }
            case 1 -> {
                Holder.Reference reference = (Holder.Reference)selector0$temp;
                yield new HolderHolder(reference);
            }
        };
    }, value -> {
        Holder<MaterialCondition> holder;
        MaterialCondition materialCondition = value;
        Objects.requireNonNull(materialCondition);
        MaterialCondition selector1$temp = materialCondition;
        int index$2 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{HolderHolder.class}, (MaterialCondition)selector1$temp, index$2)) {
            case 0: {
                HolderHolder $b$0 = (HolderHolder)selector1$temp;
                try {
                    Holder<MaterialCondition> patt3$temp;
                    Holder<MaterialCondition> holder2 = patt3$temp = $b$0.holder();
                    holder = holder2;
                    return holder;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
        }
        holder = Holder.direct(value);
        return holder;
    });

    public ConditionEvaluator compile(MaterialRuleContext var1);

    public MapCodec<? extends MaterialCondition> codec();

    public record HolderHolder(Holder<MaterialCondition> holder) implements MaterialCondition
    {
        @Override
        public ConditionEvaluator compile(MaterialRuleContext context) {
            return this.holder.value().compile(context);
        }

        public MapCodec<HolderHolder> codec() {
            throw new UnsupportedOperationException("HolderHolder cannot be serialized");
        }
    }
}


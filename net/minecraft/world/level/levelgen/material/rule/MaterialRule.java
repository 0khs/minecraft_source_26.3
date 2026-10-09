/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.material.rule;

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
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public interface MaterialRule {
    public static final Codec<MaterialRule> DIRECT_CODEC = BuiltInRegistries.MATERIAL_RULE_TYPE.byNameCodec().dispatch(MaterialRule::codec, Function.identity());
    public static final Codec<Holder<MaterialRule>> HOLDER_CODEC = RegistryCodecs.holder(Registries.MATERIAL_RULE, DIRECT_CODEC);
    public static final Codec<MaterialRule> CODEC = HOLDER_CODEC.xmap(holder -> {
        Holder holder2 = holder;
        Objects.requireNonNull(holder2);
        Holder selector0$temp = holder2;
        int index$1 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Holder.Direct.class, Holder.Reference.class}, (Holder)selector0$temp, index$1)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Holder.Direct direct = (Holder.Direct)selector0$temp;
                yield (MaterialRule)direct.value();
            }
            case 1 -> {
                Holder.Reference reference = (Holder.Reference)selector0$temp;
                yield new HolderHolder(reference);
            }
        };
    }, value -> {
        Holder<MaterialRule> holder;
        MaterialRule materialRule = value;
        Objects.requireNonNull(materialRule);
        MaterialRule selector1$temp = materialRule;
        int index$2 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{HolderHolder.class}, (MaterialRule)selector1$temp, index$2)) {
            case 0: {
                HolderHolder $b$0 = (HolderHolder)selector1$temp;
                try {
                    Holder<MaterialRule> patt3$temp;
                    Holder<MaterialRule> holder2 = patt3$temp = $b$0.holder();
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

    public RuleEvaluator compile(MaterialRuleContext var1);

    public MapCodec<? extends MaterialRule> codec();

    public record HolderHolder(Holder<MaterialRule> holder) implements MaterialRule
    {
        @Override
        public RuleEvaluator compile(MaterialRuleContext context) {
            return this.holder.value().compile(context);
        }

        public MapCodec<HolderHolder> codec() {
            throw new UnsupportedOperationException("HolderHolder cannot be serialized");
        }
    }
}


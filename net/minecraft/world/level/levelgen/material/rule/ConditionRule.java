/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.material.rule;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public record ConditionRule(MaterialCondition ifTrue, MaterialRule thenRun) implements MaterialRule
{
    public static final MapCodec<ConditionRule> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)MaterialCondition.CODEC.fieldOf("if_true").forGetter(ConditionRule::ifTrue), (App)MaterialRule.CODEC.fieldOf("then_run").forGetter(ConditionRule::thenRun)).apply((Applicative)i, ConditionRule::new));

    public MapCodec<ConditionRule> codec() {
        return CODEC;
    }

    @Override
    public RuleEvaluator compile(MaterialRuleContext context) {
        ConditionEvaluator ifTrue = this.ifTrue.compile(context);
        RuleEvaluator thenRun = this.thenRun.compile(context);
        return (blockX, blockY, blockZ) -> {
            if (!ifTrue.test()) {
                return null;
            }
            return thenRun.tryApply(blockX, blockY, blockZ);
        };
    }
}


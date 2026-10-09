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
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public record SequenceRule(List<MaterialRule> sequence) implements MaterialRule
{
    public static final MapCodec<SequenceRule> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)MaterialRule.CODEC.listOf().fieldOf("sequence").forGetter(SequenceRule::sequence)).apply((Applicative)i, SequenceRule::new));

    public MapCodec<SequenceRule> codec() {
        return CODEC;
    }

    @Override
    public RuleEvaluator compile(MaterialRuleContext context) {
        if (this.sequence.size() == 1) {
            return this.sequence.getFirst().compile(context);
        }
        RuleEvaluator[] sequence = new RuleEvaluator[this.sequence.size()];
        for (int i = 0; i < sequence.length; ++i) {
            sequence[i] = this.sequence.get(i).compile(context);
        }
        return (blockX, blockY, blockZ) -> {
            for (RuleEvaluator rule : sequence) {
                BlockState state = rule.tryApply(blockX, blockY, blockZ);
                if (state == null) continue;
                return state;
            }
            return null;
        };
    }
}


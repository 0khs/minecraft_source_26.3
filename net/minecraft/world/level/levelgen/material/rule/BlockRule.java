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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public record BlockRule(BlockState resultState) implements MaterialRule,
RuleEvaluator
{
    public static final MapCodec<BlockRule> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("result_state").forGetter(BlockRule::resultState)).apply((Applicative)i, BlockRule::new));

    public MapCodec<BlockRule> codec() {
        return CODEC;
    }

    @Override
    public RuleEvaluator compile(MaterialRuleContext context) {
        return this;
    }

    @Override
    public BlockState tryApply(int blockX, int blockY, int blockZ) {
        return this.resultState;
    }
}


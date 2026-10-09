/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class NotRuleTest
extends RuleTest {
    public static final MapCodec<NotRuleTest> CODEC = RuleTest.CODEC.fieldOf("rule").xmap(NotRuleTest::new, t -> t.rule);
    private final RuleTest rule;

    public NotRuleTest(RuleTest rule) {
        this.rule = rule;
    }

    @Override
    public boolean test(BlockState blockState, BlockPos pos, RandomSource random) {
        return !this.rule.test(blockState, pos, random);
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.NOT_TEST;
    }
}


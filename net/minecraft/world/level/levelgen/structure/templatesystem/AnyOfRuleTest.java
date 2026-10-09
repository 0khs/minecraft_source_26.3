/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class AnyOfRuleTest
extends RuleTest {
    public static final MapCodec<AnyOfRuleTest> CODEC = RuleTest.CODEC.listOf().fieldOf("rules").xmap(AnyOfRuleTest::new, t -> t.rules);
    private final List<RuleTest> rules;

    public AnyOfRuleTest(List<RuleTest> rules) {
        this.rules = rules;
    }

    @Override
    public boolean test(BlockState blockState, BlockPos pos, RandomSource random) {
        for (RuleTest rule : this.rules) {
            if (!rule.test(blockState, pos, random)) continue;
            return true;
        }
        return false;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.ANY_OF_TEST;
    }
}


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

public class AllOfRuleTest
extends RuleTest {
    public static final MapCodec<AllOfRuleTest> CODEC = RuleTest.CODEC.listOf().fieldOf("rules").xmap(AllOfRuleTest::new, t -> t.rules);
    private final List<RuleTest> rules;

    public AllOfRuleTest(List<RuleTest> rules) {
        this.rules = rules;
    }

    @Override
    public boolean test(BlockState blockState, BlockPos pos, RandomSource random) {
        for (RuleTest rule : this.rules) {
            if (rule.test(blockState, pos, random)) continue;
            return false;
        }
        return true;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.ALL_OF_TEST;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AllOfRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.AnyOfRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.NotRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public abstract class RuleTest {
    public static final Codec<RuleTest> CODEC = BuiltInRegistries.RULE_TEST.byNameCodec().dispatch("predicate_type", RuleTest::getType, RuleTestType::codec);

    public boolean testAgainstWorldState(LevelReader level, BlockPos pos, RandomSource random) {
        return this.test(level.getBlockState(pos), pos, random);
    }

    public abstract boolean test(BlockState var1, BlockPos var2, RandomSource var3);

    protected abstract RuleTestType<?> getType();

    public static RuleTest allOf(List<RuleTest> predicates) {
        return new AllOfRuleTest(predicates);
    }

    public static RuleTest allOf(RuleTest ... predicates) {
        return RuleTest.allOf(List.of(predicates));
    }

    public static RuleTest anyOf(List<RuleTest> predicates) {
        return new AnyOfRuleTest(predicates);
    }

    public static RuleTest anyOf(RuleTest ... predicates) {
        return RuleTest.anyOf(List.of(predicates));
    }

    public static RuleTest not(RuleTest predicate) {
        return new NotRuleTest(predicate);
    }

    public static RuleTest either(RuleTest condition, RuleTest ifTrue, RuleTest ifFalse) {
        return RuleTest.anyOf(RuleTest.allOf(condition, ifTrue), RuleTest.allOf(RuleTest.not(condition), ifFalse));
    }
}


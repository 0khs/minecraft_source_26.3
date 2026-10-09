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
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class HeightMatchTest
extends RuleTest {
    public static final MapCodec<HeightMatchTest> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.fieldOf("min_inclusive").forGetter(c -> c.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter(c -> c.maxInclusive)).apply((Applicative)i, HeightMatchTest::new));
    private final int minInclusive;
    private final int maxInclusive;

    public HeightMatchTest(int minInclusive, int maxInclusive) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public static RuleTest min(int minInclusive) {
        return new HeightMatchTest(minInclusive, DimensionType.MAX_Y);
    }

    public static RuleTest max(int maxInclusive) {
        return new HeightMatchTest(DimensionType.MIN_Y, maxInclusive);
    }

    @Override
    public boolean test(BlockState blockState, BlockPos pos, RandomSource random) {
        return this.minInclusive <= pos.getY() && pos.getY() <= this.maxInclusive;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.HEIGHT_TEST;
    }
}


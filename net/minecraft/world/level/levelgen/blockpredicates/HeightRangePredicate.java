/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public record HeightRangePredicate(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) implements BlockPredicate
{
    public static final MapCodec<HeightRangePredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(HeightRangePredicate::minInclusive), (App)VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(HeightRangePredicate::maxInclusive)).apply((Applicative)i, HeightRangePredicate::new));

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.HEIGHT_RANGE;
    }

    @Override
    public boolean test(LevelAccessor level, BlockPos pos) {
        WorldGenerationContext context = WorldGenerationContext.of(level);
        int min = this.minInclusive.resolveY(context);
        int max = this.maxInclusive.resolveY(context);
        return pos.getY() >= min && pos.getY() <= max;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public record BlockPredicateFilter(BlockPredicate predicate) implements PlacementFilter
{
    public static final MapCodec<BlockPredicateFilter> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockPredicate.CODEC.fieldOf("predicate").forGetter(BlockPredicateFilter::predicate)).apply((Applicative)i, BlockPredicateFilter::new));

    public static BlockPredicateFilter forPredicate(BlockPredicate predicate) {
        return new BlockPredicateFilter(predicate);
    }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        return this.predicate.test(context.getLevel(), origin);
    }

    public MapCodec<BlockPredicateFilter> codec() {
        return CODEC;
    }
}


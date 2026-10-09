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
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record EnvironmentScanPlacement(Direction directionOfSearch, BlockPredicate targetCondition, BlockPredicate allowedSearchCondition, int maxSteps) implements PlacementModifier
{
    public static final MapCodec<EnvironmentScanPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Direction.VERTICAL_CODEC.fieldOf("direction_of_search").forGetter(EnvironmentScanPlacement::directionOfSearch), (App)BlockPredicate.CODEC.fieldOf("target_condition").forGetter(EnvironmentScanPlacement::targetCondition), (App)BlockPredicate.CODEC.optionalFieldOf("allowed_search_condition", (Object)BlockPredicate.alwaysTrue()).forGetter(EnvironmentScanPlacement::allowedSearchCondition), (App)Codec.intRange((int)1, (int)32).fieldOf("max_steps").forGetter(EnvironmentScanPlacement::maxSteps)).apply((Applicative)i, EnvironmentScanPlacement::new));

    public static EnvironmentScanPlacement scanningFor(Direction directionOfSearch, BlockPredicate targetCondition, BlockPredicate allowedSearchCondition, int maxSteps) {
        return new EnvironmentScanPlacement(directionOfSearch, targetCondition, allowedSearchCondition, maxSteps);
    }

    public static EnvironmentScanPlacement scanningFor(Direction directionOfSearch, BlockPredicate targetCondition, int maxSteps) {
        return EnvironmentScanPlacement.scanningFor(directionOfSearch, targetCondition, BlockPredicate.alwaysTrue(), maxSteps);
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        BlockPos.MutableBlockPos pos = origin.mutable();
        WorldGenLevel level = context.getLevel();
        if (!this.allowedSearchCondition.test(level, pos)) {
            return;
        }
        for (int i = 0; i < this.maxSteps; ++i) {
            if (this.targetCondition.test(level, pos)) {
                output.accept(pos);
                return;
            }
            pos.move(this.directionOfSearch);
            if (level.isOutsideBuildHeight(pos.getY())) {
                return;
            }
            if (!this.allowedSearchCondition.test(level, pos)) break;
        }
        if (this.targetCondition.test(level, pos)) {
            output.accept(pos);
        }
    }

    public MapCodec<EnvironmentScanPlacement> codec() {
        return CODEC;
    }
}


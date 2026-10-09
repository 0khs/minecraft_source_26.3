/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class PoplarTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<PoplarTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(i -> PoplarTrunkPlacer.trunkPlacerParts(i).and(i.group((App)IntProviders.codec(0, 8).fieldOf("trunk_height_above_branches").forGetter(t -> t.trunkHeightAboveBranches), (App)IntProviders.codec(1, 4).fieldOf("branch_amount").forGetter(t -> t.branchAmount))).apply((Applicative)i, PoplarTrunkPlacer::new));
    private final IntProvider trunkHeightAboveBranches;
    private final IntProvider branchAmount;

    public PoplarTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, IntProvider trunkHeightAboveBranches, IntProvider branchAmount) {
        super(baseHeight, heightRandA, heightRandB);
        this.trunkHeightAboveBranches = trunkHeightAboveBranches;
        this.branchAmount = branchAmount;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.POPLAR_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int treeHeight, BlockPos origin, TreeFeature tree) {
        PoplarTrunkPlacer.placeBelowTrunkBlock(level, trunkSetter, random, origin.below(), tree);
        int trunkHeightUpToFoliageBranches = treeHeight - this.trunkHeightAboveBranches.sample(random);
        for (int y = 0; y < treeHeight; ++y) {
            this.placeLog(level, trunkSetter, random, origin.above(y), tree);
            List<Direction> directions = PoplarTrunkPlacer.getShuffledBranchDirections(random);
            if (trunkHeightUpToFoliageBranches - 1 != y) continue;
            int branches = this.branchAmount.sample(random);
            for (int x = 0; x < branches; ++x) {
                Direction branchDirection = directions.get(x);
                this.placeLog(level, trunkSetter, random, origin.above(y).relative(branchDirection, 1), tree, PoplarTrunkPlacer.getSidewaysStateModifier(branchDirection));
            }
        }
        return List.of(new FoliagePlacer.FoliageAttachment(origin.above(trunkHeightUpToFoliageBranches), 0, false));
    }

    private static Function<BlockState, BlockState> getSidewaysStateModifier(Direction branchDirection) {
        return state -> (BlockState)state.trySetValue(RotatedPillarBlock.AXIS, branchDirection.getAxis());
    }

    private static List<Direction> getShuffledBranchDirections(RandomSource random) {
        return Direction.allShuffled(random).stream().filter(direction -> !direction.getAxis().isVertical()).collect(Collectors.toList());
    }
}


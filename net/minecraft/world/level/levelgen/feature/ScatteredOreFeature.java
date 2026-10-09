/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class ScatteredOreFeature
extends AbstractOreFeature {
    private static final int MAX_DIST_FROM_ORIGIN = 7;
    public static final MapCodec<ScatteredOreFeature> CODEC = ScatteredOreFeature.makeCodec(ScatteredOreFeature::new);

    public ScatteredOreFeature(List<BlockReplacement> targetStates, int size, float discardChanceOnAirExposure) {
        super(targetStates, size, discardChanceOnAirExposure);
    }

    public ScatteredOreFeature(RuleTest target, BlockState state, int size, float discardChanceOnAirExposure) {
        this(List.of(BlockReplacement.replace(target, state)), size, discardChanceOnAirExposure);
    }

    public MapCodec<ScatteredOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int numberOfTries = random.nextInt(this.size + 1);
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        block0: for (int i = 0; i < numberOfTries; ++i) {
            this.offsetTargetPos(targetPos, random, origin, Math.min(i, 7));
            BlockState blockState = level.getBlockState(targetPos);
            for (BlockReplacement targetState : this.targetStates) {
                if (!this.canPlaceOre(blockState, level::getBlockState, random, targetState, targetPos)) continue;
                level.setBlock(targetPos, targetState.state(), 2);
                continue block0;
            }
        }
        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos targetPos, RandomSource random, BlockPos origin, int maxDistFromOriginForThisTry) {
        int xd = this.getRandomPlacementInOneAxisRelativeToOrigin(random, maxDistFromOriginForThisTry);
        int yd = this.getRandomPlacementInOneAxisRelativeToOrigin(random, maxDistFromOriginForThisTry);
        int zd = this.getRandomPlacementInOneAxisRelativeToOrigin(random, maxDistFromOriginForThisTry);
        targetPos.setWithOffset(origin, xd, yd, zd);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource random, int maxDistanceFromOrigin) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float)maxDistanceFromOrigin);
    }
}


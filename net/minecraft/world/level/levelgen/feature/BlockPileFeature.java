/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record BlockPileFeature(Holder<BlockStateProvider> stateProvider) implements Feature
{
    public static final MapCodec<BlockPileFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(BlockPileFeature::stateProvider)).apply((Applicative)i, BlockPileFeature::new));

    public MapCodec<BlockPileFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (origin.getY() < level.getMinY() + 5) {
            return false;
        }
        int xr = 2 + random.nextInt(2);
        int zr = 2 + random.nextInt(2);
        for (BlockPos blockPos : BlockPos.betweenClosed(origin.offset(-xr, 0, -zr), origin.offset(xr, 1, zr))) {
            int zd;
            int xd = origin.getX() - blockPos.getX();
            if ((float)(xd * xd + (zd = origin.getZ() - blockPos.getZ()) * zd) <= random.nextFloat() * 10.0f - random.nextFloat() * 6.0f) {
                this.tryPlaceBlock(level, blockPos, random);
                continue;
            }
            if (!((double)random.nextFloat() < 0.031)) continue;
            this.tryPlaceBlock(level, blockPos, random);
        }
        return true;
    }

    private boolean mayPlaceOn(LevelAccessor level, BlockPos blockPos, RandomSource random) {
        BlockPos below = blockPos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.is(Blocks.DIRT_PATH)) {
            return random.nextBoolean();
        }
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }

    private void tryPlaceBlock(WorldGenLevel level, BlockPos blockPos, RandomSource random) {
        if (level.isEmptyBlock(blockPos) && this.mayPlaceOn(level, blockPos, random)) {
            level.setBlock(blockPos, this.stateProvider.value().getState(level, random, blockPos), 260);
        }
    }
}


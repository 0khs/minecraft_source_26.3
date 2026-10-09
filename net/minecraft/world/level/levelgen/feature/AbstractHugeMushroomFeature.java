/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public interface AbstractHugeMushroomFeature
extends Feature {
    public static final int MIN_MUSHROOM_HEIGHT = 4;

    public Holder<BlockStateProvider> capProvider();

    public Holder<BlockStateProvider> stemProvider();

    public int foliageRadius();

    public BlockPredicate canPlaceOn();

    public MapCodec<? extends AbstractHugeMushroomFeature> codec();

    default public void placeTrunk(WorldGenLevel level, RandomSource random, BlockPos origin, int treeHeight, BlockPos.MutableBlockPos blockPos) {
        for (int dy = 0; dy < treeHeight; ++dy) {
            blockPos.set(origin).move(Direction.UP, dy);
            this.placeMushroomBlock(level, blockPos, this.stemProvider().value().getState(level, random, origin));
        }
    }

    default public void placeMushroomBlock(LevelAccessor level, BlockPos.MutableBlockPos blockPos, BlockState newState) {
        BlockState currentState = level.getBlockState(blockPos);
        if (currentState.isAir() || currentState.is(BlockTags.REPLACEABLE_BY_MUSHROOMS)) {
            this.setBlock(level, blockPos, newState);
        }
    }

    default public int getTreeHeight(RandomSource random) {
        int treeHeight = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            treeHeight *= 2;
        }
        return treeHeight;
    }

    default public boolean isValidPosition(WorldGenLevel level, BlockPos origin, int treeHeight, BlockPos.MutableBlockPos blockPos) {
        int y = origin.getY();
        if (y < level.getMinY() + 1 || y + treeHeight + 1 > level.getMaxY()) {
            return false;
        }
        if (!this.canPlaceOn().test(level, origin.below())) {
            return false;
        }
        for (int dy = 0; dy <= treeHeight; ++dy) {
            int radius = this.getTreeRadiusForHeight(-1, -1, this.foliageRadius(), dy);
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    BlockState state = level.getBlockState(blockPos.setWithOffset(origin, dx, dy, dz));
                    if (state.isAir() || state.is(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    default public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos.MutableBlockPos blockPos;
        int treeHeight = this.getTreeHeight(random);
        if (!this.isValidPosition(level, origin, treeHeight, blockPos = new BlockPos.MutableBlockPos())) {
            return false;
        }
        this.makeCap(level, random, origin, treeHeight, blockPos);
        this.placeTrunk(level, random, origin, treeHeight, blockPos);
        return true;
    }

    public int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    public void makeCap(WorldGenLevel var1, RandomSource var2, BlockPos var3, int var4, BlockPos.MutableBlockPos var5);
}


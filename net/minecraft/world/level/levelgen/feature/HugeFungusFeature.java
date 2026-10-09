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
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;

public record HugeFungusFeature(BlockState validBaseState, BlockState stemState, BlockState hatState, BlockState decorState, BlockPredicate replaceableBlocks, boolean planted) implements Feature
{
    private static final float HUGE_PROBABILITY = 0.06f;
    public static final MapCodec<HugeFungusFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("valid_base_block").forGetter(HugeFungusFeature::validBaseState), (App)BlockState.CODEC.fieldOf("stem_state").forGetter(HugeFungusFeature::stemState), (App)BlockState.CODEC.fieldOf("hat_state").forGetter(HugeFungusFeature::hatState), (App)BlockState.CODEC.fieldOf("decor_state").forGetter(HugeFungusFeature::decorState), (App)BlockPredicate.CODEC.fieldOf("replaceable_blocks").forGetter(HugeFungusFeature::replaceableBlocks), (App)Codec.BOOL.optionalFieldOf("planted", (Object)false).forGetter(HugeFungusFeature::planted)).apply((Applicative)i, HugeFungusFeature::new));

    public MapCodec<HugeFungusFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block allowedBaseBlock = this.validBaseState.getBlock();
        BlockPos newOrigin = null;
        BlockState belowState = level.getBlockState(origin.below());
        if (belowState.is(allowedBaseBlock)) {
            newOrigin = origin;
        }
        if (newOrigin == null) {
            return false;
        }
        int totalHeight = Mth.nextInt(random, 4, 13);
        if (random.nextInt(12) == 0) {
            totalHeight *= 2;
        }
        if (!this.planted()) {
            int maxHeight = chunkGenerator.getGenDepth();
            if (newOrigin.getY() + totalHeight + 1 >= maxHeight) {
                return false;
            }
        }
        boolean isHuge = !this.planted() && random.nextFloat() < 0.06f;
        level.setBlock(origin, Blocks.AIR.defaultBlockState(), 260);
        this.placeStem(level, random, newOrigin, totalHeight, isHuge);
        this.placeHat(level, random, newOrigin, totalHeight, isHuge);
        return true;
    }

    private boolean isReplaceable(WorldGenLevel level, BlockPos pos, boolean checkNonReplaceablePlants) {
        if (level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
            return true;
        }
        if (checkNonReplaceablePlants) {
            return this.replaceableBlocks.test(level, pos);
        }
        return false;
    }

    private void placeStem(WorldGenLevel level, RandomSource random, BlockPos surfaceOrigin, int totalHeight, boolean isHuge) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        int stemRadius = isHuge ? 1 : 0;
        for (int dx = -stemRadius; dx <= stemRadius; ++dx) {
            for (int dz = -stemRadius; dz <= stemRadius; ++dz) {
                boolean cornerOfHugeStem = isHuge && Mth.abs(dx) == stemRadius && Mth.abs(dz) == stemRadius;
                for (int dy = 0; dy < totalHeight; ++dy) {
                    blockPos.setWithOffset(surfaceOrigin, dx, dy, dz);
                    if (!this.isReplaceable(level, blockPos, true)) continue;
                    if (this.planted) {
                        if (!level.getBlockState((BlockPos)blockPos.below()).isAir()) {
                            level.destroyBlock(blockPos, true);
                        }
                        level.setBlockAndUpdate(blockPos, this.stemState);
                        continue;
                    }
                    if (cornerOfHugeStem) {
                        if (!(random.nextFloat() < 0.1f)) continue;
                        this.setBlock(level, blockPos, this.stemState);
                        continue;
                    }
                    this.setBlock(level, blockPos, this.stemState);
                }
            }
        }
    }

    private void placeHat(WorldGenLevel level, RandomSource random, BlockPos surfaceOrigin, int totalHeight, boolean isHuge) {
        int hatStartY;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        boolean placeVines = this.hatState.is(Blocks.NETHER_WART_BLOCK);
        int hatHeight = Math.min(random.nextInt(1 + totalHeight / 3) + 5, totalHeight);
        for (int dy = hatStartY = totalHeight - hatHeight; dy <= totalHeight; ++dy) {
            int radius;
            int n = radius = dy < totalHeight - random.nextInt(3) ? 2 : 1;
            if (hatHeight > 8 && dy < hatStartY + 4) {
                radius = 3;
            }
            if (isHuge) {
                ++radius;
            }
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    boolean isEdgeX = dx == -radius || dx == radius;
                    boolean isEdgeZ = dz == -radius || dz == radius;
                    boolean inside = !isEdgeX && !isEdgeZ && dy != totalHeight;
                    boolean corner = isEdgeX && isEdgeZ;
                    boolean isHatBottom = dy < hatStartY + 3;
                    blockPos.setWithOffset(surfaceOrigin, dx, dy, dz);
                    if (!this.isReplaceable(level, blockPos, false)) continue;
                    if (this.planted && !level.getBlockState((BlockPos)blockPos.below()).isAir()) {
                        level.destroyBlock(blockPos, true);
                    }
                    if (isHatBottom) {
                        if (inside) continue;
                        this.placeHatDropBlock(level, random, blockPos, placeVines);
                        continue;
                    }
                    if (inside) {
                        this.placeHatBlock(level, random, blockPos, 0.1f, 0.2f, placeVines ? 0.1f : 0.0f);
                        continue;
                    }
                    if (corner) {
                        this.placeHatBlock(level, random, blockPos, 0.01f, 0.7f, placeVines ? 0.083f : 0.0f);
                        continue;
                    }
                    this.placeHatBlock(level, random, blockPos, 5.0E-4f, 0.98f, placeVines ? 0.07f : 0.0f);
                }
            }
        }
    }

    private void placeHatBlock(LevelAccessor level, RandomSource random, BlockPos.MutableBlockPos blockPos, float decorBlockProbability, float hatBlockProbability, float vinesProbability) {
        if (random.nextFloat() < decorBlockProbability) {
            this.setBlock(level, blockPos, this.decorState);
        } else if (random.nextFloat() < hatBlockProbability) {
            this.setBlock(level, blockPos, this.hatState);
            if (random.nextFloat() < vinesProbability) {
                HugeFungusFeature.tryPlaceWeepingVines(blockPos, level, random);
            }
        }
    }

    private void placeHatDropBlock(LevelAccessor level, RandomSource random, BlockPos blockPos, boolean placeVines) {
        if (level.getBlockState(blockPos.below()).is(this.hatState.getBlock())) {
            this.setBlock(level, blockPos, this.hatState);
        } else if ((double)random.nextFloat() < 0.15) {
            this.setBlock(level, blockPos, this.hatState);
            if (placeVines && random.nextInt(11) == 0) {
                HugeFungusFeature.tryPlaceWeepingVines(blockPos, level, random);
            }
        }
    }

    private static void tryPlaceWeepingVines(BlockPos hatBlockPos, LevelAccessor level, RandomSource random) {
        BlockPos.MutableBlockPos placePos = hatBlockPos.mutable().move(Direction.DOWN);
        if (!level.isEmptyBlock(placePos)) {
            return;
        }
        int goalVineHeight = Mth.nextInt(random, 1, 5);
        if (random.nextInt(7) == 0) {
            goalVineHeight *= 2;
        }
        int minVineAge = 23;
        int maxVineAge = 25;
        HugeFungusFeature.placeWeepingVinesColumn(level, random, placePos, goalVineHeight, 23, 25);
    }

    private static void placeWeepingVinesColumn(LevelAccessor level, RandomSource random, BlockPos origin, int totalHeight, int minAge, int naxAge) {
        BlockPos.MutableBlockPos placePos = origin.mutable();
        for (int height = 0; height <= totalHeight; ++height) {
            if (level.isEmptyBlock(placePos)) {
                if (height == totalHeight || !level.isEmptyBlock((BlockPos)placePos.below())) {
                    level.setBlock(placePos, (BlockState)Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Mth.nextInt(random, minAge, naxAge)), 2);
                    break;
                }
                level.setBlock(placePos, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
            }
            placePos.move(Direction.DOWN);
        }
    }
}


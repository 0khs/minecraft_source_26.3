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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record VinesFeature() implements Feature
{
    public static final MapCodec<VinesFeature> CODEC = MapCodec.unit(VinesFeature::new);

    public MapCodec<VinesFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!level.isEmptyBlock(origin)) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || !VineBlock.isAcceptableNeighbour(level, origin.relative(direction), direction)) continue;
            level.setBlock(origin, (BlockState)Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), true), 2);
            return true;
        }
        return false;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record EndPlatformFeature() implements Feature
{
    public static final MapCodec<EndPlatformFeature> CODEC = MapCodec.unit(EndPlatformFeature::new);

    public MapCodec<EndPlatformFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        EndPlatformFeature.createEndPlatform(level, origin, false);
        return true;
    }

    public static void createEndPlatform(ServerLevelAccessor newLevel, BlockPos origin, boolean dropResources) {
        BlockPos.MutableBlockPos pos = origin.mutable();
        for (int dz = -2; dz <= 2; ++dz) {
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dy = -1; dy < 3; ++dy) {
                    Block block;
                    BlockPos.MutableBlockPos blockPos = pos.set(origin).move(dx, dy, dz);
                    Block block2 = block = dy == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
                    if (newLevel.getBlockState(blockPos).is(block)) continue;
                    if (dropResources) {
                        newLevel.destroyBlock(blockPos, true, null);
                    }
                    newLevel.setBlockAndUpdate(blockPos, block.defaultBlockState());
                }
            }
        }
    }
}


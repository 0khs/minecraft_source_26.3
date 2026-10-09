/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ChorusPlantFeature() implements Feature
{
    public static final MapCodec<ChorusPlantFeature> CODEC = MapCodec.unit(ChorusPlantFeature::new);

    public MapCodec<ChorusPlantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (level.isEmptyBlock(origin) && level.getBlockState(origin.below()).is(BlockTags.SUPPORTS_CHORUS_PLANT)) {
            ChorusFlowerBlock.generatePlant(level, origin, random, 8);
            return true;
        }
        return false;
    }
}


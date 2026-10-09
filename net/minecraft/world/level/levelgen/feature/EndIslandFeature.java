/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record EndIslandFeature() implements Feature
{
    public static final MapCodec<EndIslandFeature> CODEC = MapCodec.unit(EndIslandFeature::new);

    public MapCodec<EndIslandFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        float size = (float)random.nextInt(3) + 4.0f;
        int y = 0;
        while (size > 0.5f) {
            for (int x = Mth.floor(-size); x <= Mth.ceil(size); ++x) {
                for (int z = Mth.floor(-size); z <= Mth.ceil(size); ++z) {
                    if (!((float)(x * x + z * z) <= (size + 1.0f) * (size + 1.0f))) continue;
                    this.setBlock(level, origin.offset(x, y, z), Blocks.END_STONE.defaultBlockState());
                }
            }
            size -= (float)random.nextInt(2) + 0.5f;
            --y;
        }
        return true;
    }
}


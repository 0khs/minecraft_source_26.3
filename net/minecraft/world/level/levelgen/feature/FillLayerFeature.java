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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;

public record FillLayerFeature(int height, BlockState state) implements Feature
{
    public static final MapCodec<FillLayerFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.intRange((int)0, (int)DimensionType.Y_SIZE).fieldOf("height").forGetter(FillLayerFeature::height), (App)BlockState.CODEC.fieldOf("state").forGetter(FillLayerFeature::state)).apply((Applicative)i, FillLayerFeature::new));

    public MapCodec<FillLayerFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; ++dx) {
            for (int dz = 0; dz < 16; ++dz) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int y = level.getMinY() + this.height;
                pos.set(x, y, z);
                if (!level.getBlockState(pos).isAir()) continue;
                level.setBlock(pos, this.state, 2);
            }
        }
        return true;
    }
}


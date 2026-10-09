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
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ReplaceBlockFeature(List<BlockReplacement> replacements) implements Feature
{
    public static final MapCodec<ReplaceBlockFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.list(BlockReplacement.CODEC).fieldOf("targets").forGetter(ReplaceBlockFeature::replacements)).apply((Applicative)i, ReplaceBlockFeature::new));

    public MapCodec<ReplaceBlockFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        for (BlockReplacement replacement : this.replacements) {
            if (!replacement.target().test(level.getBlockState(origin), origin, random)) continue;
            level.setBlock(origin, replacement.state(), 2);
            break;
        }
        return true;
    }
}


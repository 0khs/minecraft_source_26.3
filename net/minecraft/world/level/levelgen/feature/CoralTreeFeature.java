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
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record CoralTreeFeature(Holder<PlacedFeature> feature) implements Feature
{
    public static final MapCodec<CoralTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)PlacedFeature.CODEC.fieldOf("feature").forGetter(CoralTreeFeature::feature)).apply((Applicative)i, CoralTreeFeature::new));

    public MapCodec<CoralTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos.MutableBlockPos mutPos = origin.mutable();
        int trunckHeight = random.nextInt(3) + 1;
        for (int i = 0; i < trunckHeight; ++i) {
            if (!this.feature.value().place(level, chunkGenerator, random, mutPos)) {
                return true;
            }
            mutPos.move(Direction.UP);
        }
        BlockPos trunckTopPos = mutPos.immutable();
        int nBranches = random.nextInt(3) + 2;
        List<Direction> directions = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        List<Direction> branchDirections = directions.subList(0, nBranches);
        for (Direction branchDirection : branchDirections) {
            mutPos.set(trunckTopPos);
            mutPos.move(branchDirection);
            int branchHeight = random.nextInt(5) + 2;
            int segmentLength = 0;
            for (int j = 0; j < branchHeight && this.feature.value().place(level, chunkGenerator, random, mutPos); ++j) {
                mutPos.move(Direction.UP);
                if (j != 0 && (++segmentLength < 2 || !(random.nextFloat() < 0.25f))) continue;
                mutPos.move(branchDirection);
                segmentLength = 0;
            }
        }
        return true;
    }
}


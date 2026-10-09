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
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record CoralClawFeature(Holder<PlacedFeature> feature) implements Feature
{
    public static final MapCodec<CoralClawFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)PlacedFeature.CODEC.fieldOf("feature").forGetter(CoralClawFeature::feature)).apply((Applicative)i, CoralClawFeature::new));

    public MapCodec<CoralClawFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!this.feature.value().place(level, chunkGenerator, random, origin)) {
            return false;
        }
        Direction clawDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int nBranches = random.nextInt(2) + 2;
        List<Direction> possibleDirections = Util.toShuffledList(Stream.of(clawDirection, clawDirection.getClockWise(), clawDirection.getCounterClockWise()), random);
        List<Direction> branchDirections = possibleDirections.subList(0, nBranches);
        block0: for (Direction branchDirection : branchDirections) {
            int i;
            int inwayLenth;
            Direction segmentDirection;
            BlockPos.MutableBlockPos mutPos = origin.mutable();
            int sidewayLength = random.nextInt(2) + 1;
            mutPos.move(branchDirection);
            if (branchDirection == clawDirection) {
                segmentDirection = clawDirection;
                inwayLenth = random.nextInt(3) + 2;
            } else {
                mutPos.move(Direction.UP);
                Direction[] segmentPossibleDirections = new Direction[]{branchDirection, Direction.UP};
                segmentDirection = Util.getRandom(segmentPossibleDirections, random);
                inwayLenth = random.nextInt(3) + 3;
            }
            for (i = 0; i < sidewayLength && this.feature.value().place(level, chunkGenerator, random, mutPos); ++i) {
                mutPos.move(segmentDirection);
            }
            mutPos.move(segmentDirection.getOpposite());
            mutPos.move(Direction.UP);
            for (i = 0; i < inwayLenth; ++i) {
                mutPos.move(clawDirection);
                if (!this.feature.value().place(level, chunkGenerator, random, mutPos)) continue block0;
                if (!(random.nextFloat() < 0.25f)) continue;
                mutPos.move(Direction.UP);
            }
        }
        return true;
    }
}


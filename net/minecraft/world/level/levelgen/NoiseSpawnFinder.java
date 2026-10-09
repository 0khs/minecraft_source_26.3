/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.SpawnTargetPoint;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;

public class NoiseSpawnFinder {
    private static final long MAX_RADIUS = 2048L;
    private Result result;

    private NoiseSpawnFinder(List<SpawnTargetPoint> targetPoints, DensitySamplerSet samplers) {
        this.result = NoiseSpawnFinder.getSpawnPositionAndFitness(samplers, targetPoints, 0, 0);
        this.radialSearch(samplers, targetPoints, 2048.0f, 512.0f);
        this.radialSearch(samplers, targetPoints, 512.0f, 32.0f);
    }

    public static BlockPos findSpawnPosition(List<SpawnTargetPoint> targetPoints, DensitySamplerSet samplers) {
        return new NoiseSpawnFinder(targetPoints, (DensitySamplerSet)samplers).result.location();
    }

    private void radialSearch(DensitySamplerSet samplers, List<SpawnTargetPoint> targetPoints, float maxRadius, float radiusIncrement) {
        float angle = 0.0f;
        float radius = radiusIncrement;
        BlockPos searchOrigin = this.result.location();
        while (radius <= maxRadius) {
            int z;
            int x = searchOrigin.getX() + (int)(Math.sin(angle) * (double)radius);
            Result candidate = NoiseSpawnFinder.getSpawnPositionAndFitness(samplers, targetPoints, x, z = searchOrigin.getZ() + (int)(Math.cos(angle) * (double)radius));
            if (candidate.fitness() < this.result.fitness()) {
                this.result = candidate;
            }
            if (!((double)(angle += radiusIncrement / radius) > Math.PI * 2)) continue;
            angle = 0.0f;
            radius += radiusIncrement;
        }
    }

    private static Result getSpawnPositionAndFitness(DensitySamplerSet samplers, List<SpawnTargetPoint> targetPoints, int blockX, int blockZ) {
        int quartBlockX = QuartPos.toBlock(QuartPos.fromBlock(blockX));
        int quartBlockZ = QuartPos.toBlock(QuartPos.fromBlock(blockZ));
        long minFitness = Long.MAX_VALUE;
        for (SpawnTargetPoint point : targetPoints) {
            minFitness = Math.min(minFitness, point.sampleFitness(samplers, quartBlockX, 0, quartBlockZ));
        }
        long distanceBiasToWorldOrigin = Mth.square((long)blockX) + Mth.square((long)blockZ);
        long fitnessWithDistance = minFitness * Mth.square(2048L) + distanceBiasToWorldOrigin;
        return new Result(new BlockPos(blockX, 0, blockZ), fitnessWithDistance);
    }

    private record Result(BlockPos location, long fitness) {
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public interface WorldCarver {
    public static final Codec<WorldCarver> DIRECT_CODEC = BuiltInRegistries.CARVER_TYPE.byNameCodec().dispatch(WorldCarver::codec, Function.identity());
    public static final Codec<Holder<WorldCarver>> CODEC = RegistryCodecs.holder(Registries.CARVER, DIRECT_CODEC);
    public static final Codec<HolderSet<WorldCarver>> LIST_CODEC = RegistryCodecs.holderSet(Registries.CARVER, DIRECT_CODEC);

    default public int getRange() {
        return 4;
    }

    public static void carveEllipsoid(ChunkPos chunkPos, double x, double y, double z, double horizontalRadius, double verticalRadius, CarverOutput output, CarveSkipChecker skipChecker) {
        double centerX = chunkPos.getMiddleBlockX();
        double centerZ = chunkPos.getMiddleBlockZ();
        double maxDelta = 16.0 + horizontalRadius * 2.0;
        if (Math.abs(x - centerX) > maxDelta || Math.abs(z - centerZ) > maxDelta) {
            return;
        }
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int minXIndex = Math.max(Mth.floor(x - horizontalRadius) - chunkMinX - 1, 0);
        int maxXIndex = Math.min(Mth.floor(x + horizontalRadius) - chunkMinX, 15);
        int minY = Math.max(Mth.floor(y - verticalRadius) - 1, output.minY());
        int maxY = Math.min(Mth.floor(y + verticalRadius) + 1, output.maxY());
        int minZIndex = Math.max(Mth.floor(z - horizontalRadius) - chunkMinZ - 1, 0);
        int maxZIndex = Math.min(Mth.floor(z + horizontalRadius) - chunkMinZ, 15);
        for (int xIndex = minXIndex; xIndex <= maxXIndex; ++xIndex) {
            int worldX = chunkPos.getBlockX(xIndex);
            double xd = ((double)worldX + 0.5 - x) / horizontalRadius;
            for (int zIndex = minZIndex; zIndex <= maxZIndex; ++zIndex) {
                int worldZ = chunkPos.getBlockZ(zIndex);
                double zd = ((double)worldZ + 0.5 - z) / horizontalRadius;
                if (xd * xd + zd * zd >= 1.0) continue;
                for (int worldY = maxY; worldY > minY; --worldY) {
                    double yd = ((double)worldY - 0.5 - y) / verticalRadius;
                    if (skipChecker.shouldSkip(xd, yd, zd, worldY)) continue;
                    output.carve(xIndex, worldY, zIndex);
                }
            }
        }
    }

    public boolean carve(WorldGenerationContext var1, RandomSource var2, ChunkPos var3, ChunkPos var4, CarverOutput var5);

    public boolean isStartChunk(RandomSource var1);

    public MapCodec<? extends WorldCarver> codec();

    public static boolean canReach(ChunkPos chunkPos, double x, double z, int currentStep, int totalSteps, float thickness) {
        double rr;
        double remaining;
        double zMid;
        double zd;
        double xMid = chunkPos.getMiddleBlockX();
        double xd = x - xMid;
        return xd * xd + (zd = z - (zMid = (double)chunkPos.getMiddleBlockZ())) * zd - (remaining = (double)(totalSteps - currentStep)) * remaining <= (rr = (double)(thickness + 2.0f + 16.0f)) * rr;
    }

    public static interface CarveSkipChecker {
        public boolean shouldSkip(double var1, double var3, double var5, int var7);
    }
}


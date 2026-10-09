/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

public abstract class BiomeSource {
    public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(() -> (Set)this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet()));

    protected BiomeSource() {
    }

    protected abstract MapCodec<? extends BiomeSource> codec();

    protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

    public Set<Holder<Biome>> possibleBiomes() {
        return this.possibleBiomes.get();
    }

    public @Nullable Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int x, int y, int z, int searchRadius, Predicate<Holder<Biome>> allowed, RandomSource random, RandomState randomState) {
        return this.findBiomeHorizontal(x, y, z, searchRadius, 1, allowed, random, false, randomState);
    }

    public @Nullable Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos origin, int searchRadius, int sampleResolutionHorizontal, int sampleResolutionVertical, Predicate<Holder<Biome>> allowed, RandomState randomState, LevelReader level) {
        Set candidateBiomes = this.possibleBiomes().stream().filter(allowed).collect(Collectors.toUnmodifiableSet());
        if (candidateBiomes.isEmpty()) {
            return null;
        }
        BiomeResolver resolver = this.createCachingResolver(randomState);
        int sampleRadius = Math.floorDiv(searchRadius, sampleResolutionHorizontal);
        int[] sampleYs = Mth.outFromOrigin(origin.getY(), level.getMinY() + 1, level.getMaxY() + 1, sampleResolutionVertical).toArray();
        for (BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(BlockPos.ZERO, sampleRadius, Direction.EAST, Direction.SOUTH)) {
            int blockX = origin.getX() + sampleColumn.getX() * sampleResolutionHorizontal;
            int blockZ = origin.getZ() + sampleColumn.getZ() * sampleResolutionHorizontal;
            int noiseX = QuartPos.fromBlock(blockX);
            int noiseZ = QuartPos.fromBlock(blockZ);
            for (int blockY : sampleYs) {
                int noiseY = QuartPos.fromBlock(blockY);
                Holder<Biome> biome = resolver.getNoiseBiome(noiseX, noiseY, noiseZ);
                if (!candidateBiomes.contains(biome)) continue;
                return Pair.of((Object)new BlockPos(blockX, blockY, blockZ), biome);
            }
        }
        return null;
    }

    public @Nullable Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int originX, int originY, int originZ, int searchRadius, int skipSteps, Predicate<Holder<Biome>> allowed, RandomSource random, boolean findClosest, RandomState randomState) {
        int startRadius;
        int noiseCenterX = QuartPos.fromBlock(originX);
        int noiseCenterZ = QuartPos.fromBlock(originZ);
        int noiseRadius = QuartPos.fromBlock(searchRadius);
        int noiseY = QuartPos.fromBlock(originY);
        BiomeResolver resolver = this.createCachingResolver(randomState);
        Pair result = null;
        int found = 0;
        for (int currentRadius = startRadius = findClosest ? 0 : noiseRadius; currentRadius <= noiseRadius; currentRadius += skipSteps) {
            int z;
            int n = z = SharedConstants.DEBUG_ONLY_GENERATE_HALF_THE_WORLD || SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -currentRadius;
            while (z <= currentRadius) {
                boolean zEdge = Math.abs(z) == currentRadius;
                for (int x = -currentRadius; x <= currentRadius; x += skipSteps) {
                    int noiseZ;
                    int noiseX;
                    Holder<Biome> biome;
                    if (findClosest) {
                        boolean xEdge;
                        boolean bl = xEdge = Math.abs(x) == currentRadius;
                        if (!xEdge && !zEdge) continue;
                    }
                    if (!allowed.test(biome = resolver.getNoiseBiome(noiseX = noiseCenterX + x, noiseY, noiseZ = noiseCenterZ + z))) continue;
                    if (result == null || random.nextInt(found + 1) == 0) {
                        BlockPos resultPos = new BlockPos(QuartPos.toBlock(noiseX), originY, QuartPos.toBlock(noiseZ));
                        if (findClosest) {
                            return Pair.of((Object)resultPos, biome);
                        }
                        result = Pair.of((Object)resultPos, biome);
                    }
                    ++found;
                }
                z += skipSteps;
            }
        }
        return result;
    }

    public final BiomeResolver createUncachedResolver(RandomState randomState) {
        return this.createResolver(randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED));
    }

    public final BiomeResolver createCachingResolver(RandomState randomState) {
        SamplerContext context = SamplerContext.builder().enableCaches().build();
        return this.createResolver(randomState.createClimateSampler(context));
    }

    public abstract BiomeResolver createResolver(Climate.Sampler var1);

    public BiomeResolver createResolverForChunk(Climate.Sampler sampler, int minQuartX, int minQuartY, int minQuartZ, int quartSizeX, int quartSizeY, int quartSizeZ) {
        return this.createResolver(sampler);
    }

    public void addDebugInfo(List<String> result, BlockPos feetPos, Climate.Sampler sampler) {
    }
}


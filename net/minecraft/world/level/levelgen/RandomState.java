/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferPool;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctionCompiler;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RandomState {
    private static final int MAX_BUFFER_POOLS = 16;
    private static final int MAX_BUFFER_AGE_TICKS = 20;
    private final long seed;
    private final PositionalRandomFactory random;
    private final HolderGetter<NormalNoise> noises;
    private final NoiseRouter router;
    private final MaterialSystem materialSystem;
    private final Map<ResourceKey<NormalNoise>, Noise> noiseInstances;
    private final Map<Identifier, PositionalRandomFactory> positionalRandoms;
    private final DensityFunctionCompiler densityFunctionCompiler;
    private final ReentrantLock densityBufferPoolLock = new ReentrantLock();
    private final List<DensityBufferPool> densityBufferPools = new ArrayList<DensityBufferPool>(16);

    public static RandomState create(HolderGetter<NormalNoise> noises, long seed, NoiseGeneratorSettings settings) {
        return RandomState.create(noises, seed, settings.useLegacyRandomSource(), settings.defaultBlock(), settings.seaLevel(), settings.noiseRouter());
    }

    public static RandomState create(HolderGetter<NormalNoise> noises, long seed, boolean useLegacyRandom, BlockState defaultBlock, int seaLevel, NoiseRouter noiseRouter) {
        return new RandomState(noises, seed, useLegacyRandom, defaultBlock, seaLevel, noiseRouter);
    }

    private RandomState(HolderGetter<NormalNoise> noises, final long seed, final boolean useLegacyRandom, BlockState defaultBlock, int seaLevel, NoiseRouter router) {
        WorldgenRandom.Algorithm randomAlgorithm = useLegacyRandom ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
        this.seed = seed;
        this.random = randomAlgorithm.newInstance(seed).forkPositional();
        this.noises = noises;
        this.router = router;
        this.noiseInstances = new ConcurrentHashMap<ResourceKey<NormalNoise>, Noise>();
        this.positionalRandoms = new ConcurrentHashMap<Identifier, PositionalRandomFactory>();
        this.materialSystem = new MaterialSystem(this, defaultBlock, seaLevel, router.chunkSurfaceLevel(), this.random);
        this.densityFunctionCompiler = new DensityFunctionCompiler(new DensityFunction.CompileContext(){
            final /* synthetic */ RandomState this$0;
            {
                RandomState randomState = this$0;
                Objects.requireNonNull(randomState);
                this.this$0 = randomState;
            }

            private RandomSource newLegacyInstance(long seedOffset) {
                return new LegacyRandomSource(seed + seedOffset);
            }

            @Override
            public Noise createNoiseSampler(Holder<NormalNoise> parameters) {
                if (parameters.is(Noises.TEMPERATURE_NETHER)) {
                    return parameters.value().createForLegacyNetherBiome(this.newLegacyInstance(0L));
                }
                if (parameters.is(Noises.VEGETATION_NETHER)) {
                    return parameters.value().createForLegacyNetherBiome(this.newLegacyInstance(1L));
                }
                return this.this$0.getOrCreateNoise(parameters.unwrapKey().orElseThrow());
            }

            @Override
            public RandomSource createRandom(Identifier seed2) {
                if (useLegacyRandom && seed2.equals(BlendedNoise.NOISE_SEED)) {
                    return this.newLegacyInstance(0L);
                }
                return this.this$0.random.fromHashOf(seed2);
            }

            @Override
            public RandomSource createEndIslandRandom() {
                return new LegacyRandomSource(seed);
            }
        });
    }

    public DensitySamplerSet samplersWithContext(final SamplerContext context) {
        return new DensitySamplerSet(){
            final /* synthetic */ RandomState this$0;
            {
                RandomState randomState = this$0;
                Objects.requireNonNull(randomState);
                this.this$0 = randomState;
            }

            @Override
            public DensitySampler.Bound get(DensityFunction function) {
                return this.this$0.getSampler(function).bind(context);
            }

            @Override
            public float sampleValue(DensityFunction function, int blockX, int blockY, int blockZ) {
                return this.this$0.getSampler(function).sampleValue(context, blockX, blockY, blockZ);
            }
        };
    }

    public Climate.Sampler createClimateSampler(SamplerContext context) {
        return this.router.createClimateSampler(this.samplersWithContext(context));
    }

    public Noise getOrCreateNoise(ResourceKey<NormalNoise> noise) {
        return this.noiseInstances.computeIfAbsent(noise, key -> Noises.instantiate(this.noises, this.random, noise));
    }

    public PositionalRandomFactory getOrCreateRandomFactory(Identifier name) {
        return this.positionalRandoms.computeIfAbsent(name, key -> this.random.fromHashOf(name).forkPositional());
    }

    public MaterialSystem surfaceSystem() {
        return this.materialSystem;
    }

    public DensitySampler getSampler(DensityFunction function) {
        return this.densityFunctionCompiler.getSampler(function);
    }

    @VisibleForDebug
    public float sampleBlockValueUncached(DensityFunction function, int x, int y, int z) {
        return this.getSampler(function).sampleValue(SamplerContext.EMPTY_UNCACHED, x, y, z);
    }

    public DensityBufferPool acquireDensityBufferPool() {
        this.densityBufferPoolLock.lock();
        try {
            if (this.densityBufferPools.isEmpty()) {
                DensityBufferPool densityBufferPool = new DensityBufferPool(20);
                return densityBufferPool;
            }
            DensityBufferPool densityBufferPool = this.densityBufferPools.removeLast();
            return densityBufferPool;
        }
        finally {
            this.densityBufferPoolLock.unlock();
        }
    }

    public void releaseDensityBufferPool(DensityBufferPool pool) {
        this.densityBufferPoolLock.lock();
        try {
            if (this.densityBufferPools.size() < 16) {
                this.densityBufferPools.add(pool);
            }
        }
        finally {
            this.densityBufferPoolLock.unlock();
        }
    }

    public void garbageCollect() {
        this.densityBufferPoolLock.lock();
        try {
            this.densityBufferPools.removeIf(pool -> {
                pool.garbageCollect();
                return pool.isEmpty();
            });
        }
        finally {
            this.densityBufferPoolLock.unlock();
        }
    }

    @Deprecated
    public long seed() {
        return this.seed;
    }
}


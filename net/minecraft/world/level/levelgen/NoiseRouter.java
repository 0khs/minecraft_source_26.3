/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;

public record NoiseRouter(DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction chunkSurfaceLevel, DensityFunction finalDensity) {
    public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create(i -> i.group((App)DensityFunction.CODEC.fieldOf("temperature").forGetter(NoiseRouter::temperature), (App)DensityFunction.CODEC.fieldOf("vegetation").forGetter(NoiseRouter::vegetation), (App)DensityFunction.CODEC.fieldOf("continents").forGetter(NoiseRouter::continents), (App)DensityFunction.CODEC.fieldOf("erosion").forGetter(NoiseRouter::erosion), (App)DensityFunction.CODEC.fieldOf("depth").forGetter(NoiseRouter::depth), (App)DensityFunction.CODEC.fieldOf("ridges").forGetter(NoiseRouter::ridges), (App)DensityFunction.CODEC.fieldOf("chunk_surface_level").forGetter(NoiseRouter::chunkSurfaceLevel), (App)DensityFunction.CODEC.fieldOf("final_density").forGetter(NoiseRouter::finalDensity)).apply((Applicative)i, NoiseRouter::new));

    public Climate.Sampler createClimateSampler(DensitySamplerSet densitySamplers) {
        return new Climate.Sampler(densitySamplers.get(this.temperature), densitySamplers.get(this.vegetation), densitySamplers.get(this.continents), densitySamplers.get(this.erosion), densitySamplers.get(this.depth), densitySamplers.get(this.ridges));
    }
}


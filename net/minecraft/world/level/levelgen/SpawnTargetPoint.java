/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;

public record SpawnTargetPoint(Map<Holder<DensityFunction>, Climate.Parameter> parameters) {
    public static final Codec<SpawnTargetPoint> CODEC = Codec.unboundedMap(DensityFunction.REFERENCE_CODEC, Climate.Parameter.CODEC).xmap(SpawnTargetPoint::new, SpawnTargetPoint::parameters);

    public long sampleFitness(DensitySamplerSet samplers, int blockX, int blockY, int blockZ) {
        long fitness = 0L;
        for (Map.Entry<Holder<DensityFunction>, Climate.Parameter> parameter : this.parameters.entrySet()) {
            DensityFunction function = parameter.getKey().value();
            long value = Climate.quantizeCoord(samplers.sampleValue(function, blockX, blockY, blockZ));
            fitness += Mth.square(parameter.getValue().distance(value));
        }
        return fitness;
    }
}


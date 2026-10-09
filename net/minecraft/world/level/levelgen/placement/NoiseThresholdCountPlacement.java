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
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public record NoiseThresholdCountPlacement(double noiseLevel, int belowNoise, int aboveNoise) implements RepeatingPlacement
{
    public static final MapCodec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.DOUBLE.fieldOf("noise_level").forGetter(NoiseThresholdCountPlacement::noiseLevel), (App)Codec.INT.fieldOf("below_noise").forGetter(NoiseThresholdCountPlacement::belowNoise), (App)Codec.INT.fieldOf("above_noise").forGetter(NoiseThresholdCountPlacement::aboveNoise)).apply((Applicative)i, NoiseThresholdCountPlacement::new));

    public static NoiseThresholdCountPlacement of(double noiseLevel, int belowNoise, int aboveNoise) {
        return new NoiseThresholdCountPlacement(noiseLevel, belowNoise, aboveNoise);
    }

    @Override
    public int count(RandomSource random, BlockPos origin) {
        double flowerNoise = Biome.BIOME_INFO_NOISE.get((double)origin.getX() / 200.0, (double)origin.getZ() / 200.0);
        return flowerNoise < this.noiseLevel ? this.belowNoise : this.aboveNoise;
    }

    public MapCodec<NoiseThresholdCountPlacement> codec() {
        return CODEC;
    }
}


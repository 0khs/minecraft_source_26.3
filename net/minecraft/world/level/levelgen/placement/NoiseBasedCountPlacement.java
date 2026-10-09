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

public record NoiseBasedCountPlacement(int noiseToCountRatio, double noiseFactor, double noiseOffset) implements RepeatingPlacement
{
    public static final MapCodec<NoiseBasedCountPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.fieldOf("noise_to_count_ratio").forGetter(NoiseBasedCountPlacement::noiseToCountRatio), (App)Codec.DOUBLE.fieldOf("noise_factor").forGetter(NoiseBasedCountPlacement::noiseFactor), (App)Codec.DOUBLE.optionalFieldOf("noise_offset", (Object)0.0).forGetter(NoiseBasedCountPlacement::noiseOffset)).apply((Applicative)i, NoiseBasedCountPlacement::new));

    public static NoiseBasedCountPlacement of(int noiseToCountRatio, double noiseFactor, double noiseOffset) {
        return new NoiseBasedCountPlacement(noiseToCountRatio, noiseFactor, noiseOffset);
    }

    @Override
    public int count(RandomSource random, BlockPos origin) {
        double flowerNoise = Biome.BIOME_INFO_NOISE.get((double)origin.getX() / this.noiseFactor, (double)origin.getZ() / this.noiseFactor);
        return (int)Math.ceil((flowerNoise + this.noiseOffset) * (double)this.noiseToCountRatio);
    }

    public MapCodec<NoiseBasedCountPlacement> codec() {
        return CODEC;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NoiseStack;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.jspecify.annotations.Nullable;

@Deprecated
public class LegacyFbmInitializer {
    public static NoiseStack createForLegacyNetherBiome(RandomSource random, int firstOctave, DoubleList amplitudes) {
        double zeroOctaveAmplitude;
        int octaves = amplitudes.size();
        int zeroOctaveIndex = -firstOctave;
        @Nullable PerlinNoise[] noiseLevels = new PerlinNoise[octaves];
        PerlinNoise zeroOctave = new PerlinNoise(random);
        if (zeroOctaveIndex >= 0 && zeroOctaveIndex < octaves && (zeroOctaveAmplitude = amplitudes.getDouble(zeroOctaveIndex)) != 0.0) {
            noiseLevels[zeroOctaveIndex] = zeroOctave;
        }
        for (int i = zeroOctaveIndex - 1; i >= 0; --i) {
            if (i < octaves) {
                double amplitude = amplitudes.getDouble(i);
                if (amplitude != 0.0) {
                    noiseLevels[i] = new PerlinNoise(random);
                    continue;
                }
                LegacyFbmInitializer.skipOctave(random);
                continue;
            }
            LegacyFbmInitializer.skipOctave(random);
        }
        if (Arrays.stream(noiseLevels).filter(Objects::nonNull).count() != amplitudes.stream().filter(a -> a != 0.0).count()) {
            throw new IllegalStateException("Failed to create correct number of noise levels for given non-zero amplitudes");
        }
        if (zeroOctaveIndex < octaves - 1) {
            throw new IllegalArgumentException("Positive octaves are temporarily disabled");
        }
        double factor = Math.pow(2.0, -zeroOctaveIndex);
        double valueFactor = Math.pow(2.0, octaves - 1) / (Math.pow(2.0, octaves) - 1.0);
        NoiseStack.Builder stack = NoiseStack.builder();
        for (int i = 0; i < noiseLevels.length; ++i) {
            PerlinNoise noise = noiseLevels[i];
            if (noise != null) {
                stack.add(noise, factor, (float)(valueFactor * amplitudes.getDouble(i)));
            }
            factor *= 2.0;
            valueFactor /= 2.0;
        }
        return stack.build();
    }

    private static void skipOctave(RandomSource random) {
        random.consumeCount(262);
    }
}


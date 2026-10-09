/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SmearedPerlinNoise;

public class NoiseStack
implements Noise {
    protected final Layer[] layers;
    private final Interval range;

    private NoiseStack(Layer[] layers) {
        this.layers = layers;
        Interval range = Interval.ofExact(0.0f);
        for (Layer layer : layers) {
            Interval layerRange = Interval.mul(layer.noise.range(), Interval.ofExact(layer.amplitude));
            range = Interval.add(range, layerRange);
        }
        this.range = range;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Interval range() {
        return this.range;
    }

    @Override
    public float get(double x, double y, double z) {
        float value = 0.0f;
        for (Layer layer : this.layers) {
            double frequency = layer.frequency;
            value += layer.amplitude * layer.noise.get(x * frequency, y * frequency, z * frequency);
        }
        return value;
    }

    @Override
    public float get(double x, double y) {
        float value = 0.0f;
        for (Layer layer : this.layers) {
            double frequency = layer.frequency;
            value += layer.amplitude * layer.noise.get(x * frequency, y * frequency);
        }
        return value;
    }

    @Override
    public void addToVolume(DensityBuffer buffer, DensityVolume volume, double xzScale, double yScale, float amplitude) {
        for (Layer layer : this.layers) {
            double frequency = layer.frequency;
            layer.noise.addToVolume(buffer, volume, xzScale * frequency, yScale * frequency, amplitude * layer.amplitude);
        }
    }

    @VisibleForTesting
    public Noise getLayer(int index) {
        return this.layers[index].noise;
    }

    protected record Layer(Noise noise, double frequency, float amplitude) {
    }

    public static class Builder {
        private final List<Layer> layers = new ArrayList<Layer>();

        private Builder() {
        }

        public Builder add(Noise noise, double frequency, float amplitude) {
            this.layers.add(new Layer(noise, frequency, amplitude));
            return this;
        }

        public Builder addStack(NoiseStack stack, double frequency, float amplitude) {
            for (Layer layer : stack.layers) {
                this.layers.add(new Layer(layer.noise, layer.frequency * frequency, layer.amplitude * amplitude));
            }
            return this;
        }

        public NoiseStack build() {
            Layer[] layers = (Layer[])this.layers.toArray(Layer[]::new);
            if (this.layers.stream().allMatch(layer -> layer.noise.getClass() == PerlinNoise.class)) {
                return new Perlin(layers);
            }
            if (this.layers.stream().allMatch(layer -> layer.noise.getClass() == SmearedPerlinNoise.class)) {
                return new SmearedPerlin(layers);
            }
            return new NoiseStack(layers);
        }
    }

    private static class SmearedPerlin
    extends NoiseStack {
        private SmearedPerlin(Layer[] layers) {
            super(layers);
        }

        @Override
        public float get(double x, double y, double z) {
            float value = 0.0f;
            for (Layer layer : this.layers) {
                double frequency = layer.frequency;
                value += layer.amplitude * layer.noise.get(x * frequency, y * frequency, z * frequency);
            }
            return value;
        }

        @Override
        public void addToVolume(DensityBuffer buffer, DensityVolume volume, double xzScale, double yScale, float amplitude) {
            for (Layer layer : this.layers) {
                double frequency = layer.frequency;
                layer.noise.addToVolume(buffer, volume, xzScale * frequency, yScale * frequency, amplitude * layer.amplitude);
            }
        }
    }

    private static class Perlin
    extends NoiseStack {
        private Perlin(Layer[] layers) {
            super(layers);
        }

        @Override
        public float get(double x, double y, double z) {
            float value = 0.0f;
            for (Layer layer : this.layers) {
                double frequency = layer.frequency;
                value += layer.amplitude * layer.noise.get(x * frequency, y * frequency, z * frequency);
            }
            return value;
        }

        @Override
        public void addToVolume(DensityBuffer buffer, DensityVolume volume, double xzScale, double yScale, float amplitude) {
            for (Layer layer : this.layers) {
                double frequency = layer.frequency;
                layer.noise.addToVolume(buffer, volume, xzScale * frequency, yScale * frequency, amplitude * layer.amplitude);
            }
        }
    }
}


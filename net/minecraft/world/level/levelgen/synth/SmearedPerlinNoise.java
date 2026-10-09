/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.synth.GradientNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

@Deprecated
public class SmearedPerlinNoise
extends PerlinNoise {
    private static final float SHIFT_UP_EPSILON = 1.0E-7f;
    private final double fudgeYScale;

    public SmearedPerlinNoise(RandomSource random, double fudgeYScale) {
        super(random);
        this.fudgeYScale = fudgeYScale;
    }

    public static Interval range(double fudgeYScale) {
        return Interval.ofSymmetric((float)(Math.abs(fudgeYScale) + 2.0));
    }

    @Override
    public Interval range() {
        return SmearedPerlinNoise.range(this.fudgeYScale);
    }

    @Override
    public float get(double _x, double _y, double _z) {
        double x = SmearedPerlinNoise.wrap(_x) + this.offsetX;
        double y = SmearedPerlinNoise.wrap(_y) + this.offsetY;
        double z = SmearedPerlinNoise.wrap(_z) + this.offsetZ;
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);
        float relativeX = (float)(x - (double)floorX);
        double relativeY = y - (double)floorY;
        float relativeZ = (float)(z - (double)floorZ);
        float fudgedRelativeY = (float)(relativeY - this.computeFudgeY(_y, relativeY));
        return this.sampleAndLerp(floorX, floorY, floorZ, relativeX, fudgedRelativeY, relativeZ, (float)relativeY);
    }

    private double computeFudgeY(double originalY, double relativeY) {
        double fudgeLimit = originalY >= 0.0 && originalY < relativeY ? originalY : relativeY;
        return (double)Mth.floor(fudgeLimit / this.fudgeYScale + (double)1.0E-7f) * this.fudgeYScale;
    }

    @Override
    public void addToVolume(DensityBuffer buffer, DensityVolume volume, double xzScale, double yScale, float amplitude) {
        float d000xz = 0.0f;
        float d100xz = 0.0f;
        float d010xz = 0.0f;
        float d110xz = 0.0f;
        float d001xz = 0.0f;
        float d101xz = 0.0f;
        float d011xz = 0.0f;
        float d111xz = 0.0f;
        float g000y = 0.0f;
        float g100y = 0.0f;
        float g010y = 0.0f;
        float g110y = 0.0f;
        float g001y = 0.0f;
        float g101y = 0.0f;
        float g011y = 0.0f;
        float g111y = 0.0f;
        int index = 0;
        for (int indexZ = 0; indexZ < volume.sizeZ(); ++indexZ) {
            double z = SmearedPerlinNoise.wrap((double)volume.blockZ(indexZ) * xzScale) + this.offsetZ;
            int floorZ = Mth.floor(z);
            float relativeZ = (float)(z - (double)floorZ);
            float alphaZ = Mth.smoothstep(relativeZ);
            for (int indexX = 0; indexX < volume.sizeX(); ++indexX) {
                double x = SmearedPerlinNoise.wrap((double)volume.blockX(indexX) * xzScale) + this.offsetX;
                int floorX = Mth.floor(x);
                float relativeX = (float)(x - (double)floorX);
                int x0 = this.permute(floorX);
                int x1 = this.permute(floorX + 1);
                float alphaX = Mth.smoothstep(relativeX);
                int lastFloorY = Integer.MIN_VALUE;
                for (int indexY = 0; indexY < volume.sizeY(); ++indexY) {
                    double originalY = (double)volume.blockY(indexY) * yScale;
                    double y = SmearedPerlinNoise.wrap(originalY) + this.offsetY;
                    int floorY = Mth.floor(y);
                    double relativeY = y - (double)floorY;
                    float alphaY = Mth.smoothstep((float)relativeY);
                    if (lastFloorY != floorY) {
                        int xy00 = this.permute(x0 + floorY);
                        int xy01 = this.permute(x0 + floorY + 1);
                        int xy10 = this.permute(x1 + floorY);
                        int xy11 = this.permute(x1 + floorY + 1);
                        GradientNoise.Gradient g000 = this.permuteToGrad(xy00 + floorZ);
                        d000xz = g000.dotXz(relativeX, relativeZ);
                        g000y = g000.y();
                        GradientNoise.Gradient g100 = this.permuteToGrad(xy10 + floorZ);
                        d100xz = g100.dotXz(relativeX - 1.0f, relativeZ);
                        g100y = g100.y();
                        GradientNoise.Gradient g010 = this.permuteToGrad(xy01 + floorZ);
                        d010xz = g010.dotXz(relativeX, relativeZ);
                        g010y = g010.y();
                        GradientNoise.Gradient g110 = this.permuteToGrad(xy11 + floorZ);
                        d110xz = g110.dotXz(relativeX - 1.0f, relativeZ);
                        g110y = g110.y();
                        GradientNoise.Gradient g001 = this.permuteToGrad(xy00 + floorZ + 1);
                        d001xz = g001.dotXz(relativeX, relativeZ - 1.0f);
                        g001y = g001.y();
                        GradientNoise.Gradient g101 = this.permuteToGrad(xy10 + floorZ + 1);
                        d101xz = g101.dotXz(relativeX - 1.0f, relativeZ - 1.0f);
                        g101y = g101.y();
                        GradientNoise.Gradient g011 = this.permuteToGrad(xy01 + floorZ + 1);
                        d011xz = g011.dotXz(relativeX, relativeZ - 1.0f);
                        g011y = g011.y();
                        GradientNoise.Gradient g111 = this.permuteToGrad(xy11 + floorZ + 1);
                        d111xz = g111.dotXz(relativeX - 1.0f, relativeZ - 1.0f);
                        g111y = g111.y();
                        lastFloorY = floorY;
                    }
                    float fudgedRelativeY = (float)(relativeY - this.computeFudgeY(originalY, relativeY));
                    buffer.addTo(index, amplitude * Mth.lerp3(alphaX, alphaY, alphaZ, d000xz + g000y * fudgedRelativeY, d100xz + g100y * fudgedRelativeY, d010xz + g010y * (fudgedRelativeY - 1.0f), d110xz + g110y * (fudgedRelativeY - 1.0f), d001xz + g001y * fudgedRelativeY, d101xz + g101y * fudgedRelativeY, d011xz + g011y * (fudgedRelativeY - 1.0f), d111xz + g111y * (fudgedRelativeY - 1.0f)));
                    ++index;
                }
            }
        }
    }
}


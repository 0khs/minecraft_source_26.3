/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.synth.GradientNoise;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;

public class PerlinNoise
extends GradientNoise {
    public static final Interval RANGE = Interval.ofSymmetric(2.0f);
    public static final double STANDARD_DEVIATION = 0.2702247831245211;

    public PerlinNoise(RandomSource random) {
        super(random);
    }

    @Override
    public Interval range() {
        return RANGE;
    }

    @Override
    public float get(double x, double y) {
        return this.get(PerlinNoise.wrap(x), 0.0, PerlinNoise.wrap(y));
    }

    @Override
    public float get(double _x, double _y, double _z) {
        double x = PerlinNoise.wrap(_x) + this.offsetX;
        double y = PerlinNoise.wrap(_y) + this.offsetY;
        double z = PerlinNoise.wrap(_z) + this.offsetZ;
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);
        float relativeX = (float)(x - (double)floorX);
        float relativeY = (float)(y - (double)floorY);
        float relativeZ = (float)(z - (double)floorZ);
        return this.sampleAndLerp(floorX, floorY, floorZ, relativeX, relativeY, relativeZ, relativeY);
    }

    public float noiseWithDerivative(double _x, double _y, double _z, float[] derivativeOut) {
        double x = PerlinNoise.wrap(_x) + this.offsetX;
        double y = PerlinNoise.wrap(_y) + this.offsetY;
        double z = PerlinNoise.wrap(_z) + this.offsetZ;
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);
        float relativeX = (float)(x - (double)floorX);
        float relativeY = (float)(y - (double)floorY);
        float relativeZ = (float)(z - (double)floorZ);
        return this.sampleWithDerivative(floorX, floorY, floorZ, relativeX, relativeY, relativeZ, derivativeOut);
    }

    protected float sampleAndLerp(int x, int y, int z, float relativeX, float relativeY, float relativeZ, float originalRelativeY) {
        int x0 = this.permute(x);
        int x1 = this.permute(x + 1);
        int xy00 = this.permute(x0 + y);
        int xy01 = this.permute(x0 + y + 1);
        int xy10 = this.permute(x1 + y);
        int xy11 = this.permute(x1 + y + 1);
        float d000 = PerlinNoise.gradDot(this.permute(xy00 + z), relativeX, relativeY, relativeZ);
        float d100 = PerlinNoise.gradDot(this.permute(xy10 + z), relativeX - 1.0f, relativeY, relativeZ);
        float d010 = PerlinNoise.gradDot(this.permute(xy01 + z), relativeX, relativeY - 1.0f, relativeZ);
        float d110 = PerlinNoise.gradDot(this.permute(xy11 + z), relativeX - 1.0f, relativeY - 1.0f, relativeZ);
        float d001 = PerlinNoise.gradDot(this.permute(xy00 + z + 1), relativeX, relativeY, relativeZ - 1.0f);
        float d101 = PerlinNoise.gradDot(this.permute(xy10 + z + 1), relativeX - 1.0f, relativeY, relativeZ - 1.0f);
        float d011 = PerlinNoise.gradDot(this.permute(xy01 + z + 1), relativeX, relativeY - 1.0f, relativeZ - 1.0f);
        float d111 = PerlinNoise.gradDot(this.permute(xy11 + z + 1), relativeX - 1.0f, relativeY - 1.0f, relativeZ - 1.0f);
        float xAlpha = Mth.smoothstep(relativeX);
        float yAlpha = Mth.smoothstep(originalRelativeY);
        float zAlpha = Mth.smoothstep(relativeZ);
        return Mth.lerp3(xAlpha, yAlpha, zAlpha, d000, d100, d010, d110, d001, d101, d011, d111);
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
            double z = PerlinNoise.wrap((double)volume.blockZ(indexZ) * xzScale) + this.offsetZ;
            int floorZ = Mth.floor(z);
            float relativeZ = (float)(z - (double)floorZ);
            float alphaZ = Mth.smoothstep(relativeZ);
            for (int indexX = 0; indexX < volume.sizeX(); ++indexX) {
                double x = PerlinNoise.wrap((double)volume.blockX(indexX) * xzScale) + this.offsetX;
                int floorX = Mth.floor(x);
                float relativeX = (float)(x - (double)floorX);
                int x0 = this.permute(floorX);
                int x1 = this.permute(floorX + 1);
                float alphaX = Mth.smoothstep(relativeX);
                int lastFloorY = Integer.MIN_VALUE;
                for (int indexY = 0; indexY < volume.sizeY(); ++indexY) {
                    double y = PerlinNoise.wrap((double)volume.blockY(indexY) * yScale) + this.offsetY;
                    int floorY = Mth.floor(y);
                    float relativeY = (float)(y - (double)floorY);
                    float alphaY = Mth.smoothstep(relativeY);
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
                    buffer.addTo(index, amplitude * Mth.lerp3(alphaX, alphaY, alphaZ, d000xz + g000y * relativeY, d100xz + g100y * relativeY, d010xz + g010y * (relativeY - 1.0f), d110xz + g110y * (relativeY - 1.0f), d001xz + g001y * relativeY, d101xz + g101y * relativeY, d011xz + g011y * (relativeY - 1.0f), d111xz + g111y * (relativeY - 1.0f)));
                    ++index;
                }
            }
        }
    }

    private float sampleWithDerivative(int x, int y, int z, float xr, float yr, float zr, float[] derivativeOut) {
        int x0 = this.permute(x);
        int x1 = this.permute(x + 1);
        int xy00 = this.permute(x0 + y);
        int xy01 = this.permute(x0 + y + 1);
        int xy10 = this.permute(x1 + y);
        int xy11 = this.permute(x1 + y + 1);
        GradientNoise.Gradient g000 = this.permuteToGrad(xy00 + z);
        GradientNoise.Gradient g100 = this.permuteToGrad(xy10 + z);
        GradientNoise.Gradient g010 = this.permuteToGrad(xy01 + z);
        GradientNoise.Gradient g110 = this.permuteToGrad(xy11 + z);
        GradientNoise.Gradient g001 = this.permuteToGrad(xy00 + z + 1);
        GradientNoise.Gradient g101 = this.permuteToGrad(xy10 + z + 1);
        GradientNoise.Gradient g011 = this.permuteToGrad(xy01 + z + 1);
        GradientNoise.Gradient g111 = this.permuteToGrad(xy11 + z + 1);
        float d000 = g000.dot(xr, yr, zr);
        float d100 = g100.dot(xr - 1.0f, yr, zr);
        float d010 = g010.dot(xr, yr - 1.0f, zr);
        float d110 = g110.dot(xr - 1.0f, yr - 1.0f, zr);
        float d001 = g001.dot(xr, yr, zr - 1.0f);
        float d101 = g101.dot(xr - 1.0f, yr, zr - 1.0f);
        float d011 = g011.dot(xr, yr - 1.0f, zr - 1.0f);
        float d111 = g111.dot(xr - 1.0f, yr - 1.0f, zr - 1.0f);
        float xAlpha = Mth.smoothstep(xr);
        float yAlpha = Mth.smoothstep(yr);
        float zAlpha = Mth.smoothstep(zr);
        float d1x = Mth.lerp3(xAlpha, yAlpha, zAlpha, g000.x(), g100.x(), g010.x(), g110.x(), g001.x(), g101.x(), g011.x(), g111.x());
        float d1y = Mth.lerp3(xAlpha, yAlpha, zAlpha, g000.y(), g100.y(), g010.y(), g110.y(), g001.y(), g101.y(), g011.y(), g111.y());
        float d1z = Mth.lerp3(xAlpha, yAlpha, zAlpha, g000.z(), g100.z(), g010.z(), g110.z(), g001.z(), g101.z(), g011.z(), g111.z());
        float d2x = Mth.lerp2(yAlpha, zAlpha, d100 - d000, d110 - d010, d101 - d001, d111 - d011);
        float d2y = Mth.lerp2(zAlpha, xAlpha, d010 - d000, d011 - d001, d110 - d100, d111 - d101);
        float d2z = Mth.lerp2(xAlpha, yAlpha, d001 - d000, d101 - d100, d011 - d010, d111 - d110);
        float xSD = Mth.smoothstepDerivative(xr);
        float ySD = Mth.smoothstepDerivative(yr);
        float zSD = Mth.smoothstepDerivative(zr);
        float dX = d1x + xSD * d2x;
        float dY = d1y + ySD * d2y;
        float dZ = d1z + zSD * d2z;
        derivativeOut[0] = derivativeOut[0] + dX;
        derivativeOut[1] = derivativeOut[1] + dY;
        derivativeOut[2] = derivativeOut[2] + dZ;
        return Mth.lerp3(xAlpha, yAlpha, zAlpha, d000, d100, d010, d110, d001, d101, d011, d111);
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder sb) {
        NoiseUtils.parityNoiseOctaveConfigString(sb, this.offsetX, this.offsetY, this.offsetZ, this.perms);
    }
}


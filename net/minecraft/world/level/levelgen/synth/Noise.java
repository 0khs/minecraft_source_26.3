/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;

public interface Noise {
    public Interval range();

    public float get(double var1, double var3);

    public float get(double var1, double var3, double var5);

    default public void addToVolume(DensityBuffer buffer, DensityVolume volume, double xzScale, double yScale, float amplitude) {
        int index = 0;
        for (int indexZ = 0; indexZ < volume.sizeZ(); ++indexZ) {
            double z = (double)volume.blockZ(indexZ) * xzScale;
            for (int indexX = 0; indexX < volume.sizeX(); ++indexX) {
                double x = (double)volume.blockX(indexX) * xzScale;
                for (int indexY = 0; indexY < volume.sizeY(); ++indexY) {
                    double y = (double)volume.blockY(indexY) * yScale;
                    buffer.addTo(index, amplitude * this.get(x, y, z));
                    ++index;
                }
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import java.util.Arrays;

public class DensityBuffer {
    protected final float[] values;
    protected int size;

    protected DensityBuffer(int size) {
        this.values = new float[size];
        this.size = size;
    }

    public static DensityBuffer createUnpooled(int size) {
        return new DensityBuffer(size);
    }

    public void set(int index, float value) {
        this.values[index] = value;
    }

    public void setRange(int index, int size, float value) {
        Arrays.fill(this.values, index, index + size, value);
    }

    public void addTo(int index, float value) {
        int n = index;
        this.values[n] = this.values[n] + value;
    }

    public float get(int index) {
        return this.values[index];
    }

    public void fill(float value) {
        Arrays.fill(this.values, 0, this.size, value);
    }

    public void copyFrom(DensityBuffer other) {
        if (this.size() != other.size()) {
            throw new IllegalArgumentException("Cannot copy from buffer with size=" + other.size() + ", expected" + this.size());
        }
        System.arraycopy(other.values, 0, this.values, 0, this.size());
    }

    public int capacity() {
        return this.values.length;
    }

    public int size() {
        return this.size;
    }
}


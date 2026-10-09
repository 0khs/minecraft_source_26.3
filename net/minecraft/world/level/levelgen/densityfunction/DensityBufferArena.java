/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;

public interface DensityBufferArena {
    public static final DensityBufferArena GLOBAL = new DensityBufferArena(){

        @Override
        public ScopedDensityBuffer acquire(int size) {
            return new ScopedDensityBuffer(this, size, size);
        }

        @Override
        public void release(ScopedDensityBuffer buffer) {
        }
    };

    public ScopedDensityBuffer acquire(int var1);

    public void release(ScopedDensityBuffer var1);
}


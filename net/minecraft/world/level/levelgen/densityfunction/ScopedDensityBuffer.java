/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferArena;

public class ScopedDensityBuffer
extends DensityBuffer
implements AutoCloseable {
    private final DensityBufferArena arena;
    private int age;
    private boolean closed;

    ScopedDensityBuffer(DensityBufferArena arena, int capacity, int size) {
        super(capacity);
        this.size = size;
        this.arena = arena;
    }

    void restore(int size) {
        if (size > this.values.length) {
            throw new IllegalArgumentException("Cannot set size larger than buffer capacity");
        }
        if (!this.closed) {
            throw new IllegalArgumentException("Buffer is already in use");
        }
        this.age = 0;
        this.closed = false;
        this.size = size;
    }

    int incrementAge() {
        return ++this.age;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.arena.release(this);
    }
}


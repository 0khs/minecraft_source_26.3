/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.clock;

public interface ClockInstance {
    public long totalTicks();

    public float partialTick();

    public float rate();

    public boolean isPaused();
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.clock;

import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.clock.WorldClock;

public interface ClockManager {
    public ClockInstance getInstance(Holder<WorldClock> var1);
}


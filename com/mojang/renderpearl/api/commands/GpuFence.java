/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.commands;

import com.mojang.renderpearl.util.UncheckedAutoCloseable;

public interface GpuFence
extends UncheckedAutoCloseable {
    public static final long NO_TIMEOUT = -1L;

    @Override
    public void close();

    public boolean awaitCompletion(long var1);
}


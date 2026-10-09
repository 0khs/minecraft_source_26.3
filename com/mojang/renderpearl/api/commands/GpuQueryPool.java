/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.commands;

import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.OptionalLong;

public interface GpuQueryPool
extends UncheckedAutoCloseable {
    public int size();

    public OptionalLong getValue(int var1);

    public OptionalLong[] getValues(int var1, int var2);
}


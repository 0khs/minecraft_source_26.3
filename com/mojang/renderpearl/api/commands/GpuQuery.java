/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.commands;

import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.OptionalLong;

public interface GpuQuery
extends UncheckedAutoCloseable {
    public OptionalLong getValue();

    @Override
    public void close();
}


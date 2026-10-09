/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.util;

import org.jspecify.annotations.Nullable;

public interface UncheckedAutoCloseable
extends AutoCloseable {
    @Override
    public void close();

    public static void safeClose(@Nullable UncheckedAutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        closeable.close();
    }
}


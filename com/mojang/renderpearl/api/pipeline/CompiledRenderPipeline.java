/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.api.pipeline;

import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import org.jspecify.annotations.Nullable;

public interface CompiledRenderPipeline
extends UncheckedAutoCloseable {
    public boolean isClosed();

    @FunctionalInterface
    public static interface Pending {
        public static final Pending NULL = () -> null;

        public @Nullable CompiledRenderPipeline finishCompile();
    }
}


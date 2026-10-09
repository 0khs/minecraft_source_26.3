/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.Nullable;

final class Removed {
    public static final Removed INSTANCE = new Removed();
    public static final Codec<Removed> CODEC = MapCodec.unitCodec((Object)INSTANCE);

    private Removed() {
    }

    public String toString() {
        return "<removed>";
    }

    public static boolean isNotRemoved(Object value) {
        return value != INSTANCE;
    }

    public static boolean isRemoved(Object value) {
        return value == INSTANCE;
    }

    public static Object nullToRemoved(@Nullable Object value) {
        return value == null ? INSTANCE : value;
    }

    public static @Nullable Object removedToNull(Object value) {
        return value == INSTANCE ? null : value;
    }
}


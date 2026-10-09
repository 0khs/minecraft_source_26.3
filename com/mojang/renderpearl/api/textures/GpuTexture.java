/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.textures;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface GpuTexture
extends UncheckedAutoCloseable {
    public static final int USAGE_COPY_DST = 1;
    public static final int USAGE_COPY_SRC = 2;
    public static final int USAGE_TEXTURE_BINDING = 4;
    public static final int USAGE_RENDER_ATTACHMENT = 8;
    public static final int USAGE_CUBEMAP_COMPATIBLE = 16;

    public int getWidth(int var1);

    public int getHeight(int var1);

    public int getDepthOrLayers();

    public int getMipLevels();

    public GpuFormat getFormat();

    public @Usage int usage();

    public String getLabel();

    public boolean isClosed();

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Usage {
    }
}


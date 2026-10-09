/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client.renderer.texture;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MipmapStrategy implements StringRepresentable
{
    AUTO("auto"),
    MEAN("mean"),
    CUTOUT("cutout"),
    STRICT_CUTOUT("strict_cutout"),
    DARK_CUTOUT("dark_cutout");

    public static final Codec<MipmapStrategy> CODEC;
    private final String name;

    private MipmapStrategy(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromValues(MipmapStrategy::values);
    }
}


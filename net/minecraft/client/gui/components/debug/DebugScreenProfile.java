/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debug;

import net.minecraft.util.StringRepresentable;

public enum DebugScreenProfile implements StringRepresentable
{
    DEFAULT("default", "debug.options.profile.default"),
    PERFORMANCE("performance", "debug.options.profile.performance");

    public static final StringRepresentable.EnumCodec<DebugScreenProfile> CODEC;
    private final String name;
    private final String translationKey;

    private DebugScreenProfile(String name, String translationKey) {
        this.name = name;
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return this.translationKey;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(DebugScreenProfile::values);
    }
}


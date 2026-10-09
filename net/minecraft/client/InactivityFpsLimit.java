/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum InactivityFpsLimit implements StringRepresentable
{
    MINIMIZED("minimized", "options.inactivityFpsLimit.minimized"),
    AFK("afk", "options.inactivityFpsLimit.afk");

    public static final Codec<InactivityFpsLimit> CODEC;
    private final String serializedName;
    private final Component caption;

    private InactivityFpsLimit(String serializedName, String key) {
        this.serializedName = serializedName;
        this.caption = Component.translatable(key);
    }

    public Component caption() {
        return this.caption;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    static {
        CODEC = StringRepresentable.fromEnum(InactivityFpsLimit::values);
    }
}


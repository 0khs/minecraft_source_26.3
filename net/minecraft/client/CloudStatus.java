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

public enum CloudStatus implements StringRepresentable
{
    OFF("false", "options.off"),
    FAST("fast", "options.clouds.fast"),
    FANCY("true", "options.clouds.fancy");

    public static final Codec<CloudStatus> CODEC;
    private final String legacyName;
    private final Component caption;

    private CloudStatus(String legacyName, String key) {
        this.legacyName = legacyName;
        this.caption = Component.translatable(key);
    }

    public Component caption() {
        return this.caption;
    }

    @Override
    public String getSerializedName() {
        return this.legacyName;
    }

    static {
        CODEC = StringRepresentable.fromEnum(CloudStatus::values);
    }
}


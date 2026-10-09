/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client.gui.components.debug;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DebugScreenEntryStatus implements StringRepresentable
{
    ALWAYS_ON("alwaysOn"),
    IN_OVERLAY("inOverlay"),
    NEVER("never");

    public static final Codec<DebugScreenEntryStatus> CODEC;
    private final String name;

    private DebugScreenEntryStatus(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(DebugScreenEntryStatus::values);
    }
}


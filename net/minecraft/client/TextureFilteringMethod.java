/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

public enum TextureFilteringMethod {
    NONE(0, "options.textureFiltering.none"),
    RGSS(1, "options.textureFiltering.rgss"),
    ANISOTROPIC(2, "options.textureFiltering.anisotropic");

    private static final IntFunction<TextureFilteringMethod> BY_ID;
    public static final Codec<TextureFilteringMethod> LEGACY_CODEC;
    private final int id;
    private final Component caption;

    private TextureFilteringMethod(int id, String key) {
        this.id = id;
        this.caption = Component.translatable(key);
    }

    public Component caption() {
        return this.caption;
    }

    static {
        BY_ID = ByIdMap.continuous(p -> p.id, TextureFilteringMethod.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, p -> p.id);
    }
}


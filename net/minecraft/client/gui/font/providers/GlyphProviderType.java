/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.SpaceProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.ProviderReferenceDefinition;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import net.minecraft.util.StringRepresentable;

public enum GlyphProviderType implements StringRepresentable
{
    BITMAP("bitmap", BitmapProvider.Definition.CODEC),
    TTF("ttf", TrueTypeGlyphProviderDefinition.CODEC),
    SPACE("space", SpaceProvider.Definition.CODEC),
    UNIHEX("unihex", UnihexProvider.Definition.CODEC),
    REFERENCE("reference", ProviderReferenceDefinition.CODEC);

    public static final Codec<GlyphProviderType> CODEC;
    private final String name;
    private final MapCodec<? extends GlyphProviderDefinition> codec;

    private GlyphProviderType(String name, MapCodec<? extends GlyphProviderDefinition> codec) {
        this.name = name;
        this.codec = codec;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public MapCodec<? extends GlyphProviderDefinition> mapCodec() {
        return this.codec;
    }

    static {
        CODEC = StringRepresentable.fromEnum(GlyphProviderType::values);
    }
}


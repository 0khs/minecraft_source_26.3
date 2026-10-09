/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.metadata.texture;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record PaletteMetadataSection(Identifier basePalette) {
    public static final Codec<PaletteMetadataSection> CODEC = RecordCodecBuilder.create(i -> i.group((App)Identifier.CODEC.fieldOf("base_palette").forGetter(PaletteMetadataSection::basePalette)).apply((Applicative)i, PaletteMetadataSection::new));
    public static final MetadataSectionType<PaletteMetadataSection> TYPE = new MetadataSectionType<PaletteMetadataSection>("palette", CODEC);
}


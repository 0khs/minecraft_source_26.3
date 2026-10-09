/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client.resources.metadata.language;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record LanguageMetadataSection(Map<String, LanguageInfo> languages) {
    public static final Codec<String> LANGUAGE_CODE_CODEC = Codec.string((int)1, (int)16);
    public static final Codec<LanguageMetadataSection> CODEC = Codec.unboundedMap(LANGUAGE_CODE_CODEC, LanguageInfo.CODEC).xmap(LanguageMetadataSection::new, LanguageMetadataSection::languages);
    public static final MetadataSectionType<LanguageMetadataSection> TYPE = new MetadataSectionType<LanguageMetadataSection>("language", CODEC);
}


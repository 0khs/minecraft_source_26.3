/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.RandomSource;

public interface GlyphSource {
    public BakedGlyph getGlyph(int var1);

    public BakedGlyph getRandomGlyph(RandomSource var1, int var2);
}


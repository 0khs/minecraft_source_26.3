/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.font;

import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.RandomSource;

public record SingleSpriteSource(BakedGlyph glyph) implements GlyphSource
{
    @Override
    public BakedGlyph getGlyph(int codepoint) {
        return this.glyph;
    }

    @Override
    public BakedGlyph getRandomGlyph(RandomSource random, int width) {
        return this.glyph;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;

public enum TextAlignment {
    LEFT{

        @Override
        public int calculateLeft(int anchor, int width) {
            return anchor;
        }

        @Override
        public int calculateLeft(int anchor, Font font, FormattedCharSequence text) {
            return anchor;
        }
    }
    ,
    CENTER{

        @Override
        public int calculateLeft(int anchor, int width) {
            return anchor - width / 2;
        }
    }
    ,
    RIGHT{

        @Override
        public int calculateLeft(int anchor, int width) {
            return anchor - width;
        }
    };


    public abstract int calculateLeft(int var1, int var2);

    public int calculateLeft(int anchor, Font font, FormattedCharSequence text) {
        return this.calculateLeft(anchor, font.width(text));
    }
}


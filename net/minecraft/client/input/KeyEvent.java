/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraft.client.input.InputWithModifiers;

public record KeyEvent(@InputConstants.Value int key, int keycode, @InputWithModifiers.Modifiers int modifiers) implements InputWithModifiers
{
    @Override
    public int input() {
        return this.key;
    }

    @Override
    public int shortcutKey() {
        return this.keycode;
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Action {
    }
}


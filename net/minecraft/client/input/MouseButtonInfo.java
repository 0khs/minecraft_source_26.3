/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraft.client.input.InputWithModifiers;

public record MouseButtonInfo(@MouseButton int button, @InputWithModifiers.Modifiers int modifiers) implements InputWithModifiers
{
    @Override
    public @MouseButton int input() {
        return this.button;
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface MouseButton {
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Action {
    }
}


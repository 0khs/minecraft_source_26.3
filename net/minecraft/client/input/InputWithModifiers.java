/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraft.client.input.InputQuirks;

public interface InputWithModifiers {
    public @InputConstants.Value int input();

    default public int shortcutKey() {
        return this.input();
    }

    public @Modifiers int modifiers();

    default public boolean isSelection() {
        return this.input() == 40 || this.input() == 44 || this.input() == 88;
    }

    default public boolean isConfirmation() {
        return this.input() == 40 || this.input() == 88;
    }

    default public boolean isEscape() {
        return this.input() == 41;
    }

    default public boolean isLeft() {
        return this.input() == 80;
    }

    default public boolean isRight() {
        return this.input() == 79;
    }

    default public boolean isUp() {
        return this.input() == 82;
    }

    default public boolean isDown() {
        return this.input() == 81;
    }

    default public boolean isCycleFocus() {
        return this.input() == 43;
    }

    default public boolean hasAltDown() {
        return (this.modifiers() & 0x300) != 0;
    }

    default public boolean hasShiftDown() {
        return (this.modifiers() & 3) != 0;
    }

    default public boolean hasControlDown() {
        return (this.modifiers() & 0xC0) != 0;
    }

    default public boolean hasControlDownWithQuirk() {
        return (this.modifiers() & InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER) != 0;
    }

    default public boolean isSelectAll() {
        return this.shortcutKey() == 97 && this.hasControlDownWithQuirk() && !this.hasShiftDown() && !this.hasAltDown();
    }

    default public boolean isCopy() {
        return this.shortcutKey() == 99 && this.hasControlDownWithQuirk() && !this.hasShiftDown() && !this.hasAltDown();
    }

    default public boolean isPaste() {
        return this.shortcutKey() == 118 && this.hasControlDownWithQuirk() && !this.hasShiftDown() && !this.hasAltDown();
    }

    default public boolean isCut() {
        return this.shortcutKey() == 120 && this.hasControlDownWithQuirk() && !this.hasShiftDown() && !this.hasAltDown();
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.TYPE_USE})
    public static @interface Modifiers {
    }
}


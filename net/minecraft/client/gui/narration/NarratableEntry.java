/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.narration;

import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.narration.NarrationSupplier;

public interface NarratableEntry
extends NarrationSupplier,
TabOrderedElement {
    public NarrationPriority narrationPriority();

    default public boolean isActive() {
        return true;
    }

    default public Collection<? extends NarratableEntry> getNarratables() {
        return List.of(this);
    }

    public static enum NarrationPriority {
        NONE,
        HOVERED,
        FOCUSED;


        public boolean isTerminal() {
            return this == FOCUSED;
        }
    }
}


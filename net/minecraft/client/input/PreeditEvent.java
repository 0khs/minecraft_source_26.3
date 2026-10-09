/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public record PreeditEvent(String fullText, int caretPosition, List<String> blocks, int focusedBlock) {
    public PreeditEvent {
        Preconditions.checkElementIndex((int)focusedBlock, (int)blocks.size());
    }

    public static @Nullable PreeditEvent fromSdlTextEditing(@Nullable String text, int selectionStart, int selectionLength) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        int totalCodepoints = text.codePointCount(0, text.length());
        int caretCodepoint = selectionStart < 0 ? totalCodepoints : Math.min(selectionStart, totalCodepoints);
        int caretChar = text.offsetByCodePoints(0, caretCodepoint);
        if (selectionLength > 0) {
            int selectionEndCodepoint = Math.min(caretCodepoint + selectionLength, totalCodepoints);
            int selectionEndChar = text.offsetByCodePoints(0, selectionEndCodepoint);
            if (selectionEndChar <= caretChar) {
                return new PreeditEvent(text, caretChar, List.of(text), 0);
            }
            ImmutableList.Builder blocks = ImmutableList.builder();
            int focusedBlock = 0;
            if (caretChar > 0) {
                blocks.add((Object)text.substring(0, caretChar));
                focusedBlock = 1;
            }
            blocks.add((Object)text.substring(caretChar, selectionEndChar));
            if (selectionEndChar < text.length()) {
                blocks.add((Object)text.substring(selectionEndChar));
            }
            return new PreeditEvent(text, caretChar, (List<String>)blocks.build(), focusedBlock);
        }
        return new PreeditEvent(text, caretChar, (List<String>)ImmutableList.of((Object)text), 0);
    }

    public MutableComponent toFormattedText(Style focusedStyle) {
        int blockCount = this.blocks.size();
        if (blockCount == 1) {
            return Component.literal(this.blocks.getFirst()).withStyle(focusedStyle);
        }
        MutableComponent result = Component.empty();
        for (int i = 0; i < blockCount; ++i) {
            MutableComponent part = Component.literal(this.blocks.get(i));
            if (i == this.focusedBlock) {
                part.withStyle(focusedStyle);
            }
            result.append(part);
        }
        return result;
    }
}


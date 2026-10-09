/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.texture;

import net.minecraft.client.renderer.texture.DynamicAtlasTreeSlot;
import org.jspecify.annotations.Nullable;

public class DynamicAtlasTree
implements DynamicAtlasTreeSlot {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private @Nullable DynamicAtlasTree left;
    private @Nullable DynamicAtlasTree right;
    private boolean occupied;

    public DynamicAtlasTree(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public @Nullable DynamicAtlasTreeSlot insert(int slotWidth, int slotHeight, int spacing) {
        return this.insertInner(slotWidth, slotHeight, spacing);
    }

    private @Nullable DynamicAtlasTree insertInner(int slotWidth, int slotHeight, int spacing) {
        if (this.left != null && this.right != null) {
            DynamicAtlasTree newNode = this.left.insertInner(slotWidth, slotHeight, spacing);
            if (newNode == null) {
                newNode = this.right.insertInner(slotWidth, slotHeight, spacing);
            }
            return newNode;
        }
        if (this.occupied) {
            return null;
        }
        if (slotWidth > this.width || slotHeight > this.height) {
            return null;
        }
        if (slotWidth == this.width && slotHeight == this.height) {
            this.occupied = true;
            return this;
        }
        int deltaWidth = this.width - slotWidth;
        int deltaHeight = this.height - slotHeight;
        if (deltaWidth > deltaHeight) {
            this.left = new DynamicAtlasTree(this.x, this.y, slotWidth, this.height);
            this.right = new DynamicAtlasTree(this.x + slotWidth + spacing, this.y, this.width - slotWidth - spacing, this.height);
        } else {
            this.left = new DynamicAtlasTree(this.x, this.y, this.width, slotHeight);
            this.right = new DynamicAtlasTree(this.x, this.y + slotHeight + spacing, this.width, this.height - slotHeight - spacing);
        }
        return this.left.insertInner(slotWidth, slotHeight, spacing);
    }

    @Override
    public int x() {
        return this.x;
    }

    @Override
    public int y() {
        return this.y;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }
}


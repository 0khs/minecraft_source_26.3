/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.player;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemActivation {
    public static final int ANIMATION_LENGTH = 40;
    private @Nullable ItemStack item;
    private int ticks;
    private float offX;
    private float offY;

    public void tick() {
        if (this.ticks > 0) {
            --this.ticks;
            if (this.ticks == 0) {
                this.item = null;
            }
        }
    }

    public void reset() {
        this.item = null;
        this.ticks = 0;
    }

    public void activate(ItemStack itemStack, RandomSource random) {
        this.item = itemStack.copy();
        this.ticks = 40;
        this.offX = random.nextFloat() * 2.0f - 1.0f;
        this.offY = random.nextFloat() * 2.0f - 1.0f;
    }

    public boolean isActive() {
        return this.item != null && this.ticks > 0;
    }

    public ItemStack item() {
        return this.item;
    }

    public int ticks() {
        return this.ticks;
    }

    public float offX() {
        return this.offX;
    }

    public float offY() {
        return this.offY;
    }
}


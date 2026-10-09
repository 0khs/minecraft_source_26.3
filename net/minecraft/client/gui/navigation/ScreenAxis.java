/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.navigation;

import net.minecraft.client.gui.navigation.ScreenDirection;

public enum ScreenAxis {
    HORIZONTAL,
    VERTICAL;


    public ScreenAxis orthogonal() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> VERTICAL;
            case 1 -> HORIZONTAL;
        };
    }

    public ScreenDirection getPositive() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ScreenDirection.RIGHT;
            case 1 -> ScreenDirection.DOWN;
        };
    }

    public ScreenDirection getNegative() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ScreenDirection.LEFT;
            case 1 -> ScreenDirection.UP;
        };
    }

    public ScreenDirection getDirection(boolean positive) {
        return positive ? this.getPositive() : this.getNegative();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

public enum MoverType {
    SELF,
    PLAYER,
    PISTON,
    SHULKER_BOX,
    SHULKER;


    public boolean isServerAndClientSimulated() {
        return this != SELF;
    }
}


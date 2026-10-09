/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

public interface HolderOwner<T> {
    default public boolean canSerialize(HolderOwner<T> owner) {
        return owner == this;
    }
}


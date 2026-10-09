/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContextAccess;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContext<T>
extends BootstrapContextAccess {
    public Holder.Reference<T> register(ResourceKey<T> var1, T var2);
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.registries;

import net.minecraft.data.worldgen.BootstrapContext;

@FunctionalInterface
public interface SingleRegistryBootstrap<T> {
    public void run(BootstrapContext<T> var1);
}


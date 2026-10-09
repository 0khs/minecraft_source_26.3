/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.registries;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public interface MultiRegistryBootstrap {
    public Set<ResourceKey<? extends Registry<?>>> requestedRegistries();

    public void run(BootstrapGetter var1);

    public static interface BootstrapGetter {
        public <T> BootstrapContext<T> get(ResourceKey<? extends Registry<T>> var1);
    }
}


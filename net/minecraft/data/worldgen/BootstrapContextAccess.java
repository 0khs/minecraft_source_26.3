/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContextAccess {
    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> var1);

    @Deprecated
    public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> var1);
}


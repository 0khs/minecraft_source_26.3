/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.textures;

import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.OptionalDouble;

public interface GpuSampler
extends UncheckedAutoCloseable {
    public AddressMode getAddressModeU();

    public AddressMode getAddressModeV();

    public FilterMode getMinFilter();

    public FilterMode getMagFilter();

    public int getMaxAnisotropy();

    public OptionalDouble getMaxLod();

    public boolean isClosed();
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.api;

import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.Collection;

public interface GpuSurfaceBackend
extends UncheckedAutoCloseable {
    public void configure(GpuSurface.Configuration var1) throws SurfaceException;

    public boolean isSuboptimal();

    public void acquireNextTexture() throws SurfaceException;

    public void blitFromTexture(CommandEncoderBackend var1, GpuTextureView var2);

    public void present();

    public Collection<GpuSurface.PresentMode> supportedPresentModes();
}


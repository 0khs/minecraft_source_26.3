/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.frontend;

import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import java.util.Collection;
import java.util.Optional;

public class FrontendGpuSurface
implements GpuSurface {
    private final GpuSurfaceBackend backend;
    private boolean hasImageAcquired = false;
    private boolean hasBlittedTexture = false;
    private Optional<GpuSurface.Configuration> currentConfiguration = Optional.empty();

    public FrontendGpuSurface(GpuSurfaceBackend backend) {
        this.backend = backend;
    }

    @Override
    public void close() {
        if (this.hasImageAcquired) {
            throw new IllegalStateException("Cannot close a surface while it is acquired");
        }
        this.backend.close();
    }

    @Override
    public void configure(GpuSurface.Configuration config) throws SurfaceException {
        if (this.hasImageAcquired) {
            throw new IllegalStateException("Cannot configure a surface while it is acquired");
        }
        if (!this.supportedPresentModes().contains((Object)config.presentMode())) {
            throw new SurfaceException("Surface does not support present mode " + String.valueOf((Object)config.presentMode()) + " (supported: " + String.valueOf(this.supportedPresentModes()) + ")");
        }
        this.backend.configure(config);
        this.currentConfiguration = Optional.of(config);
    }

    @Override
    public Optional<GpuSurface.Configuration> currentConfiguration() {
        return this.currentConfiguration;
    }

    @Override
    public Collection<GpuSurface.PresentMode> supportedPresentModes() {
        return this.backend.supportedPresentModes();
    }

    @Override
    public boolean isSuboptimal() {
        return this.backend.isSuboptimal();
    }

    @Override
    public boolean isAcquired() {
        return this.hasImageAcquired;
    }

    @Override
    public void acquireNextTexture() throws SurfaceException {
        if (this.hasImageAcquired) {
            throw new IllegalStateException("Cannot acquire a surface while it is already acquired");
        }
        if (this.currentConfiguration.isEmpty()) {
            throw new IllegalStateException("Cannot acquire an unconfigured surface");
        }
        this.backend.acquireNextTexture();
        this.hasImageAcquired = true;
        this.hasBlittedTexture = false;
    }

    @Override
    public void blitFromTexture(CommandEncoder commandEncoder, GpuTextureView textureView) {
        if (!(commandEncoder instanceof FrontendCommandEncoder)) {
            throw new IllegalArgumentException("CommandEncoder must be instance of FrontendCommandEncoder");
        }
        FrontendCommandEncoder frontendCommandEncoder = (FrontendCommandEncoder)commandEncoder;
        if (frontendCommandEncoder.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before presenting with a command encoder");
        }
        if (!textureView.texture().getFormat().hasColorAspect()) {
            throw new IllegalStateException("Cannot present a non-color texture!");
        }
        if ((textureView.texture().usage() & 2) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_SRC to presented to the screen");
        }
        if (textureView.texture().getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for presentation");
        }
        if (!this.hasImageAcquired) {
            throw new IllegalStateException("Cannot present to an unacquired surface");
        }
        if (this.hasBlittedTexture) {
            throw new IllegalStateException("Already blitted to this frame!");
        }
        this.backend.blitFromTexture(frontendCommandEncoder.backend(), textureView);
        this.hasBlittedTexture = true;
    }

    @Override
    public void present() {
        if (!this.hasImageAcquired) {
            throw new IllegalStateException("Cannot present to a surface if it isn't acquired");
        }
        if (!this.hasBlittedTexture) {
            throw new IllegalStateException("Must blit to surface before presenting!");
        }
        this.backend.present();
        this.hasImageAcquired = false;
    }
}


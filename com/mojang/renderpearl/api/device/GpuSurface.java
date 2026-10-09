/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.device;

import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import java.util.Collection;
import java.util.Optional;

public interface GpuSurface
extends UncheckedAutoCloseable {
    public void configure(Configuration var1) throws SurfaceException;

    public Optional<Configuration> currentConfiguration();

    public Collection<PresentMode> supportedPresentModes();

    public boolean isSuboptimal();

    public boolean isAcquired();

    public void acquireNextTexture() throws SurfaceException;

    public void blitFromTexture(CommandEncoder var1, GpuTextureView var2);

    public void present();

    public static enum PresentMode {
        IMMEDIATE,
        MAILBOX,
        FIFO,
        FIFO_RELAXED;

        private static final PresentMode[] PRESENT_MODES_VSYNC;
        private static final PresentMode[] PRESENT_MODES_NO_VSYNC;

        public static PresentMode getSupportedVsyncMode(Collection<PresentMode> supportedModes, boolean vsync) {
            PresentMode[] preferred;
            for (PresentMode mode : preferred = vsync ? PRESENT_MODES_VSYNC : PRESENT_MODES_NO_VSYNC) {
                if (!supportedModes.contains((Object)mode)) continue;
                return mode;
            }
            throw new IllegalStateException("No supported presentation mode was found");
        }

        static {
            PRESENT_MODES_VSYNC = new PresentMode[]{FIFO};
            PRESENT_MODES_NO_VSYNC = new PresentMode[]{IMMEDIATE, MAILBOX, FIFO};
        }
    }

    public record Configuration(int width, int height, PresentMode presentMode) {
    }
}


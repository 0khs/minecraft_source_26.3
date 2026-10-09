/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.sdl.SDLError
 *  org.lwjgl.sdl.SDLHints
 *  org.lwjgl.sdl.SDLPlatform
 *  org.lwjgl.sdl.SDLSurface
 *  org.lwjgl.sdl.SDLVideo
 *  org.lwjgl.sdl.SDL_DisplayMode
 *  org.lwjgl.sdl.SDL_Event
 *  org.lwjgl.sdl.SDL_Rect
 *  org.lwjgl.sdl.SDL_Surface
 *  org.lwjgl.system.MemoryStack
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.MonitorManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.device.GpuBackend;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLPlatform;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

public final class Window
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_WINDOW_WIDTH = 320;
    public static final int MIN_WINDOW_HEIGHT = 240;
    public static final int BASE_WIDTH = 320;
    public static final int BASE_HEIGHT = 240;
    private static final int BORDERLESS_FULLSCREEN_PADDING = 1;
    private final WindowEventHandler eventHandler;
    private final MonitorManager monitorManager;
    private final long handle;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private Optional<VideoMode> preferredFullscreenVideoMode;
    private boolean fullscreenRequested;
    private boolean fullscreen;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int guiScaledWidth;
    private int guiScaledHeight;
    private int guiScale;
    private String errorSection = "Startup";
    private boolean dirty;
    private boolean iconified;
    private boolean focused = true;
    private boolean shouldClose;
    private @Nullable Runnable closeCallback;
    private boolean allowCursorChanges;
    private boolean quitShortcuts;
    private CursorType currentCursor = CursorType.DEFAULT;
    private boolean exclusiveFullscreen;
    private boolean borderlessFullscreen;

    public Window(WindowEventHandler eventHandler, DisplayData displayData, @Nullable String fullscreenVideoModeString, boolean exclusiveFullscreen, String title, MonitorManager monitorManager, GpuBackend backend) {
        this(eventHandler, displayData, fullscreenVideoModeString, exclusiveFullscreen, title, monitorManager, backend, 0);
    }

    public Window(WindowEventHandler eventHandler, DisplayData displayData, @Nullable String fullscreenVideoModeString, boolean exclusiveFullscreen, String title, MonitorManager monitorManager, GpuBackend backend, int maximumSize) {
        this.monitorManager = monitorManager;
        this.exclusiveFullscreen = exclusiveFullscreen;
        this.eventHandler = eventHandler;
        Optional<VideoMode> optionsMode = VideoMode.read(fullscreenVideoModeString);
        this.preferredFullscreenVideoMode = optionsMode.isPresent() ? optionsMode : (displayData.fullscreenWidth().isPresent() && displayData.fullscreenHeight().isPresent() ? Optional.of(new VideoMode(displayData.fullscreenWidth().getAsInt(), displayData.fullscreenHeight().getAsInt(), 8, 8, 8, 60)) : Optional.empty());
        this.fullscreenRequested = displayData.isFullscreen();
        Monitor initialMonitor = monitorManager.getMonitor(SDLVideo.SDL_GetPrimaryDisplay());
        this.windowedWidth = this.width = Window.allowedWindowMinSize(displayData.width(), 320);
        this.windowedHeight = this.height = Window.allowedWindowMinSize(displayData.height(), 240);
        this.handle = this.createWindow(backend, this.width, this.height, title);
        this.setWindowMaxSize(maximumSize, maximumSize);
        MacosUtil.disableCloseWindowMenuItem();
        if (initialMonitor != null) {
            this.windowedX = this.x = initialMonitor.x() + (initialMonitor.w() - this.width) / 2;
            this.windowedY = this.y = initialMonitor.y() + (initialMonitor.h() - this.height) / 2;
        } else {
            try (MemoryStack stack = MemoryStack.stackPush();){
                IntBuffer actualX = stack.mallocInt(1);
                IntBuffer actualY = stack.mallocInt(1);
                if (!SDLVideo.SDL_GetWindowPosition((long)this.handle, (IntBuffer)actualX, (IntBuffer)actualY)) {
                    throw new IllegalStateException("Failed to query initial window position: " + SDLError.SDL_GetError());
                }
                this.windowedX = this.x = actualX.get(0);
                this.windowedY = this.y = actualY.get(0);
            }
        }
        this.setMode();
        this.refreshFramebufferSize();
    }

    public static String getPlatform() {
        String platform = SDLPlatform.SDL_GetPlatform();
        if (platform == null) {
            return "unknown platform";
        }
        return platform;
    }

    private static @Nullable SDL_Surface createIconSurface(NativeImage image) {
        int pitch = image.getWidth() * 4;
        return SDLSurface.SDL_CreateSurfaceFrom((int)image.getWidth(), (int)image.getHeight(), (int)376840196, (ByteBuffer)image.getPixelBytes(), (int)pitch);
    }

    private long createWindow(GpuBackend backend, int width, int height, String title) {
        long flags = 8224L;
        long windowHandle = backend.createWindow(title, width, height, 8224L);
        if (windowHandle == 0L) {
            throw new IllegalStateException("Failed to create window: " + Objects.requireNonNullElse(SDLError.SDL_GetError(), "<no error>"));
        }
        SDLVideo.SDL_SetWindowMinimumSize((long)windowHandle, (int)320, (int)240);
        LOGGER.info("Created window using SDL video driver: {}", (Object)SDLVideo.SDL_GetCurrentVideoDriver());
        return windowHandle;
    }

    public @Nullable VideoMode getActiveVideoMode() {
        RenderSystem.assertOnRenderThread();
        SDL_DisplayMode mode = this.getActiveDisplayMode();
        return mode == null ? null : new VideoMode(mode);
    }

    private @Nullable SDL_DisplayMode getActiveDisplayMode() {
        SDL_DisplayMode windowMode = SDLVideo.SDL_GetWindowFullscreenMode((long)this.handle);
        if (windowMode != null) {
            return windowMode;
        }
        int displayId = SDLVideo.SDL_GetDisplayForWindow((long)this.handle);
        return displayId == 0 ? null : SDLVideo.SDL_GetCurrentDisplayMode((int)displayId);
    }

    public boolean shouldClose() {
        return this.shouldClose;
    }

    public void handleEvent(SDL_Event event) {
        switch (event.type()) {
            case 338: {
                this.monitorManager.onDisplayConnected(event.display().displayID());
                break;
            }
            case 339: {
                this.monitorManager.onDisplayDisconnected(event.display().displayID());
                break;
            }
            case 342: {
                this.onDisplayModeChanged(event.display().displayID());
                break;
            }
            case 519: {
                this.onFramebufferResize(event.window().data1(), event.window().data2());
                break;
            }
            case 518: {
                this.onResize(event.window().data1(), event.window().data2());
                break;
            }
            case 517: {
                this.onMove(event.window().data1(), event.window().data2());
                break;
            }
            case 526: {
                this.onFocus(true);
                break;
            }
            case 527: {
                this.onFocus(false);
                break;
            }
            case 524: 
            case 525: {
                this.eventHandler.cursorEntered();
                break;
            }
            case 521: {
                this.onIconified(true);
                break;
            }
            case 522: 
            case 523: {
                this.onIconified(false);
                break;
            }
            case 531: {
                this.eventHandler.framebufferSizeChanged();
                break;
            }
            case 535: 
            case 536: {
                this.updateFullscreenState();
                break;
            }
            case 256: 
            case 528: {
                this.onQuitRequested();
                break;
            }
            case 257: {
                this.requestClose();
                break;
            }
        }
    }

    private void onDisplayModeChanged(int displayId) {
        this.monitorManager.onDisplayModeChanged(displayId);
        this.refreshFramebufferSize();
        this.eventHandler.framebufferSizeChanged();
    }

    private void onQuitRequested() {
        if (this.quitShortcuts || !InputQuirks.isQuitShortcutDown()) {
            this.requestClose();
        }
    }

    private void updateFullscreenState() {
        boolean newFullscreen = this.isWindowFullscreen();
        if (this.fullscreen != newFullscreen) {
            this.fullscreen = newFullscreen;
            this.eventHandler.fullscreenStateChanged(newFullscreen);
        }
    }

    private void requestClose() {
        this.shouldClose = true;
        if (this.closeCallback != null) {
            this.closeCallback.run();
        }
    }

    public void setIcon(PackMetadataResources resources, IconSet iconSet) throws IOException {
        Util.OS platform = Util.getPlatform();
        switch (platform) {
            case WINDOWS: 
            case LINUX: 
            case SOLARIS: 
            case OSX: {
                this.setIcon(iconSet.getStandardIcons(resources));
                break;
            }
            default: {
                LOGGER.warn("Not setting icon for unrecognized platform: {}", (Object)platform);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setIcon(List<IoSupplier<InputStream>> iconStreams) throws IOException {
        if (iconStreams.isEmpty()) {
            return;
        }
        ArrayList<NativeImage> images = new ArrayList<NativeImage>(iconStreams.size());
        try {
            SDL_Surface primarySurface = Window.createIconSurface(iconStreams.getFirst(), images);
            if (primarySurface == null) {
                return;
            }
            for (IoSupplier<InputStream> iconStream : iconStreams.subList(1, iconStreams.size())) {
                SDL_Surface surface = Window.createIconSurface(iconStream, images);
                if (surface == null) continue;
                if (!SDLSurface.SDL_AddSurfaceAlternateImage((SDL_Surface)primarySurface, (SDL_Surface)surface)) {
                    LOGGER.warn("Failed to add {}x{} icon as alternate window icon image: {}", new Object[]{surface.w(), surface.h(), SDLError.SDL_GetError()});
                }
                SDLSurface.SDL_DestroySurface((SDL_Surface)surface);
            }
            if (!SDLVideo.SDL_SetWindowIcon((long)this.handle, (SDL_Surface)primarySurface)) {
                LOGGER.warn("Failed to set window icon: {}", (Object)SDLError.SDL_GetError());
            }
            SDLSurface.SDL_DestroySurface((SDL_Surface)primarySurface);
        }
        finally {
            images.forEach(NativeImage::close);
        }
    }

    private static @Nullable SDL_Surface createIconSurface(IoSupplier<InputStream> iconStream, List<NativeImage> images) throws IOException {
        NativeImage image = NativeImage.read(iconStream.get());
        images.add(image);
        SDL_Surface surface = Window.createIconSurface(image);
        if (surface == null) {
            LOGGER.warn("Failed to create SDL surface for {}x{} icon: {}", new Object[]{image.getWidth(), image.getHeight(), SDLError.SDL_GetError()});
        }
        return surface;
    }

    public String getErrorSection() {
        return this.errorSection;
    }

    public void setErrorSection(String string) {
        this.errorSection = string;
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        SDLVideo.SDL_DestroyWindow((long)this.handle);
    }

    private void onMove(int x, int y) {
        this.x = x;
        this.y = y;
        if (!this.isWindowFullscreen()) {
            this.windowedX = x;
            this.windowedY = y;
        }
    }

    private void onResize(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        if (!this.isWindowFullscreen()) {
            this.windowedWidth = Window.allowedWindowMinSize(newWidth, 320);
            this.windowedHeight = Window.allowedWindowMinSize(newHeight, 240);
        }
        this.updateWindowMouseGrab();
        if (Minecraft.getInstance().mouseHandler.isMouseGrabbed()) {
            double xpos = (double)this.getScreenWidth() / 2.0;
            double ypos = (double)this.getScreenHeight() / 2.0;
            InputConstants.grabMouse(this, xpos, ypos);
        }
    }

    private void onFramebufferResize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) {
            return;
        }
        int oldWidth = this.getWidth();
        int oldHeight = this.getHeight();
        this.framebufferWidth = newWidth - this.framebufferWidthPadding();
        this.framebufferHeight = newHeight;
        try {
            this.eventHandler.framebufferSizeChanged();
        }
        catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Window resize");
            CrashReportCategory windowSizeDetails = report.addCategory("Window Dimensions");
            windowSizeDetails.setDetail("Old", oldWidth + "x" + oldHeight);
            windowSizeDetails.setDetail("New", newWidth + "x" + newHeight);
            throw new ReportedException(report);
        }
    }

    private void refreshFramebufferSize() {
        FramebufferSize size = this.queryFramebufferSize();
        this.framebufferWidth = size.width() - this.framebufferWidthPadding();
        this.framebufferHeight = size.height();
    }

    private int framebufferWidthPadding() {
        if (this.borderlessFullscreen) {
            return 1;
        }
        return 0;
    }

    public FramebufferSize queryFramebufferSize() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer outWidth = stack.mallocInt(1);
            IntBuffer outHeight = stack.mallocInt(1);
            if (!SDLVideo.SDL_GetWindowSizeInPixels((long)this.handle, (IntBuffer)outWidth, (IntBuffer)outHeight)) {
                throw new IllegalStateException("Failed to query window size in pixels: " + SDLError.SDL_GetError());
            }
            FramebufferSize framebufferSize = new FramebufferSize(Math.max(outWidth.get(0), 1), Math.max(outHeight.get(0), 1));
            return framebufferSize;
        }
    }

    private void onFocus(boolean focused) {
        this.focused = focused;
    }

    private void onIconified(boolean iconified) {
        this.iconified = iconified;
        Minecraft.getInstance().invalidateSurfaceConfiguration();
    }

    public void updateFullscreenIfChanged() {
        RenderSystem.assertOnRenderThread();
        if (this.fullscreenRequested != this.fullscreen) {
            this.setMode();
            this.eventHandler.framebufferSizeChanged();
        }
    }

    public Optional<VideoMode> getPreferredFullscreenVideoMode() {
        return this.preferredFullscreenVideoMode;
    }

    public void setPreferredFullscreenVideoMode(Optional<VideoMode> preferredFullscreenVideoMode) {
        boolean changed = !preferredFullscreenVideoMode.equals(this.preferredFullscreenVideoMode);
        this.preferredFullscreenVideoMode = preferredFullscreenVideoMode;
        if (changed) {
            this.dirty = true;
        }
    }

    public void changeFullscreenVideoMode() {
        RenderSystem.assertOnRenderThread();
        if (this.fullscreenRequested && this.dirty) {
            this.dirty = false;
            this.setMode();
            this.eventHandler.framebufferSizeChanged();
        }
    }

    private void setMode() {
        boolean success;
        RenderSystem.assertOnRenderThread();
        if (this.fullscreenRequested && !this.fullscreen) {
            this.windowedX = this.x;
            this.windowedY = this.y;
            this.windowedWidth = Window.allowedWindowMinSize(this.width, 320);
            this.windowedHeight = Window.allowedWindowMinSize(this.height, 240);
        }
        boolean bl = success = this.fullscreenRequested ? this.applyFullscreen() : this.applyWindowed();
        if (!success) {
            LOGGER.error("Couldn't {} fullscreen: {}", (Object)(this.fullscreenRequested ? "enter" : "leave"), (Object)SDLError.SDL_GetError());
            this.fullscreenRequested = this.fullscreen;
            this.eventHandler.fullscreenStateChanged(this.fullscreen);
            return;
        }
        this.syncWindow();
        this.updateFullscreenState();
        this.refreshFramebufferSize();
        if (!this.isExclusiveFullscreen()) {
            this.updateWindowMouseGrab();
        }
        if (this.exclusiveFullscreen && this.fullscreen && !this.isExclusiveFullscreen()) {
            LOGGER.info("Exclusive fullscreen request resolved to borderless desktop");
        }
    }

    private void updateWindowMouseGrab() {
        boolean shouldGrabMouse;
        boolean bl = shouldGrabMouse = this.fullscreen && this.isExclusiveFullscreen();
        if (!SDLVideo.SDL_SetWindowMouseGrab((long)this.handle, (boolean)shouldGrabMouse)) {
            LOGGER.warn("Failed to update window mouse grab state: {}", (Object)SDLError.SDL_GetError());
        }
    }

    private boolean isWindowFullscreen() {
        return this.borderlessFullscreen || (SDLVideo.SDL_GetWindowFlags((long)this.handle) & 1L) != 0L;
    }

    public boolean isExclusiveFullscreen() {
        return this.isWindowFullscreen() && SDLVideo.SDL_GetWindowFullscreenMode((long)this.handle) != null;
    }

    private boolean applyFullscreen() {
        if (this.useBorderlessFullscreenWindow()) {
            return this.applyBorderlessFullscreenWindow();
        }
        this.leaveBorderlessFullscreenWindow();
        return this.applyFullscreenMode() && SDLVideo.SDL_SetWindowFullscreen((long)this.handle, (boolean)true);
    }

    private boolean useBorderlessFullscreenWindow() {
        return Util.getPlatform() == Util.OS.WINDOWS && !this.exclusiveFullscreen;
    }

    private boolean applySdlBorderlessFullscreen() {
        this.leaveBorderlessFullscreenWindow();
        return this.applyBorderlessFullscreen() && SDLVideo.SDL_SetWindowFullscreen((long)this.handle, (boolean)true);
    }

    private void leaveBorderlessFullscreenWindow() {
        if (!this.borderlessFullscreen) {
            return;
        }
        this.borderlessFullscreen = false;
        if (!SDLVideo.SDL_SetWindowBordered((long)this.handle, (boolean)true)) {
            LOGGER.warn("Failed to restore window decorations: {}", (Object)SDLError.SDL_GetError());
        }
    }

    private boolean applyFullscreenMode() {
        if (!this.exclusiveFullscreen) {
            return this.applyBorderlessFullscreen();
        }
        return this.applyExclusiveFullscreen();
    }

    private boolean applyExclusiveFullscreen() {
        Monitor monitor = this.monitorManager.findBestMonitor(this);
        if (monitor == null) {
            LOGGER.warn("Failed to find suitable monitor for exclusive fullscreen, falling back to borderless fullscreen");
            return this.applyBorderlessFullscreen();
        }
        VideoMode videoMode = monitor.getPreferredVideoMode(this.preferredFullscreenVideoMode);
        LOGGER.info("Exclusive target {} on monitor {}", (Object)videoMode, (Object)monitor);
        try (MemoryStack stack = MemoryStack.stackPush();){
            SDL_DisplayMode mode = SDL_DisplayMode.malloc((MemoryStack)stack);
            if (SDLVideo.SDL_GetClosestFullscreenDisplayMode((int)monitor.id(), (int)videoMode.getWidth(), (int)videoMode.getHeight(), (float)videoMode.getRefreshRate(), (boolean)true, (SDL_DisplayMode)mode)) {
                boolean bl = SDLVideo.SDL_SetWindowFullscreenMode((long)this.handle, (SDL_DisplayMode)mode);
                return bl;
            }
            LOGGER.warn("No matching exclusive fullscreen mode found for {}, falling back to borderless fullscreen", (Object)videoMode);
            boolean bl = this.applyBorderlessFullscreen();
            return bl;
        }
    }

    private boolean applyWindowed() {
        this.leaveBorderlessFullscreenWindow();
        if (!SDLVideo.SDL_SetWindowFullscreen((long)this.handle, (boolean)false)) {
            return false;
        }
        return this.setWindowSizeAndPosition(this.windowedX, this.windowedY, Window.allowedWindowMinSize(this.windowedWidth, 320), Window.allowedWindowMinSize(this.windowedHeight, 240));
    }

    private boolean applyBorderlessFullscreenWindow() {
        Monitor monitor = this.monitorManager.findBestMonitor(this);
        if (monitor == null) {
            LOGGER.warn("Failed to find suitable monitor for borderless fullscreen, falling back to SDL borderless fullscreen");
            return this.applySdlBorderlessFullscreen();
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            SDL_Rect bounds = SDL_Rect.malloc((MemoryStack)stack);
            if (!SDLVideo.SDL_GetDisplayBounds((int)monitor.id(), (SDL_Rect)bounds)) {
                LOGGER.warn("Failed to query bounds of monitor {}, falling back to SDL borderless fullscreen: {}", (Object)monitor, (Object)SDLError.SDL_GetError());
                boolean bl = this.applySdlBorderlessFullscreen();
                return bl;
            }
            if (!this.applyBorderlessFullscreen() || !SDLVideo.SDL_SetWindowFullscreen((long)this.handle, (boolean)false)) {
                boolean bl = false;
                return bl;
            }
            this.restoreWindow();
            this.borderlessFullscreen = true;
            if (!SDLVideo.SDL_SetWindowBordered((long)this.handle, (boolean)false)) {
                LOGGER.warn("Failed to remove window decorations for borderless fullscreen: {}", (Object)SDLError.SDL_GetError());
            }
            boolean bl = this.setWindowSizeAndPosition(bounds.x(), bounds.y(), bounds.w() + 1, bounds.h());
            return bl;
        }
    }

    private void restoreWindow() {
        if (!SDLVideo.SDL_RestoreWindow((long)this.handle)) {
            LOGGER.warn("Failed to restore window before entering fullscreen: {}", (Object)SDLError.SDL_GetError());
            return;
        }
        this.syncWindow();
    }

    private void syncWindow() {
        if (!SDLVideo.SDL_SyncWindow((long)this.handle)) {
            LOGGER.warn("Failed to synchronize SDL window: {}", (Object)SDLError.SDL_GetError());
        }
    }

    private boolean setWindowSizeAndPosition(int windowX, int windowY, int windowWidth, int windowHeight) {
        this.x = windowX;
        this.y = windowY;
        this.width = windowWidth;
        this.height = windowHeight;
        if (!SDLVideo.SDL_SetWindowSize((long)this.handle, (int)this.width, (int)this.height)) {
            return false;
        }
        if (!SDLVideo.SDL_SetWindowPosition((long)this.handle, (int)this.x, (int)this.y)) {
            LOGGER.debug("Window manager declined window positioning: {}", (Object)SDLError.SDL_GetError());
        }
        return true;
    }

    private boolean applyBorderlessFullscreen() {
        return SDLVideo.SDL_SetWindowFullscreenMode((long)this.handle, null);
    }

    public void setExclusiveFullscreen(boolean exclusiveFullscreen) {
        if (this.exclusiveFullscreen != exclusiveFullscreen) {
            this.exclusiveFullscreen = exclusiveFullscreen;
            if (this.fullscreenRequested) {
                this.setMode();
                this.eventHandler.framebufferSizeChanged();
            }
        }
    }

    public void setWindowed(int width, int height) {
        this.windowedWidth = Window.allowedWindowMinSize(width, 320);
        this.windowedHeight = Window.allowedWindowMinSize(height, 240);
        this.fullscreenRequested = false;
        this.setMode();
    }

    public int calculateScale(int maxScale, boolean enforceUnicode) {
        int scale;
        for (scale = 1; scale != maxScale && scale < this.framebufferWidth && scale < this.framebufferHeight && this.framebufferWidth / (scale + 1) >= 320 && this.framebufferHeight / (scale + 1) >= 240; ++scale) {
        }
        if (enforceUnicode && scale % 2 != 0) {
            ++scale;
        }
        return scale;
    }

    public void setTitle(String title) {
        SDLVideo.SDL_SetWindowTitle((long)this.handle, (CharSequence)title);
    }

    public void setWindowMaxSize(int width, int height) {
        SDLVideo.SDL_SetWindowMaximumSize((long)this.handle, (int)width, (int)height);
    }

    public long handle() {
        return this.handle;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreenRequested = fullscreen;
    }

    public boolean isIconified() {
        return this.iconified;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public int getWidth() {
        return this.framebufferWidth;
    }

    public void setWidth(int width) {
        this.framebufferWidth = width;
    }

    public int getHeight() {
        return this.framebufferHeight;
    }

    public void setHeight(int height) {
        this.framebufferHeight = height;
    }

    public int getScreenWidth() {
        return this.width;
    }

    public int getScreenHeight() {
        return this.height;
    }

    public int getGuiScaledWidth() {
        return this.guiScaledWidth;
    }

    public int getGuiScaledHeight() {
        return this.guiScaledHeight;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getGuiScale() {
        return this.guiScale;
    }

    public void setGuiScale(int guiScale) {
        this.guiScale = guiScale;
        this.guiScaledWidth = (int)Math.ceil((double)this.framebufferWidth / (double)guiScale);
        this.guiScaledHeight = (int)Math.ceil((double)this.framebufferHeight / (double)guiScale);
    }

    public float getPixelDensity() {
        float density = SDLVideo.SDL_GetWindowPixelDensity((long)this.handle);
        return density > 0.0f ? density : 1.0f;
    }

    public @Nullable Monitor findBestMonitor() {
        return this.monitorManager.findBestMonitor(this);
    }

    public void setWindowCloseCallback(Runnable task) {
        this.closeCallback = task;
    }

    public void setAllowCursorChanges(boolean value) {
        this.allowCursorChanges = value;
    }

    public void setQuitShortcuts(boolean value) {
        this.quitShortcuts = value;
        SDLHints.SDL_SetHint((CharSequence)"SDL_WINDOWS_CLOSE_ON_ALT_F4", (CharSequence)(value ? "1" : "0"));
    }

    public void selectCursor(CursorType cursor) {
        CursorType effectiveCursor;
        CursorType cursorType = effectiveCursor = this.allowCursorChanges ? cursor : CursorType.DEFAULT;
        if (this.currentCursor != effectiveCursor) {
            this.currentCursor = effectiveCursor;
            effectiveCursor.select();
        }
    }

    public float getAppropriateLineWidth() {
        return Math.max(2.5f, (float)this.getWidth() / 1920.0f * 2.5f);
    }

    private static int allowedWindowMinSize(int size, int minSize) {
        return Math.max(size, minSize);
    }

    public record FramebufferSize(int width, int height) {
    }

    public static class WindowInitFailed
    extends SilentInitException {
        public WindowInitFailed(String message) {
            super(message);
        }
    }
}


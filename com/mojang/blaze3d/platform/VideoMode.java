/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.sdl.SDLPixels
 *  org.lwjgl.sdl.SDL_DisplayMode
 *  org.lwjgl.sdl.SDL_PixelFormatDetails
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.sdl.SDL_PixelFormatDetails;
import org.slf4j.Logger;

public final class VideoMode {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<VideoMode> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("width").forGetter(VideoMode::getWidth), (App)Codec.INT.fieldOf("height").forGetter(VideoMode::getHeight), (App)Codec.INT.fieldOf("red_bits").forGetter(VideoMode::getRedBits), (App)Codec.INT.fieldOf("green_bits").forGetter(VideoMode::getGreenBits), (App)Codec.INT.fieldOf("blue_bits").forGetter(VideoMode::getBlueBits), (App)Codec.FLOAT.fieldOf("refresh_rate").forGetter(VideoMode::getRefreshRate)).apply((Applicative)instance, VideoMode::new));
    private static final Gson GSON = new Gson();
    private final int width;
    private final int height;
    private final int redBits;
    private final int greenBits;
    private final int blueBits;
    private final float refreshRate;

    public VideoMode(int width, int height, int redBits, int greenBits, int blueBits, int refreshRate) {
        this(width, height, redBits, greenBits, blueBits, (float)refreshRate);
    }

    private VideoMode(int width, int height, int redBits, int greenBits, int blueBits, float refreshRate) {
        this.width = width;
        this.height = height;
        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;
        this.refreshRate = refreshRate;
    }

    public VideoMode(SDL_DisplayMode mode) {
        this.width = mode.w();
        this.height = mode.h();
        this.refreshRate = mode.refresh_rate();
        SDL_PixelFormatDetails details = SDLPixels.SDL_GetPixelFormatDetails((int)mode.format());
        this.redBits = details != null ? (int)details.Rbits() : 8;
        this.greenBits = details != null ? (int)details.Gbits() : 8;
        this.blueBits = details != null ? (int)details.Bbits() : 8;
    }

    public static Optional<VideoMode> read(@Nullable String s) {
        if (s == null) {
            return Optional.empty();
        }
        try {
            JsonElement json = JsonParser.parseString((String)s);
            return CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)json).resultOrPartial(error -> LOGGER.warn("Failed to parse video mode '{}': {}", (Object)s, error));
        }
        catch (RuntimeException error2) {
            LOGGER.warn("Failed to parse video mode '{}'", (Object)s, (Object)error2);
            return Optional.empty();
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getRedBits() {
        return this.redBits;
    }

    public int getGreenBits() {
        return this.greenBits;
    }

    public int getBlueBits() {
        return this.blueBits;
    }

    public float getRefreshRate() {
        return this.refreshRate;
    }

    public String refreshRateLabel() {
        float rate = this.getRefreshRate();
        return (double)rate == Math.rint(rate) ? Integer.toString((int)rate) : String.format(Locale.ROOT, "%.2f", Float.valueOf(rate));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        VideoMode videoMode = (VideoMode)o;
        return this.width == videoMode.width && this.height == videoMode.height && this.redBits == videoMode.redBits && this.greenBits == videoMode.greenBits && this.blueBits == videoMode.blueBits && Float.compare(this.refreshRate, videoMode.refreshRate) == 0;
    }

    public int hashCode() {
        return Objects.hash(this.width, this.height, this.redBits, this.greenBits, this.blueBits, Float.valueOf(this.refreshRate));
    }

    public String toString() {
        return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.width, this.height, this.refreshRateLabel(), this.redBits + this.greenBits + this.blueBits);
    }

    public String write() {
        return CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)this).result().map(arg_0 -> ((Gson)GSON).toJson(arg_0)).orElseThrow();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrays
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.palette;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.thread.ParallelMapTransform;
import org.slf4j.Logger;

public class Palette {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Palette EMPTY = new Palette(IntArrays.EMPTY_ARRAY);
    public static final FileToIdConverter ID_CONVERTER = new FileToIdConverter("textures/palettes", ".png");
    private final int[] colors;

    private Palette(int[] colors) {
        this.colors = colors;
    }

    public static CompletableFuture<Map<Identifier, Palette>> listAndLoad(ResourceManager resourceManager, Executor taskExecutor) {
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> ID_CONVERTER.listMatchingResources(resourceManager), taskExecutor).thenCompose(resources -> ParallelMapTransform.schedule(resources, Palette::tryLoad, taskExecutor))).thenApply(palettes -> palettes.entrySet().stream().filter(entry -> !((Palette)entry.getValue()).isEmpty()).collect(Collectors.toMap(entry -> ID_CONVERTER.fileToId((Identifier)entry.getKey()), Map.Entry::getValue)));
    }

    private static Palette tryLoad(Identifier id, Resource resource) {
        try {
            return Palette.load(resource);
        }
        catch (IOException e) {
            LOGGER.error("Failed to load palette with id {}", (Object)id, (Object)e);
            return EMPTY;
        }
    }

    public static Palette load(Resource resource) throws IOException {
        try (InputStream input = resource.open();){
            NativeImage image = NativeImage.read(input);
            try {
                Palette palette = Palette.from(image);
                if (image != null) {
                    image.close();
                }
                return palette;
            }
            catch (Throwable throwable) {
                if (image != null) {
                    try {
                        image.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    public static Palette from(NativeImage image) {
        return new Palette(image.getPixels());
    }

    @VisibleForTesting
    public static Palette of(int ... colors) {
        return new Palette(IntArrays.copy((int[])colors));
    }

    public int get(int index) {
        return this.colors[index];
    }

    public int size() {
        return this.colors.length;
    }

    public boolean isEmpty() {
        return this.colors.length == 0;
    }
}


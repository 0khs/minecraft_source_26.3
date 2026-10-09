/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.palette;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.DynamicAtlasTree;
import net.minecraft.client.renderer.texture.DynamicAtlasTreeSlot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.metadata.texture.PaletteMetadataSection;
import net.minecraft.client.resources.palette.Palette;
import net.minecraft.client.resources.palette.PaletteMapping;
import net.minecraft.client.resources.palette.PaletteMappingCache;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PalettedTextureManager
implements PreparableReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TextureManager textureManager;
    private BiFunction<Identifier, Identifier, PaletteMapping> paletteMappings = (identifier, identifier2) -> PaletteMapping.NONE;
    private final LoadingCache<Identifier, Optional<BaseTexture>> baseTextureCache;
    private final List<AtlasTexture> atlasTextures = new ArrayList<AtlasTexture>();
    private final List<OverflowTexture> overflowTextures = new ArrayList<OverflowTexture>();
    private final Map<SlotKey, Handle> handles = new HashMap<SlotKey, Handle>();
    private final Handle missingHandle;

    public PalettedTextureManager(final ResourceManager resourceManager, TextureManager textureManager) {
        this.textureManager = textureManager;
        this.baseTextureCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5L)).maximumSize(64L).removalListener(notification -> ((Optional)notification.getValue()).ifPresent(BaseTexture::close)).build((CacheLoader)new CacheLoader<Identifier, Optional<BaseTexture>>(this){
            {
                Objects.requireNonNull(this$0);
            }

            public Optional<BaseTexture> load(Identifier id) {
                return Optional.ofNullable(PalettedTextureManager.loadBaseTexture(id, resourceManager));
            }
        });
        this.missingHandle = new Handle(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public Identifier textureLocation() {
                return MissingTextureAtlasSprite.getLocation();
            }

            @Override
            public float getU(float offset) {
                return offset;
            }

            @Override
            public float getV(float offset) {
                return offset;
            }
        };
    }

    private static @Nullable BaseTexture loadBaseTexture(Identifier id, ResourceManager resourceManager) {
        Identifier location = id.withPath(path -> "textures/" + path + ".png");
        try {
            NativeImage image;
            Resource resource = resourceManager.getResourceOrThrow(location);
            try (InputStream input = resource.open();){
                image = NativeImage.read(input);
            }
            return new BaseTexture(image, resource.metadata().getSection(PaletteMetadataSection.TYPE).orElse(null));
        }
        catch (IOException e) {
            LOGGER.error("Failed to load paletted base texture at {}", (Object)location, (Object)e);
            return null;
        }
    }

    public Handle getOrPrepare(Identifier baseTexture, Identifier paletteId) {
        SlotKey key = new SlotKey(baseTexture, paletteId);
        Handle existingHandle = this.handles.get(key);
        if (existingHandle != null) {
            return existingHandle;
        }
        Handle handle = this.prepareSlot(baseTexture, paletteId);
        this.handles.put(key, handle);
        return handle;
    }

    private Handle prepareSlot(Identifier baseTextureId, Identifier paletteId) {
        Optional maybeBaseTexture = (Optional)this.baseTextureCache.getUnchecked((Object)baseTextureId);
        if (maybeBaseTexture.isEmpty()) {
            return this.missingHandle;
        }
        BaseTexture baseTexture = (BaseTexture)maybeBaseTexture.get();
        PaletteMetadataSection paletteMetadata = baseTexture.paletteMetadata();
        PaletteMapping paletteMapping = paletteMetadata != null ? this.paletteMappings.apply(paletteMetadata.basePalette(), paletteId) : PaletteMapping.NONE;
        try (NativeImage newTexture = baseTexture.image().mappedCopy((IntUnaryOperator)((Object)paletteMapping));){
            Slot slot = this.allocateSlot(newTexture.getWidth(), newTexture.getHeight());
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(slot.texture.getTexture(), newTexture, 0, 0, slot.x, slot.y);
            Slot slot2 = slot;
            return slot2;
        }
    }

    private Slot allocateSlot(int width, int height) {
        if (width > 512 || height > 512) {
            OverflowTexture texture = new OverflowTexture(Identifier.withDefaultNamespace("paletted/overflow_" + this.overflowTextures.size()), width, height);
            this.textureManager.register(texture.location, texture);
            this.overflowTextures.add(texture);
            return new Slot(texture, texture.location, 0, 0, width, height);
        }
        for (AtlasTexture atlas : this.atlasTextures) {
            DynamicAtlasTreeSlot atlasSlot = atlas.tryAllocateSlot(width, height);
            if (atlasSlot == null) continue;
            return new Slot(atlas, atlas.location, atlasSlot.x(), atlasSlot.y(), atlasSlot.width(), atlasSlot.height());
        }
        AtlasTexture atlas = new AtlasTexture(Identifier.withDefaultNamespace("paletted/atlas_" + this.atlasTextures.size()));
        this.textureManager.register(atlas.location, atlas);
        this.atlasTextures.add(atlas);
        DynamicAtlasTreeSlot atlasSlot = atlas.tryAllocateSlot(width, height);
        if (atlasSlot == null) {
            throw new IllegalStateException("Could not allocate slot in fresh atlas for sprite with size " + width + "x" + height);
        }
        return new Slot(atlas, atlas.location, atlasSlot.x(), atlasSlot.y(), atlasSlot.width(), atlasSlot.height());
    }

    @Override
    public void close() {
        for (AtlasTexture atlas : this.atlasTextures) {
            this.textureManager.release(atlas.location);
        }
        for (OverflowTexture texture : this.overflowTextures) {
            this.textureManager.release(texture.location);
        }
        this.atlasTextures.clear();
        this.overflowTextures.clear();
        this.handles.clear();
        this.paletteMappings = (identifier, identifier2) -> PaletteMapping.NONE;
        this.baseTextureCache.invalidateAll();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState currentReload, Executor taskExecutor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        return ((CompletableFuture)Palette.listAndLoad(currentReload.resourceManager(), taskExecutor).thenCompose(preparationBarrier::wait)).thenAcceptAsync(palettes -> {
            this.close();
            PaletteMappingCache paletteMappings = new PaletteMappingCache((Map<Identifier, Palette>)palettes);
            this.paletteMappings = paletteMappings::get;
        }, reloadExecutor);
    }

    public static interface Handle
    extends UvMapping {
        public Identifier textureLocation();
    }

    private record BaseTexture(NativeImage image, @Nullable PaletteMetadataSection paletteMetadata) implements AutoCloseable
    {
        @Override
        public void close() {
            this.image.close();
        }
    }

    private record SlotKey(Identifier baseTexture, Identifier paletteId) {
    }

    private record Slot(AbstractTexture texture, Identifier textureLocation, int x, int y, int width, int height) implements Handle
    {
        @Override
        public float getU(float offset) {
            return ((float)this.x + offset * (float)this.width) / (float)this.texture.getTexture().getWidth(0);
        }

        @Override
        public float getV(float offset) {
            return ((float)this.y + offset * (float)this.height) / (float)this.texture.getTexture().getHeight(0);
        }
    }

    private static class AtlasTexture
    extends AbstractTexture
    implements Dumpable {
        private static final int SIZE = 512;
        private final Identifier location;
        private final DynamicAtlasTree tree = new DynamicAtlasTree(0, 0, 512, 512);

        private AtlasTexture(Identifier location) {
            GpuDevice device = RenderSystem.getDevice();
            this.texture = device.createTexture(() -> "Paletted Atlas", 7, GpuFormat.RGBA8_UNORM, 512, 512, 1, 1);
            this.textureView = device.createTextureView(this.texture);
            this.location = location;
            this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        }

        public @Nullable DynamicAtlasTreeSlot tryAllocateSlot(int width, int height) {
            return this.tree.insert(width, height, 0);
        }

        @Override
        public void dumpContents(Identifier selfId, Path dir) {
            if (this.texture != null) {
                String outputId = selfId.toDebugFileName();
                TextureUtil.writeAsPNG(dir, outputId, this.texture, 0, argb -> ARGB.alpha(argb) == 0 ? -16777216 : argb);
            }
        }
    }

    private static class OverflowTexture
    extends DynamicTexture {
        private final Identifier location;

        public OverflowTexture(Identifier location, int width, int height) {
            super(location::toString, width, height, false);
            this.location = location;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.rendertype;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class RenderSetup {
    final RenderPipeline pipeline;
    final @Nullable OitPipelineSet oitPipelineSet;
    final Map<String, TextureBinding> textures;
    final TextureTransform textureTransform;
    final OutlineProperty outlineProperty;
    final @Nullable String outlineTextureName;
    final boolean useLightmap;
    final boolean useOverlay;
    final boolean affectsCrumbling;
    final boolean sortOnUpload;
    final LayeringTransform layeringTransform;
    final boolean forceSolidModelPhase;

    private RenderSetup(RenderPipeline pipeline, @Nullable OitPipelineSet oitPipelineSet, Map<String, TextureBinding> textures, boolean useLightmap, boolean useOverlay, LayeringTransform layeringTransform, TextureTransform textureTransform, OutlineProperty outlineProperty, @Nullable String outlineTextureName, boolean affectsCrumbling, boolean sortOnUpload, boolean forceSolidModelPhase) {
        this.pipeline = pipeline;
        this.oitPipelineSet = oitPipelineSet;
        this.textures = textures;
        this.textureTransform = textureTransform;
        this.useLightmap = useLightmap;
        this.useOverlay = useOverlay;
        this.outlineProperty = outlineProperty;
        this.outlineTextureName = outlineTextureName;
        this.layeringTransform = layeringTransform;
        this.affectsCrumbling = affectsCrumbling;
        this.sortOnUpload = sortOnUpload;
        this.forceSolidModelPhase = forceSolidModelPhase;
    }

    public String toString() {
        return "RenderSetup[layeringTransform=" + String.valueOf(this.layeringTransform) + ", textureTransform=" + String.valueOf(this.textureTransform) + ", textures=" + String.valueOf(this.textures) + ", outlineProperty=" + String.valueOf((Object)this.outlineProperty) + ", useLightmap=" + this.useLightmap + ", useOverlay=" + this.useOverlay + "]";
    }

    public static RenderSetupBuilder builder(RenderPipeline pipeline) {
        return new RenderSetupBuilder(pipeline);
    }

    public List<PreparedRenderType.Texture> prepareTextures(TextureManager textureManager, SamplerCache samplerCache, GpuTextureView overlayTexture, GpuTextureView lightmapTexture) {
        if (this.textures.isEmpty() && !this.useOverlay && !this.useLightmap) {
            return List.of();
        }
        ImmutableList.Builder textures = ImmutableList.builderWithExpectedSize((int)(this.textures.size() + 2));
        if (this.useOverlay) {
            textures.add((Object)new PreparedRenderType.Texture("Sampler1", overlayTexture, samplerCache.getClampToEdge(FilterMode.LINEAR)));
        }
        if (this.useLightmap) {
            textures.add((Object)new PreparedRenderType.Texture("Sampler2", lightmapTexture, samplerCache.getClampToEdge(FilterMode.LINEAR)));
        }
        for (Map.Entry<String, TextureBinding> entry : this.textures.entrySet()) {
            AbstractTexture texture = textureManager.getTexture(entry.getValue().location);
            GpuSampler samplerOverride = entry.getValue().sampler().get();
            textures.add((Object)new PreparedRenderType.Texture(entry.getKey(), texture.getTextureView(), samplerOverride != null ? samplerOverride : texture.getSampler()));
        }
        return textures.build();
    }

    public static enum OutlineProperty {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineProperty(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static class RenderSetupBuilder {
        private final RenderPipeline pipeline;
        private @Nullable OitPipelineSet oitPipelineSet;
        private boolean useLightmap = false;
        private boolean useOverlay = false;
        private LayeringTransform layeringTransform = LayeringTransform.NO_LAYERING;
        private TextureTransform textureTransform = TextureTransform.DEFAULT_TEXTURING;
        private boolean affectsCrumbling = false;
        private boolean sortOnUpload = false;
        private OutlineProperty outlineProperty = OutlineProperty.NONE;
        private @Nullable String outlineTextureName;
        private final Map<String, TextureBinding> textures = new HashMap<String, TextureBinding>();
        private boolean forceSolidModelPhase;

        private RenderSetupBuilder(RenderPipeline pipeline) {
            this.pipeline = pipeline;
        }

        public RenderSetupBuilder withTexture(String name, Identifier texture) {
            this.textures.put(name, new TextureBinding(texture, () -> null));
            return this;
        }

        public RenderSetupBuilder withTexture(String name, Identifier texture, @Nullable Supplier<GpuSampler> sampler) {
            this.textures.put(name, new TextureBinding(texture, (Supplier<GpuSampler>)Suppliers.memoize(() -> sampler == null ? null : (GpuSampler)sampler.get())));
            return this;
        }

        public RenderSetupBuilder useLightmap() {
            this.useLightmap = true;
            return this;
        }

        public RenderSetupBuilder useOverlay() {
            this.useOverlay = true;
            return this;
        }

        public RenderSetupBuilder affectsCrumbling() {
            this.affectsCrumbling = true;
            return this;
        }

        public RenderSetupBuilder sortOnUpload() {
            this.sortOnUpload = true;
            return this;
        }

        public RenderSetupBuilder setLayeringTransform(LayeringTransform layeringTransform) {
            this.layeringTransform = layeringTransform;
            return this;
        }

        public RenderSetupBuilder setTextureTransform(TextureTransform textureTransform) {
            this.textureTransform = textureTransform;
            return this;
        }

        public RenderSetupBuilder setOutline(OutlineProperty outlineProperty) {
            this.outlineProperty = outlineProperty;
            this.outlineTextureName = null;
            return this;
        }

        public RenderSetupBuilder setOutline(OutlineProperty outlineProperty, String outlineTextureName) {
            this.outlineProperty = outlineProperty;
            this.outlineTextureName = outlineTextureName;
            return this;
        }

        public RenderSetupBuilder setOitPipelines(OitPipelineSet oitPipelineSet) {
            this.oitPipelineSet = oitPipelineSet;
            return this;
        }

        public RenderSetupBuilder withForcedSolidModelPhase() {
            this.forceSolidModelPhase = true;
            return this;
        }

        public RenderSetup createRenderSetup() {
            return new RenderSetup(this.pipeline, this.oitPipelineSet, this.textures, this.useLightmap, this.useOverlay, this.layeringTransform, this.textureTransform, this.outlineProperty, this.outlineTextureName, this.affectsCrumbling, this.sortOnUpload, this.forceSolidModelPhase);
        }
    }

    record TextureBinding(Identifier location, Supplier<@Nullable GpuSampler> sampler) {
    }
}


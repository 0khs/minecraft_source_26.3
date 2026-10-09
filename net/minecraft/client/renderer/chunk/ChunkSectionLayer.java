/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.Locale;
import net.minecraft.client.renderer.RenderPipelines;

public enum ChunkSectionLayer {
    SOLID(RenderPipelines.SOLID_TERRAIN, RenderPipelines.SOLID_TERRAIN_MULTIDRAW, 0x400000, false),
    CUTOUT(RenderPipelines.CUTOUT_TERRAIN, RenderPipelines.CUTOUT_TERRAIN_MULTIDRAW, 0x400000, false),
    TRANSLUCENT(RenderPipelines.TRANSLUCENT_TERRAIN, RenderPipelines.TRANSLUCENT_TERRAIN_MULTIDRAW, 786432, true);

    private final RenderPipeline pipeline;
    private final RenderPipeline multiDrawPipeline;
    private final int bufferSize;
    private final boolean translucent;
    private final String label;

    private ChunkSectionLayer(RenderPipeline pipeline, RenderPipeline multiDrawPipeline, int bufferSize, boolean translucent) {
        this.pipeline = pipeline;
        this.multiDrawPipeline = multiDrawPipeline;
        this.bufferSize = bufferSize;
        this.translucent = translucent;
        this.label = this.toString().toLowerCase(Locale.ROOT);
    }

    public static ChunkSectionLayer byTransparency(Transparency transparency) {
        if (transparency.hasTranslucent()) {
            return TRANSLUCENT;
        }
        if (transparency.hasTransparent()) {
            return CUTOUT;
        }
        return SOLID;
    }

    public RenderPipeline pipeline(boolean multiDraw) {
        return multiDraw ? this.multiDrawPipeline : this.pipeline;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public String label() {
        return this.label;
    }

    public boolean translucent() {
        return this.translucent;
    }

    public VertexFormat vertexFormat() {
        return this.pipeline.getVertexFormatBinding(0);
    }
}


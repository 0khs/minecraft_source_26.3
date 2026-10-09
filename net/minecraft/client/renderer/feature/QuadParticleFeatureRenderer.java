/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jspecify.annotations.Nullable;

public class QuadParticleFeatureRenderer
implements FeatureRenderer<Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Particle");
    private final List<PreparedGroup> groups = new ArrayList<PreparedGroup>();
    private @Nullable GpuBufferSlice dynamicTransforms;

    @Override
    public void prepareGroup(FeatureFrameContext context, List<Submit> submits, boolean strictlyOrdered) {
        if (submits.isEmpty()) {
            return;
        }
        StagedVertexBuffer stagedVertexBuffer = context.stagedVertexBuffer();
        IdentityHashMap<SingleQuadParticle.Layer, StagedVertexBuffer.Draw> drawByLayer = new IdentityHashMap<SingleQuadParticle.Layer, StagedVertexBuffer.Draw>();
        IdentityHashMap<SingleQuadParticle.Layer, AbstractTexture> textures = new IdentityHashMap<SingleQuadParticle.Layer, AbstractTexture>();
        for (Submit submit : submits) {
            QuadParticleRenderState particles = submit.particles();
            if (particles.isEmpty()) continue;
            for (SingleQuadParticle.Layer layer2 : particles.layers()) {
                if (layer2.translucent() != submit.translucent()) continue;
                StagedVertexBuffer.Draw draw = drawByLayer.computeIfAbsent(layer2, layer -> stagedVertexBuffer.appendDraw(DefaultVertexFormat.PARTICLE, PrimitiveTopology.QUADS, null));
                particles.buildLayer(layer2, stagedVertexBuffer.getVertexBuilder(draw));
                textures.put(layer2, context.textureManager().getTexture(layer2.textureAtlasLocation()));
                stagedVertexBuffer.requestIndexCount(draw);
            }
        }
        boolean translucent = submits.getFirst().translucent();
        this.groups.add(new PreparedGroup(drawByLayer, textures, translucent));
    }

    @Override
    public void finishPrepare(FeatureFrameContext context) {
        this.dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());
    }

    @Override
    public void executeGroup(FeatureFrameContext context, @Nullable OitStage stage, RenderPass renderPass, int groupIndex, List<Submit> submits, boolean strictlyOrdered) {
        PreparedGroup group = this.groups.get(groupIndex);
        renderPass.pushDebugGroup(() -> "Particles - " + (group.translucent ? "Translucent" : "Solid"));
        RenderSystem.bindDefaultUniforms(renderPass);
        renderPass.setUniform("DynamicTransforms", Objects.requireNonNull(this.dynamicTransforms));
        renderPass.setUniform("Sampler2", context.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        QuadParticleFeatureRenderer.drawLayers(context.stagedVertexBuffer(), group, renderPass, stage);
        renderPass.popDebugGroup();
    }

    private static void drawLayers(StagedVertexBuffer stagedBuffer, PreparedGroup group, RenderPass renderPass, @Nullable OitStage stage) {
        for (Map.Entry<SingleQuadParticle.Layer, StagedVertexBuffer.Draw> entry : group.layers.entrySet()) {
            StagedVertexBuffer.ExecuteInfo executeInfo = stagedBuffer.getExecuteInfo(entry.getValue());
            if (executeInfo == null) continue;
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(stage != null ? QuadParticleFeatureRenderer.getOitPipeline(stage, entry.getKey()) : entry.getKey().pipeline()));
            renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice());
            renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType());
            AbstractTexture texture = group.textures.get(entry.getKey());
            renderPass.setUniform("Sampler0", texture.getTextureView(), texture.getSampler());
            renderPass.drawIndexed(executeInfo.indexCount(), 1, executeInfo.firstIndex(), executeInfo.baseVertex(), 0);
        }
    }

    private static RenderPipeline getOitPipeline(OitStage stage, SingleQuadParticle.Layer layer) {
        if (layer.oitPipelineSet() == null) {
            throw new IllegalStateException("OIT pipeline set for particle layer not specified.");
        }
        return layer.oitPipelineSet().getPipeline(stage);
    }

    @Override
    public void finishExecute(FeatureFrameContext context) {
        this.groups.clear();
        this.dynamicTransforms = null;
    }

    public record Submit(QuadParticleRenderState particles, boolean translucent) implements SubmitNode
    {
        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }

    private record PreparedGroup(Map<SingleQuadParticle.Layer, StagedVertexBuffer.Draw> layers, Map<SingleQuadParticle.Layer, AbstractTexture> textures, boolean translucent) {
    }
}


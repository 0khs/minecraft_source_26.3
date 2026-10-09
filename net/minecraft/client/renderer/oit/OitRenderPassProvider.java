/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.oit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.oit.OitStage;

public class OitRenderPassProvider {
    public static RenderPass createRenderPass(OitStage stage, Supplier<String> label, Parameters params) {
        return switch (stage) {
            case OitStage.DEPTH_BOUNDS -> OitRenderPassProvider.createDepthBoundsPass(label, params);
            case OitStage.TRANSMITTANCE -> OitRenderPassProvider.createTransmittancePass(label, params);
            case OitStage.ACCUMULATE -> OitRenderPassProvider.createAccumulatePass(label, params);
            default -> throw new IllegalArgumentException("Invalid OIT stage.");
        };
    }

    private static RenderPass createDepthBoundsPass(Supplier<String> label, Parameters params) {
        RenderPassDescriptor descriptor = RenderPassDescriptor.builder(() -> "OIT Depth Bounds for " + (String)label.get()).withColorAttachment(params.depthBoundsTargetView).withDepthAttachment(params.depthTextureView).build();
        RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);
        RenderSystem.bindDefaultUniforms(renderPass);
        return renderPass;
    }

    private static RenderPass createTransmittancePass(Supplier<String> label, Parameters params) {
        RenderPassDescriptor.Builder descriptor = RenderPassDescriptor.builder(() -> "OIT Transmittance for " + (String)label.get()).withDepthAttachment(params.depthTextureView, OptionalDouble.empty());
        for (int i = 0; i < LevelRenderer.OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
            descriptor.withColorAttachment(params.transmittanceTargetViews[i], Optional.empty());
        }
        GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor.build());
        RenderSystem.bindDefaultUniforms(renderPass);
        renderPass.setUniform("DepthBoundsSampler", params.depthBoundsTargetView, nearestSampler);
        return renderPass;
    }

    private static RenderPass createAccumulatePass(Supplier<String> label, Parameters params) {
        RenderPassDescriptor descriptor = RenderPassDescriptor.builder(() -> "OIT Accumulate for " + (String)label.get()).withColorAttachment(params.accumulateTargetView).withDepthAttachment(params.depthTextureView).build();
        RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);
        GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        RenderSystem.bindDefaultUniforms(renderPass);
        for (int i = 0; i < LevelRenderer.OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
            renderPass.setUniform("Coeff" + i, params.transmittanceTargetViews[i], nearestSampler);
        }
        renderPass.setUniform("DepthBoundsSampler", params.depthBoundsTargetView, nearestSampler);
        return renderPass;
    }

    public static final class Parameters {
        private GpuTextureView depthBoundsTargetView;
        private final GpuTextureView[] transmittanceTargetViews;
        private final GpuTextureView accumulateTargetView;
        private final GpuTextureView depthTextureView;

        public Parameters(GpuTextureView depthBoundsTargetView, GpuTextureView[] transmittanceTargetViews, GpuTextureView accumulateTargetView, GpuTextureView depthTextureView) {
            this.depthBoundsTargetView = depthBoundsTargetView;
            this.transmittanceTargetViews = transmittanceTargetViews;
            this.accumulateTargetView = accumulateTargetView;
            this.depthTextureView = depthTextureView;
        }

        public void setDepthBoundsTargetView(GpuTextureView depthBoundsTargetView) {
            this.depthBoundsTargetView = depthBoundsTargetView;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.oit;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitStage;

public record OitPipelineSet(RenderPipeline depthBoundsPipeline, RenderPipeline transmittancePipeline, RenderPipeline accumulatePipeline) {
    public RenderPipeline getPipeline(OitStage stage) {
        return switch (stage) {
            case OitStage.DEPTH_BOUNDS -> this.depthBoundsPipeline;
            case OitStage.TRANSMITTANCE -> this.transmittancePipeline;
            case OitStage.ACCUMULATE -> this.accumulatePipeline;
            default -> throw new IllegalArgumentException("Unsupported OIT stage.");
        };
    }

    public static Builder builder(String locationSuffix, RenderPipeline.Builder builder) {
        return new Builder(builder.buildSnippet(), locationSuffix);
    }

    public static class Builder {
        private static final Consumer<RenderPipeline.Builder> DISABLE_DEPTH_TEST = builder -> builder.withDepthStencilState(Optional.empty());
        private final RenderPipeline.Snippet baseSnippet;
        private final String locationSuffix;
        private Optional<Consumer<RenderPipeline.Builder>> depthBoundsModifier = Optional.empty();
        private Optional<Consumer<RenderPipeline.Builder>> transmittanceModifier = Optional.empty();
        private Optional<Consumer<RenderPipeline.Builder>> accumulateModifier = Optional.empty();

        public Builder(RenderPipeline.Snippet baseSnippet, String locationSuffix) {
            this.baseSnippet = baseSnippet;
            this.locationSuffix = locationSuffix;
        }

        public Builder withDepthBoundsModifier(Consumer<RenderPipeline.Builder> modifier) {
            this.depthBoundsModifier = Builder.composeModifiers(this.depthBoundsModifier, modifier);
            return this;
        }

        public Builder withTransmittanceModifier(Consumer<RenderPipeline.Builder> modifier) {
            this.transmittanceModifier = Builder.composeModifiers(this.transmittanceModifier, modifier);
            return this;
        }

        public Builder withAccumulateModifier(Consumer<RenderPipeline.Builder> modifier) {
            this.accumulateModifier = Builder.composeModifiers(this.accumulateModifier, modifier);
            return this;
        }

        public Builder withoutDepthTest() {
            return this.withDepthBoundsModifier(DISABLE_DEPTH_TEST).withTransmittanceModifier(DISABLE_DEPTH_TEST).withAccumulateModifier(DISABLE_DEPTH_TEST);
        }

        private static Optional<Consumer<RenderPipeline.Builder>> composeModifiers(Optional<Consumer<RenderPipeline.Builder>> currentModifier, Consumer<RenderPipeline.Builder> newModifier) {
            if (currentModifier.isPresent()) {
                return Optional.of(builder -> {
                    ((Consumer)currentModifier.get()).accept(builder);
                    newModifier.accept((RenderPipeline.Builder)builder);
                });
            }
            return Optional.of(newModifier);
        }

        public OitPipelineSet build() {
            RenderPipeline.Builder depthBoundsBuilder = RenderPipeline.builder(this.baseSnippet, RenderPipelines.OIT_DEPTH_BOUNDS_SNIPPET).withLocation("pipeline/oit_depth_bounds_" + this.locationSuffix);
            this.depthBoundsModifier.ifPresent(modifier -> modifier.accept(depthBoundsBuilder));
            RenderPipeline.Builder transmittanceBuilder = RenderPipeline.builder(this.baseSnippet, RenderPipelines.OIT_TRANSMITTANCE_SNIPPET).withLocation("pipeline/oit_transmittance_" + this.locationSuffix);
            this.transmittanceModifier.ifPresent(modifier -> modifier.accept(transmittanceBuilder));
            RenderPipeline.Builder accumulateBuilder = RenderPipeline.builder(this.baseSnippet, RenderPipelines.OIT_ACCUMULATE_SNIPPET).withLocation("pipeline/oit_accumulate_" + this.locationSuffix);
            this.accumulateModifier.ifPresent(modifier -> modifier.accept(accumulateBuilder));
            return new OitPipelineSet(depthBoundsBuilder.build(), transmittanceBuilder.build(), accumulateBuilder.build());
        }
    }
}


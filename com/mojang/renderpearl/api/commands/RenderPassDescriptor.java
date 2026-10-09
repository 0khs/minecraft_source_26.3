/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  it.unimi.dsi.fastutil.objects.ReferenceLists
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.api.commands;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public record RenderPassDescriptor(Supplier<String> label, List<@Nullable Attachment<Optional<Vector4fc>>> colorAttachments, @Nullable Attachment<OptionalDouble> depthAttachment, RenderPass.RenderArea renderArea) {
    public RenderPassDescriptor(Supplier<String> label, List<Attachment<Optional<Vector4fc>>> colorAttachments, @Nullable Attachment<OptionalDouble> depthAttachment, RenderPass.RenderArea renderArea) {
        colorAttachments = ReferenceLists.unmodifiable((ReferenceList)new ReferenceArrayList(colorAttachments));
    }

    public static Builder builder(Supplier<String> label) {
        return new Builder(label);
    }

    public record Attachment<T>(GpuTextureView textureView, T clearValue) {
    }

    public static class Builder {
        private final Supplier<String> label;
        private final List<@Nullable Attachment<Optional<Vector4fc>>> colorAttachments = new ArrayList<Attachment<Optional<Vector4fc>>>();
        private @Nullable Attachment<OptionalDouble> depthAttachment;
        private @Nullable RenderPass.RenderArea renderArea;

        private Builder(Supplier<String> label) {
            this.label = label;
        }

        public Builder withColorAttachment(GpuTextureView textureView) {
            this.colorAttachments.add(new Attachment(textureView, Optional.empty()));
            return this;
        }

        public Builder withColorAttachment(GpuTextureView textureView, Optional<Vector4fc> clearValue) {
            this.colorAttachments.add(new Attachment<Optional<Vector4fc>>(textureView, clearValue));
            return this;
        }

        public Builder withUnusedColorAttachment() {
            this.colorAttachments.add(null);
            return this;
        }

        public Builder withDepthAttachment(GpuTextureView textureView) {
            this.depthAttachment = new Attachment<OptionalDouble>(textureView, OptionalDouble.empty());
            return this;
        }

        public Builder withDepthAttachment(GpuTextureView textureView, OptionalDouble clearValue) {
            this.depthAttachment = new Attachment<OptionalDouble>(textureView, clearValue);
            return this;
        }

        public Builder withRenderArea(RenderPass.RenderArea renderArea) {
            this.renderArea = renderArea;
            return this;
        }

        public RenderPassDescriptor build() {
            RenderPass.RenderArea renderArea = this.renderArea != null ? this.renderArea : Builder.defaultRenderArea(this.colorAttachments, this.depthAttachment);
            return new RenderPassDescriptor(this.label, this.colorAttachments, this.depthAttachment, renderArea);
        }

        private static RenderPass.RenderArea defaultRenderArea(List<@Nullable Attachment<Optional<Vector4fc>>> colorAttachments, @Nullable Attachment<OptionalDouble> depthAttachment) {
            int width = 0;
            int height = 0;
            if (!colorAttachments.isEmpty()) {
                for (Attachment<Optional<Vector4fc>> colorAttachment : colorAttachments) {
                    if (colorAttachment == null) continue;
                    GpuTextureView textureView = colorAttachment.textureView();
                    width = textureView.getWidth(0);
                    height = textureView.getHeight(0);
                }
            } else if (depthAttachment != null) {
                width = depthAttachment.textureView().getWidth(0);
                height = depthAttachment.textureView().getHeight(0);
            }
            return new RenderPass.RenderArea(0, 0, width, height);
        }
    }
}


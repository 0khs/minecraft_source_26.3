/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.api.pipeline;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.UniformType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record BindGroupLayout(List<UniformDescription> uniforms) {
    public BindGroupLayout {
        uniforms = List.copyOf(uniforms);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static List<UniformDescription> flattenUniforms(List<BindGroupLayout> bindGroupLayouts) {
        ArrayList<UniformDescription> flattened = new ArrayList<UniformDescription>();
        for (BindGroupLayout bindGroupLayout : bindGroupLayouts) {
            flattened.addAll(bindGroupLayout.uniforms());
        }
        return flattened;
    }

    public static void ensureCompatible(List<BindGroupLayout> bindGroupLayouts) {
        HashSet<String> names = new HashSet<String>();
        for (int layoutIndex = 0; layoutIndex < bindGroupLayouts.size(); ++layoutIndex) {
            BindGroupLayout bindGroupLayout = bindGroupLayouts.get(layoutIndex);
            for (UniformDescription uniform : bindGroupLayout.uniforms()) {
                if (names.add(uniform.name())) continue;
                throw new IllegalArgumentException("Duplicate bind name '" + uniform.name() + "' in bind group layout " + layoutIndex);
            }
        }
    }

    public static class Builder {
        private final List<UniformDescription> uniforms = new ArrayList<UniformDescription>();

        private Builder() {
        }

        public Builder withUniform(String name, UniformType type) {
            if (type == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Cannot use texel buffer without specifying texture format");
            }
            this.uniforms.add(new UniformDescription(name, type));
            return this;
        }

        public Builder withUniform(String name, UniformType type, GpuFormat format) {
            if (type != UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Only texel buffer can specify texture format");
            }
            this.uniforms.add(new UniformDescription(name, format));
            return this;
        }

        public BindGroupLayout build() {
            return new BindGroupLayout(this.uniforms);
        }
    }

    public record UniformDescription(String name, UniformType type, @Nullable GpuFormat gpuFormat) {
        public UniformDescription(String name, UniformType type) {
            this(name, type, null);
            if (type == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Texel buffer needs a texture format");
            }
        }

        public UniformDescription(String name, GpuFormat gpuFormat) {
            this(name, UniformType.TEXEL_BUFFER, gpuFormat);
        }
    }
}


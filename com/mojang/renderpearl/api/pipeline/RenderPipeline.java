/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  it.unimi.dsi.fastutil.objects.ReferenceLists
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.api.pipeline;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class RenderPipeline {
    private final Identifier location;
    private final Map<ShaderType, Identifier> shaders;
    private final ShaderDefines shaderDefines;
    private final List<BindGroupLayout> bindGroupLayouts;
    private final @Nullable DepthStencilState depthStencilState;
    private final PolygonMode polygonMode;
    private final boolean cull;
    private final List<@Nullable ColorTargetState> colorTargetStates;
    private final List<@Nullable VertexFormat> vertexFormatPerBuffer;
    private final PrimitiveTopology primitiveTopology;
    private final int pushConstantSize;
    private final int sortKey;
    private static int sortKeySeed;

    protected RenderPipeline(Identifier location, Map<ShaderType, Identifier> shaders, ShaderDefines shaderDefines, Collection<BindGroupLayout> bindGroupLayouts, @Nullable ColorTargetState[] colorTargetStates, @Nullable DepthStencilState depthStencilState, PolygonMode polygonMode, boolean cull, @Nullable VertexFormat[] vertexFormatPerBuffer, PrimitiveTopology primitiveTopology, int pushConstantSize, int sortKey) {
        this.location = location;
        this.shaders = Collections.unmodifiableMap(new EnumMap<ShaderType, Identifier>(shaders));
        this.shaderDefines = shaderDefines;
        this.bindGroupLayouts = List.copyOf(bindGroupLayouts);
        this.depthStencilState = depthStencilState;
        this.polygonMode = polygonMode;
        this.cull = cull;
        this.colorTargetStates = ReferenceLists.unmodifiable((ReferenceList)new ReferenceArrayList((Object[])colorTargetStates));
        this.vertexFormatPerBuffer = ReferenceLists.unmodifiable((ReferenceList)new ReferenceArrayList((Object[])vertexFormatPerBuffer));
        this.primitiveTopology = primitiveTopology;
        this.pushConstantSize = pushConstantSize;
        this.sortKey = sortKey;
    }

    public int getSortKey() {
        return SharedConstants.DEBUG_SHUFFLE_UI_RENDERING_ORDER ? super.hashCode() * (sortKeySeed + 1) : this.sortKey;
    }

    public static void updateSortKeySeed() {
        sortKeySeed = Math.round(100000.0f * (float)Math.random());
    }

    public String toString() {
        return this.location.toString();
    }

    public PolygonMode getPolygonMode() {
        return this.polygonMode;
    }

    public boolean isCull() {
        return this.cull;
    }

    public List<@Nullable ColorTargetState> getColorTargetStates() {
        return this.colorTargetStates;
    }

    public @Nullable DepthStencilState getDepthStencilState() {
        return this.depthStencilState;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public List<@Nullable VertexFormat> getVertexFormatBindings() {
        return this.vertexFormatPerBuffer;
    }

    public @Nullable VertexFormat getVertexFormatBinding(int bindingIndex) {
        return this.vertexFormatPerBuffer.get(bindingIndex);
    }

    public PrimitiveTopology getPrimitiveTopology() {
        return this.primitiveTopology;
    }

    public Map<ShaderType, Identifier> getShaders() {
        return this.shaders;
    }

    public ShaderDefines getShaderDefines() {
        return this.shaderDefines;
    }

    public List<BindGroupLayout> getBindGroupLayouts() {
        return this.bindGroupLayouts;
    }

    public boolean wantsDepthTexture() {
        return this.depthStencilState != null;
    }

    public int pushConstantSize() {
        return this.pushConstantSize;
    }

    public static Builder builder(Snippet ... snippets) {
        Builder builder = new Builder();
        for (Snippet snippet : snippets) {
            builder.withSnippet(snippet);
        }
        return builder;
    }

    public static class Builder {
        private static int nextPipelineSortKey;
        private Optional<Identifier> location = Optional.empty();
        private final Map<ShaderType, Identifier> shaders = new EnumMap<ShaderType, Identifier>(ShaderType.class);
        private Optional<ShaderDefines.Builder> definesBuilder = Optional.empty();
        private Optional<Set<BindGroupLayout>> bindGroupLayouts = Optional.empty();
        private Optional<DepthStencilState> depthStencilState = Optional.empty();
        private Optional<PolygonMode> polygonMode = Optional.empty();
        private Optional<Boolean> cull = Optional.empty();
        private final @Nullable ColorTargetState[] colorTargetStates = new ColorTargetState[8];
        private int activeColorTargetStateCount;
        private final @Nullable VertexFormat[] vertexFormatPerBuffer = new VertexFormat[16];
        private Optional<PrimitiveTopology> primitiveTopology = Optional.empty();
        private int pushConstantSize = 0;

        private Builder() {
        }

        public Builder withLocation(String location) {
            this.location = Optional.of(Identifier.withDefaultNamespace(location));
            return this;
        }

        public Builder withLocation(Identifier location) {
            this.location = Optional.of(location);
            return this;
        }

        public Builder withFragmentShader(String fragmentShader) {
            return this.withFragmentShader(Identifier.withDefaultNamespace(fragmentShader));
        }

        public Builder withFragmentShader(Identifier fragmentShader) {
            this.shaders.put(ShaderType.FRAGMENT, fragmentShader);
            return this;
        }

        public Builder withVertexShader(String vertexShader) {
            return this.withVertexShader(Identifier.withDefaultNamespace(vertexShader));
        }

        public Builder withVertexShader(Identifier vertexShader) {
            this.shaders.put(ShaderType.VERTEX, vertexShader);
            return this;
        }

        public Builder withShaderDefine(String key) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(key);
            return this;
        }

        public Builder withShaderDefine(String key, int value) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(key, value);
            return this;
        }

        public Builder withShaderDefine(String key, float value) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(key, value);
            return this;
        }

        public Builder withBindGroupLayout(BindGroupLayout bindGroupLayout) {
            if (this.bindGroupLayouts.isEmpty()) {
                this.bindGroupLayouts = Optional.of(new ObjectOpenHashSet());
            }
            this.bindGroupLayouts.get().add(bindGroupLayout);
            return this;
        }

        public Builder withPolygonMode(PolygonMode polygonMode) {
            this.polygonMode = Optional.of(polygonMode);
            return this;
        }

        public Builder withCull(boolean cull) {
            this.cull = Optional.of(cull);
            return this;
        }

        public Builder withColorTargetState(int index, ColorTargetState colorTargetState) {
            this.colorTargetStates[index] = colorTargetState;
            this.activeColorTargetStateCount = Math.max(this.activeColorTargetStateCount, index + 1);
            return this;
        }

        public Builder withColorTargetStates(int startIindex, int endIndex, Supplier<ColorTargetState> colorTargetState) {
            for (int i = startIindex; i <= endIndex; ++i) {
                this.colorTargetStates[i] = colorTargetState.get();
                this.activeColorTargetStateCount = Math.max(this.activeColorTargetStateCount, i + 1);
            }
            return this;
        }

        public Builder withUnusedColorTargetState(int index) {
            this.colorTargetStates[index] = null;
            this.activeColorTargetStateCount = Math.max(this.activeColorTargetStateCount, index + 1);
            return this;
        }

        public Builder withColorTargetState(ColorTargetState colorTargetState) {
            return this.withColorTargetState(0, colorTargetState);
        }

        public Builder withDepthStencilState(DepthStencilState depthStencilState) {
            this.depthStencilState = Optional.of(depthStencilState);
            return this;
        }

        public Builder withDepthStencilState(Optional<DepthStencilState> depthStencilState) {
            this.depthStencilState = depthStencilState;
            return this;
        }

        public Builder withVertexBinding(int bindingIndex, VertexFormat vertexFormat) {
            this.vertexFormatPerBuffer[bindingIndex] = vertexFormat;
            return this;
        }

        public Builder withPrimitiveTopology(PrimitiveTopology primitiveTopology) {
            this.primitiveTopology = Optional.of(primitiveTopology);
            return this;
        }

        public Builder withPushConstantSize(int pushConstantSize) {
            this.pushConstantSize = pushConstantSize;
            return this;
        }

        public Builder withSnippet(Snippet snippet) {
            int i;
            this.shaders.putAll(snippet.shaders);
            if (snippet.shaderDefines.isPresent()) {
                if (this.definesBuilder.isEmpty()) {
                    this.definesBuilder = Optional.of(ShaderDefines.builder());
                }
                ShaderDefines snippetDefines = snippet.shaderDefines.get();
                for (Map.Entry<String, String> snippetValue : snippetDefines.values().entrySet()) {
                    this.definesBuilder.get().define(snippetValue.getKey(), snippetValue.getValue());
                }
                for (String flag : snippetDefines.flags()) {
                    this.definesBuilder.get().define(flag);
                }
            }
            snippet.bindGroupLayouts.ifPresent(snippetLayouts -> {
                if (this.bindGroupLayouts.isPresent()) {
                    this.bindGroupLayouts.get().addAll((Collection<BindGroupLayout>)snippetLayouts);
                } else {
                    this.bindGroupLayouts = Optional.of(new ObjectOpenHashSet((Collection)snippetLayouts));
                }
            });
            if (snippet.depthStencilState.isPresent()) {
                this.depthStencilState = snippet.depthStencilState;
            }
            if (snippet.cull.isPresent()) {
                this.cull = snippet.cull;
            }
            for (i = 0; i < snippet.activeColorTargetStateCount; ++i) {
                if (this.colorTargetStates[i] != null || snippet.colorTargetStates[i] == null) continue;
                this.colorTargetStates[i] = snippet.colorTargetStates[i];
            }
            this.activeColorTargetStateCount = Math.max(this.activeColorTargetStateCount, snippet.activeColorTargetStateCount);
            for (i = 0; i < snippet.vertexFormatPerBuffer.length; ++i) {
                VertexFormat vertexFormat = snippet.vertexFormatPerBuffer[i];
                if (vertexFormat == null) continue;
                this.vertexFormatPerBuffer[i] = vertexFormat;
            }
            if (snippet.vertexFormatMode.isPresent()) {
                this.primitiveTopology = snippet.vertexFormatMode;
            }
            if (snippet.polygonMode.isPresent()) {
                this.polygonMode = snippet.polygonMode;
            }
            this.pushConstantSize = Math.max(this.pushConstantSize, snippet.pushConstantSize);
            return this;
        }

        public Snippet buildSnippet() {
            return new Snippet(Collections.unmodifiableMap(new EnumMap<ShaderType, Identifier>(this.shaders)), this.definesBuilder.map(ShaderDefines.Builder::build), this.bindGroupLayouts.map(List::copyOf), this.colorTargetStates, this.activeColorTargetStateCount, this.depthStencilState, this.polygonMode, this.cull, this.vertexFormatPerBuffer, this.primitiveTopology, this.pushConstantSize);
        }

        public RenderPipeline build() {
            ColorTargetState[] activeColorTargetStates;
            if (this.location.isEmpty()) {
                throw new IllegalStateException("Missing location");
            }
            if (!this.shaders.containsKey((Object)ShaderType.VERTEX)) {
                throw new IllegalStateException("Missing vertex shader");
            }
            if (!this.shaders.containsKey((Object)ShaderType.FRAGMENT)) {
                throw new IllegalStateException("Missing fragment shader");
            }
            if (this.primitiveTopology.isEmpty()) {
                throw new IllegalStateException("Missing primitive topology");
            }
            if (this.activeColorTargetStateCount == 0) {
                activeColorTargetStates = new ColorTargetState[]{};
            } else {
                activeColorTargetStates = Arrays.copyOf(this.colorTargetStates, this.activeColorTargetStateCount);
                Optional<Object> lastBlend = Optional.empty();
                for (ColorTargetState activeColorTargetState : activeColorTargetStates) {
                    Optional<BlendFunction> currentBlend;
                    if (activeColorTargetState == null || !(currentBlend = activeColorTargetState.blendFunction()).isPresent()) continue;
                    if (lastBlend.isEmpty()) {
                        lastBlend = currentBlend;
                        continue;
                    }
                    if (currentBlend.equals(lastBlend)) continue;
                    throw new IllegalStateException("Blend functions must currently be the same for all color targets");
                }
            }
            int boundVertexAttribCount = 0;
            for (VertexFormat bindings : this.vertexFormatPerBuffer) {
                if (bindings == null) continue;
                boundVertexAttribCount += bindings.getElements().size();
            }
            if (boundVertexAttribCount > 16) {
                throw new IllegalStateException("Binding more than 16 vertex attributes is not supported");
            }
            if (this.pushConstantSize > 128) {
                throw new IllegalStateException("Maximum push constant size is 128 bytes");
            }
            return new RenderPipeline(this.location.get(), this.shaders, this.definesBuilder.orElse(ShaderDefines.builder()).build(), this.bindGroupLayouts.orElse(Collections.emptySet()), activeColorTargetStates, this.depthStencilState.orElse(null), this.polygonMode.orElse(PolygonMode.FILL), this.cull.orElse(true), this.vertexFormatPerBuffer, this.primitiveTopology.get(), this.pushConstantSize, nextPipelineSortKey++);
        }
    }

    public record Snippet(Map<ShaderType, Identifier> shaders, Optional<ShaderDefines> shaderDefines, Optional<List<BindGroupLayout>> bindGroupLayouts, @Nullable ColorTargetState[] colorTargetStates, int activeColorTargetStateCount, Optional<DepthStencilState> depthStencilState, Optional<PolygonMode> polygonMode, Optional<Boolean> cull, @Nullable VertexFormat[] vertexFormatPerBuffer, Optional<PrimitiveTopology> vertexFormatMode, int pushConstantSize) {
    }
}


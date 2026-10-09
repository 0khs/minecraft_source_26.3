/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.GL33C
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.opengl;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.backend.opengl.GlShaderModule;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.Uniform;
import com.mojang.renderpearl.util.ShaderCompileException;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL33C;
import org.slf4j.Logger;

public class GlProgram
implements UncheckedAutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<@Nullable Uniform> uniforms = new ReferenceArrayList();
    private @Nullable Uniform.Ubo pushConstant = null;
    private final int programId;
    private final String debugLabel;

    private GlProgram(int programId, String debugLabel) {
        this.programId = programId;
        this.debugLabel = debugLabel;
    }

    public static GlProgram link(List<GlShaderModule> compiledShaders, String debugLabel) throws ShaderCompileException {
        int programId = GlStateManager.glCreateProgram();
        if (programId <= 0) {
            throw new ShaderCompileException("Could not create shader program (returned program ID " + programId + ")");
        }
        for (GlShaderModule shaderModule : compiledShaders) {
            GlStateManager.glAttachShader(programId, shaderModule.getShaderId());
        }
        GlStateManager.glLinkProgram(programId);
        int linkStatus = GlStateManager.glGetProgrami(programId, 35714);
        String linkMessage = GlStateManager.glGetProgramInfoLog(programId, 32768);
        if (linkStatus == 0 || linkMessage.contains("Failed for unknown reason")) {
            throw new ShaderCompileException("Error encountered when linking program containing " + GlProgram.shaderList(compiledShaders) + ". Log output: " + linkMessage);
        }
        if (!linkMessage.isEmpty()) {
            LOGGER.info("Info log when linking program containing {}. Log output: {}", (Object)GlProgram.shaderList(compiledShaders), (Object)linkMessage);
        }
        return new GlProgram(programId, debugLabel);
    }

    private static String shaderList(List<GlShaderModule> compiledShaders) {
        return compiledShaders.stream().map(shader -> String.valueOf((Object)shader.getType()) + " " + shader.getLabel()).collect(Collectors.joining(", "));
    }

    public void setupBindGroupLayouts(List<BindGroupLayout.UniformDescription> uniforms) {
        int nextUboBinding = 0;
        int nextSamplerIndex = 0;
        GlStateManager._glUseProgram(this.programId);
        int pushConstantBlock = GL33C.glGetUniformBlockIndex((int)this.programId, (CharSequence)"_push_constants");
        if (pushConstantBlock != -1) {
            int uboBinding = nextUboBinding++;
            GL33C.glUniformBlockBinding((int)this.programId, (int)pushConstantBlock, (int)uboBinding);
            this.pushConstant = new Uniform.Ubo(uboBinding);
        }
        for (int i = 0; i < uniforms.size(); ++i) {
            BindGroupLayout.UniformDescription uniformDescription = uniforms.get(i);
            String uniformName = String.format(Locale.ROOT, "_uniform_%02d_%02d", 0, i);
            Uniform.Utb uniform = switch (uniformDescription.type()) {
                default -> throw new MatchException(null, null);
                case UniformType.UNIFORM_BUFFER -> {
                    int index = GL33C.glGetUniformBlockIndex((int)this.programId, (CharSequence)uniformName);
                    if (index == -1) {
                        yield null;
                    }
                    int uboBinding = nextUboBinding++;
                    GL33C.glUniformBlockBinding((int)this.programId, (int)index, (int)uboBinding);
                    yield new Uniform.Ubo(uboBinding);
                }
                case UniformType.TEXEL_BUFFER -> {
                    int location = GlStateManager._glGetUniformLocation(this.programId, uniformName);
                    if (location == -1) {
                        yield null;
                    }
                    int samplerIndex = nextSamplerIndex++;
                    GL33C.glUniform1i((int)location, (int)samplerIndex);
                    yield new Uniform.Utb(samplerIndex, Objects.requireNonNull(uniformDescription.gpuFormat()));
                }
                case UniformType.COMBINED_IMAGE_SAMPLER -> {
                    int location = GlStateManager._glGetUniformLocation(this.programId, uniformName);
                    if (location == -1) {
                        yield null;
                    }
                    int samplerIndex = nextSamplerIndex++;
                    GL33C.glUniform1i((int)location, (int)samplerIndex);
                    yield new Uniform.Sampler(samplerIndex);
                }
            };
            this.uniforms.add(uniform);
        }
        GlStateManager._glUseProgram(0);
    }

    @Override
    public void close() {
        this.uniforms.forEach(UncheckedAutoCloseable::safeClose);
        GlStateManager.glDeleteProgram(this.programId);
    }

    public @Nullable Uniform getUniform(int index) {
        if (index >= this.uniforms.size()) {
            return null;
        }
        return this.uniforms.get(index);
    }

    public int uniformCount() {
        return this.uniforms.size();
    }

    @VisibleForTesting
    public int getProgramId() {
        return this.programId;
    }

    public String toString() {
        return this.debugLabel;
    }

    public String getDebugLabel() {
        return this.debugLabel;
    }

    public @Nullable Uniform.Ubo pushConstant() {
        return this.pushConstant;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.shaderc.Shaderc
 *  org.lwjgl.util.shaderc.ShadercIncludeResolve
 *  org.lwjgl.util.shaderc.ShadercIncludeResolveI
 *  org.lwjgl.util.shaderc.ShadercIncludeResultRelease
 *  org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI
 */
package com.mojang.renderpearl.frontend.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.backend.api.SpvModule;
import com.mojang.renderpearl.frontend.shaders.SPIRVModule;
import com.mojang.renderpearl.util.ShaderCompileException;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.Map;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.util.shaderc.ShadercIncludeResolve;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;

public class GlslCompiler
implements UncheckedAutoCloseable {
    private final boolean isZeroToOne;
    private final boolean shaderDrawParameters;
    private final ShadercIncludeResultRelease includeResultRelease;
    private final ShaderSource.CachedIncludeSource missingIncludeResult;
    private final ShaderSource.CachedIncludeSource malformedIdResult;
    private final LongArrayList compilers = new LongArrayList();

    public GlslCompiler(boolean isZeroToOne, boolean shaderDrawParameters) {
        this.isZeroToOne = isZeroToOne;
        this.shaderDrawParameters = shaderDrawParameters;
        this.includeResultRelease = ShadercIncludeResultRelease.create(GlslCompiler::releaseIncludeResult);
        this.missingIncludeResult = ShaderSource.CachedIncludeSource.createError("not found");
        this.malformedIdResult = ShaderSource.CachedIncludeSource.createError("malformed id");
    }

    private ShaderSource.CachedIncludeSource processInclude(ShaderSource shaderSource, String requestedShader) {
        Identifier id = Identifier.tryParse(requestedShader);
        if (id == null) {
            return this.malformedIdResult;
        }
        ShaderSource.CachedIncludeSource shaderContents = shaderSource.getInclude(id);
        if (shaderContents == null) {
            return this.missingIncludeResult;
        }
        return shaderContents;
    }

    private ShadercIncludeResolve createIncludeResolver(ShaderSource shaderSource) {
        return ShadercIncludeResolve.create((l, requested_source, n, l2, l3) -> {
            String requestedShader = MemoryUtil.memASCII((long)requested_source);
            return this.processInclude(shaderSource, requestedShader).includeResultPtr();
        });
    }

    private static void releaseIncludeResult(long user_data, long include_result) {
    }

    @Override
    public void close() {
        this.includeResultRelease.close();
        this.malformedIdResult.close();
        this.missingIncludeResult.close();
        this.compilers.forEach(Shaderc::shaderc_compiler_release);
        this.compilers.clear();
    }

    private synchronized long acquireCompiler() {
        if (this.compilers.isEmpty()) {
            return Shaderc.shaderc_compiler_initialize();
        }
        return this.compilers.popLong();
    }

    private synchronized void releaseCompiler(long compiler) {
        this.compilers.add(compiler);
    }

    private long createBaseShaderOptions() {
        long shaderOptions = Shaderc.shaderc_compile_options_initialize();
        Shaderc.shaderc_compile_options_set_target_env((long)shaderOptions, (int)0, (int)0x402000);
        Shaderc.shaderc_compile_options_set_auto_bind_uniforms((long)shaderOptions, (boolean)true);
        Shaderc.shaderc_compile_options_set_preserve_bindings((long)shaderOptions, (boolean)false);
        Shaderc.shaderc_compile_options_set_generate_debug_info((long)shaderOptions);
        Shaderc.shaderc_compile_options_set_optimization_level((long)shaderOptions, (int)0);
        if (this.isZeroToOne) {
            Shaderc.shaderc_compile_options_add_macro_definition((long)shaderOptions, (CharSequence)"RENDERPEARL_DEPTH_IS_ZERO_TO_ONE", (CharSequence)"");
        }
        if (RenderSystem.getDevice().getDeviceInfo().hintsAndWorkarounds().isExplicitDepthRequired()) {
            Shaderc.shaderc_compile_options_add_macro_definition((long)shaderOptions, (CharSequence)"RENDERPEARL_EXPLICIT_DEPTH_INVARIANCE", (CharSequence)"");
        }
        if (this.shaderDrawParameters) {
            Shaderc.shaderc_compile_options_add_macro_definition((long)shaderOptions, (CharSequence)"RENDERPEARL_INSTANCE_INDEX_INCLUDES_BASE_INSTANCE", (CharSequence)"");
        }
        return shaderOptions;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SpvModule compileToSpv(String name, String source, ShaderType type, ShaderDefines shaderDefines, ShaderSource shaderSource) throws ShaderCompileException {
        int shaderType = type == ShaderType.FRAGMENT ? 1 : 0;
        ByteBuffer sourceBuffer = MemoryUtil.memUTF8((CharSequence)source, (boolean)false);
        ByteBuffer filenameBuffer = MemoryUtil.memUTF8((CharSequence)name);
        ByteBuffer entrypointBuffer = MemoryUtil.memUTF8((CharSequence)"main");
        long shaderOptions = this.createBaseShaderOptions();
        for (Map.Entry<String, String> macro : shaderDefines.values().entrySet()) {
            Shaderc.shaderc_compile_options_add_macro_definition((long)shaderOptions, (CharSequence)macro.getKey(), (CharSequence)macro.getValue());
        }
        for (String flag : shaderDefines.flags()) {
            Shaderc.shaderc_compile_options_add_macro_definition((long)shaderOptions, (CharSequence)flag, (CharSequence)"");
        }
        try (ShadercIncludeResolve includeResolver = this.createIncludeResolver(shaderSource);){
            SPIRVModule sPIRVModule;
            long result;
            Shaderc.shaderc_compile_options_set_include_callbacks((long)shaderOptions, (ShadercIncludeResolveI)includeResolver, (ShadercIncludeResultReleaseI)this.includeResultRelease, (long)0L);
            long compiler = this.acquireCompiler();
            try (Zone tracyZone = TracyClient.beginZone((String)"Compile to SPV", (boolean)false);){
                tracyZone.addText(name);
                result = Shaderc.shaderc_compile_into_spv((long)compiler, (ByteBuffer)sourceBuffer, (int)shaderType, (ByteBuffer)filenameBuffer, (ByteBuffer)entrypointBuffer, (long)shaderOptions);
            }
            finally {
                this.releaseCompiler(compiler);
            }
            try {
                int status = Shaderc.shaderc_result_get_compilation_status((long)result);
                if (status != 0) {
                    throw new ShaderCompileException("Couldn't parse GLSL: " + Shaderc.shaderc_result_get_error_message((long)result));
                }
                ByteBuffer spirv = Shaderc.shaderc_result_get_bytes((long)result);
                ByteBuffer copy = MemoryUtil.memCalloc((int)spirv.remaining());
                MemoryUtil.memCopy((ByteBuffer)spirv, (ByteBuffer)copy);
                sPIRVModule = new SPIRVModule(copy, type);
            }
            catch (Throwable throwable) {
                Shaderc.shaderc_result_release((long)result);
                Shaderc.shaderc_compile_options_release((long)shaderOptions);
                MemoryUtil.memFree((ByteBuffer)entrypointBuffer);
                MemoryUtil.memFree((ByteBuffer)filenameBuffer);
                MemoryUtil.memFree((ByteBuffer)sourceBuffer);
                throw throwable;
            }
            Shaderc.shaderc_result_release((long)result);
            Shaderc.shaderc_compile_options_release((long)shaderOptions);
            MemoryUtil.memFree((ByteBuffer)entrypointBuffer);
            MemoryUtil.memFree((ByteBuffer)filenameBuffer);
            MemoryUtil.memFree((ByteBuffer)sourceBuffer);
            return sPIRVModule;
        }
    }
}


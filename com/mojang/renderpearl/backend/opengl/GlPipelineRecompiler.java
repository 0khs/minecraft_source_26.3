/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.spvc.Spvc
 *  org.lwjgl.util.spvc.SpvcReflectedResource
 *  org.lwjgl.util.spvc.SpvcReflectedResource$Buffer
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.SpvModule;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlDebugLabel;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlShaderModule;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.frontend.shaders.SpvUtil;
import com.mojang.renderpearl.util.ShaderCompileException;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;
import org.slf4j.Logger;

public class GlPipelineRecompiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_LOG_LENGTH = 32768;
    private static final boolean IS_MACOS = Util.getPlatform() == Util.OS.OSX;
    public static final String UNIFORM_FORMAT_STRING = "_uniform_%02d_%02d";
    public static final String PUSH_CONSTANT_BLOCK_NAME = "_push_constants";
    private static final boolean SHADER_DEBUG_MODE = SharedConstants.IS_RENDERDOC_ATTACHED;
    private final GlDebugLabel debugLabels;
    private final boolean drawParametersSupported;

    public GlPipelineRecompiler(GlDebugLabel debugLabels, boolean drawParametersSupported) {
        this.debugLabels = debugLabels;
        this.drawParametersSupported = drawParametersSupported;
    }

    private String decompileShader(BackendRenderPipeline.CreateInfo.Shader shaderCreateInfo) throws ShaderCompileException {
        SpvModule spvModule = shaderCreateInfo.module();
        IntBuffer spirv = spvModule.spv().asIntBuffer();
        long spvcContext = 0L;
        try {
            String string;
            block22: {
                MemoryStack stack = MemoryStack.stackPush();
                try {
                    PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
                    SpvUtil.throwIfError(Spvc.spvc_context_create((PointerBuffer)pointerReturnBuffer), "Couldn't create spvc context");
                    spvcContext = pointerReturnBuffer.get(0);
                    SpvUtil.throwIfError(Spvc.spvc_context_parse_spirv((long)spvcContext, (IntBuffer)spirv, (long)spirv.remaining(), (PointerBuffer)pointerReturnBuffer), "Couldn't parse spirv");
                    long ir = pointerReturnBuffer.get(0);
                    SpvUtil.throwIfError(Spvc.spvc_context_create_compiler((long)spvcContext, (int)1, (long)ir, (int)0, (PointerBuffer)pointerReturnBuffer), "Couldn't create compiler");
                    long compiler = pointerReturnBuffer.get(0);
                    Spvc.spvc_compiler_create_compiler_options((long)compiler, (PointerBuffer)pointerReturnBuffer);
                    long options = pointerReturnBuffer.get(0);
                    Spvc.spvc_compiler_options_set_uint((long)options, (int)0x2000008, (int)330);
                    Spvc.spvc_compiler_options_set_bool((long)options, (int)0x2000007, (boolean)false);
                    Spvc.spvc_compiler_options_set_bool((long)options, (int)0x2000021, (boolean)true);
                    Spvc.spvc_compiler_options_set_bool((long)options, (int)0x2000005, (boolean)this.drawParametersSupported);
                    if (!SHADER_DEBUG_MODE) {
                        Spvc.spvc_compiler_options_set_bool((long)options, (int)16777270, (boolean)true);
                        Spvc.spvc_compiler_options_set_bool((long)options, (int)0x1000002, (boolean)true);
                    }
                    SpvUtil.throwIfError(Spvc.spvc_compiler_create_shader_resources((long)compiler, (PointerBuffer)pointerReturnBuffer), "Couldn't create resource list");
                    long spvcResources = pointerReturnBuffer.get(0);
                    if (!SHADER_DEBUG_MODE) {
                        if (spvModule.type() == ShaderType.VERTEX) {
                            this.renameInterfaceVariables(compiler, spvcResources, 3, "_vert_input_%02d");
                        }
                        if (spvModule.type() == ShaderType.FRAGMENT) {
                            this.renameInterfaceVariables(compiler, spvcResources, 4, "_frag_output_%02d");
                        }
                    }
                    if (!SHADER_DEBUG_MODE || IS_MACOS) {
                        if (spvModule.type() == ShaderType.VERTEX || spvModule.type() == ShaderType.FRAGMENT) {
                            this.renameInterfaceVariables(compiler, spvcResources, spvModule.type() == ShaderType.FRAGMENT ? 3 : 4, "_interface_variable_%02d");
                        }
                    } else {
                        Spvc.spvc_compiler_options_set_bool((long)options, (int)0x2000006, (boolean)true);
                    }
                    for (int i = 0; i < SpvUtil.DESCRIPTOR_TYPES.size(); ++i) {
                        int descriptorType = SpvUtil.DESCRIPTOR_TYPES.getInt(i);
                        this.renameDescriptors(compiler, spvcResources, descriptorType);
                    }
                    this.renameDescriptors(compiler, spvcResources, 9);
                    String string2 = shaderCreateInfo.entryPoint();
                    Spvc.spvc_compiler_set_entry_point((long)compiler, (CharSequence)string2, (int)(switch (spvModule.type()) {
                        default -> throw new MatchException(null, null);
                        case ShaderType.VERTEX -> 0;
                        case ShaderType.FRAGMENT -> 4;
                    }));
                    Spvc.spvc_compiler_install_compiler_options((long)compiler, (long)options);
                    Spvc.spvc_compiler_compile((long)compiler, (PointerBuffer)pointerReturnBuffer);
                    string = MemoryUtil.memASCII((long)pointerReturnBuffer.get(0));
                    if (stack == null) break block22;
                }
                catch (Throwable throwable) {
                    if (stack != null) {
                        try {
                            stack.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                stack.close();
            }
            return string;
        }
        finally {
            if (spvcContext != 0L) {
                Spvc.spvc_context_destroy((long)spvcContext);
            }
        }
    }

    private void renameInterfaceVariables(long compiler, long spvcResources, int resourceType, String formatString) throws ShaderCompileException {
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer countPointer = stack.callocPointer(1);
            IntBuffer intReturnBuffer = stack.callocInt(1);
            PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_resources_get_resource_list_for_type((long)spvcResources, (int)resourceType, (PointerBuffer)pointerReturnBuffer, (PointerBuffer)countPointer), "Couldn't get resource list");
            long interfaceVariableCount = countPointer.get(0);
            SpvcReflectedResource.Buffer interfaceVariables = SpvcReflectedResource.create((long)pointerReturnBuffer.get(0), (int)((int)interfaceVariableCount));
            int i = 0;
            while ((long)i < interfaceVariableCount) {
                SpvcReflectedResource interfaceVariable = (SpvcReflectedResource)interfaceVariables.get(i);
                int location = Spvc.spvc_compiler_get_decoration((long)compiler, (int)interfaceVariable.id(), (int)30);
                Spvc.spvc_compiler_set_name((long)compiler, (int)interfaceVariable.id(), (CharSequence)String.format(Locale.ROOT, formatString, location));
                ++i;
            }
        }
    }

    private void renameDescriptors(long compiler, long spvcResources, int resourceType) throws ShaderCompileException {
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
            IntBuffer intReturnBuffer = stack.callocInt(1);
            PointerBuffer countPointer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_resources_get_resource_list_for_type((long)spvcResources, (int)resourceType, (PointerBuffer)pointerReturnBuffer, (PointerBuffer)countPointer), "Couldn't list input variables");
            long resourceCount = countPointer.get(0);
            SpvcReflectedResource.Buffer resources = SpvcReflectedResource.create((long)pointerReturnBuffer.get(0), (int)((int)resourceCount));
            int i = 0;
            while ((long)i < resourceCount) {
                SpvcReflectedResource resource = (SpvcReflectedResource)resources.get(i);
                int descriptorSet = Spvc.spvc_compiler_get_decoration((long)compiler, (int)resource.id(), (int)34);
                int binding = Spvc.spvc_compiler_get_decoration((long)compiler, (int)resource.id(), (int)33);
                switch (resourceType) {
                    case 9: {
                        String existingBlockName;
                        String blockName = "_push_constants_instance";
                        if (SHADER_DEBUG_MODE && (existingBlockName = Spvc.spvc_compiler_get_name((long)compiler, (int)resource.base_type_id())) != null) {
                            blockName = existingBlockName;
                        }
                        Spvc.spvc_compiler_set_name((long)compiler, (int)resource.id(), (CharSequence)blockName);
                        Spvc.spvc_compiler_set_name((long)compiler, (int)resource.base_type_id(), (CharSequence)PUSH_CONSTANT_BLOCK_NAME);
                        break;
                    }
                    case 1: 
                    case 2: {
                        String existingBlockName;
                        String blockName = String.format(Locale.ROOT, "_uniform_instance_%02d_%02d", descriptorSet, binding);
                        if (SHADER_DEBUG_MODE && (existingBlockName = Spvc.spvc_compiler_get_name((long)compiler, (int)resource.base_type_id())) != null) {
                            blockName = existingBlockName;
                        }
                        Spvc.spvc_compiler_set_name((long)compiler, (int)resource.id(), (CharSequence)blockName);
                        Spvc.spvc_compiler_set_name((long)compiler, (int)resource.base_type_id(), (CharSequence)String.format(Locale.ROOT, UNIFORM_FORMAT_STRING, descriptorSet, binding));
                        break;
                    }
                    case 6: 
                    case 7: 
                    case 10: 
                    case 11: {
                        Spvc.spvc_compiler_set_name((long)compiler, (int)resource.id(), (CharSequence)String.format(Locale.ROOT, UNIFORM_FORMAT_STRING, descriptorSet, binding));
                    }
                }
                ++i;
            }
        }
    }

    private GlShaderModule compileShader(String name, ShaderType type, String source) {
        int shaderId = GlStateManager.glCreateShader(GlConst.toGl(type));
        GlStateManager.glShaderSource(shaderId, source);
        GlStateManager.glCompileShader(shaderId);
        if (GlStateManager.glGetShaderi(shaderId, 35713) == 0) {
            String logInfo = StringUtils.trim((String)GlStateManager.glGetShaderInfoLog(shaderId, 32768));
            LOGGER.error("Couldn't compile {} shader for pipeline ({}): {}", new Object[]{type.getName(), name, logInfo});
            return GlShaderModule.INVALID_SHADER;
        }
        GlShaderModule module = new GlShaderModule(shaderId, name, type);
        this.debugLabels.applyLabel(module);
        return module;
    }

    public @Nullable Map<BackendRenderPipeline.CreateInfo.Shader, String> decompileShaders(BackendRenderPipeline.CreateInfo createInfo) {
        Reference2ReferenceArrayMap reference2ReferenceArrayMap;
        block9: {
            Zone tracyZone = TracyClient.beginZone((String)"Decompile shaders", (boolean)false);
            try {
                tracyZone.addText(createInfo.name());
                Reference2ReferenceArrayMap decompiledShaders = new Reference2ReferenceArrayMap();
                for (BackendRenderPipeline.CreateInfo.Shader shader : createInfo.shaders()) {
                    decompiledShaders.put(shader, this.decompileShader(shader));
                }
                reference2ReferenceArrayMap = decompiledShaders;
                if (tracyZone == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (tracyZone != null) {
                        try {
                            tracyZone.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (ShaderCompileException e) {
                    LOGGER.error("Couldn't compile program for pipeline {}", (Object)createInfo.name(), (Object)e);
                    return null;
                }
            }
            tracyZone.close();
        }
        return reference2ReferenceArrayMap;
    }

    /*
     * Exception decompiling
     */
    public @Nullable GlProgram compileProgram(BackendRenderPipeline.CreateInfo createInfo, Map<BackendRenderPipeline.CreateInfo.Shader, String> decompiledShaders) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [12[WHILELOOP]], but top level block is 3[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }
}


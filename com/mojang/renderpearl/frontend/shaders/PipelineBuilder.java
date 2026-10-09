/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  it.unimi.dsi.fastutil.objects.ReferenceLists
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.frontend.shaders;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.api.SpvModule;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import com.mojang.renderpearl.frontend.shaders.GlslCompiler;
import com.mojang.renderpearl.frontend.shaders.SpvUtil;
import com.mojang.renderpearl.util.ShaderCompileException;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PipelineBuilder
implements UncheckedAutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GpuDeviceBackend backendDevice;
    private final GlslCompiler compiler;

    public PipelineBuilder(GpuDeviceBackend backendDevice) {
        this.backendDevice = backendDevice;
        DeviceInfo deviceInfo = backendDevice.getDeviceInfo();
        this.compiler = new GlslCompiler(deviceInfo.isZZeroToOne(), deviceInfo.features().shaderDrawParameters());
    }

    @Override
    public void close() {
        this.compiler.close();
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    private @Nullable BackendRenderPipeline.CreateInfo generateBackendCreateInfo(RenderPipeline pipeline, ShaderSource shaderSource, ReferenceArrayList<BackendRenderPipeline.CreateInfo.Shader> shaderCreateInfos, Object2IntOpenHashMap<String> uniformBindings) {
        try {
            Int2ObjectMap<InterfaceVariableInfo> outputSlotMap;
            String vertexStageName;
            EnumMap<ShaderType, SpvModule> shaderSources = new EnumMap<ShaderType, SpvModule>(ShaderType.class);
            for (Map.Entry<ShaderType, Identifier> shader2 : pipeline.getShaders().entrySet()) {
                String source = PipelineBuilder.loadShaderSource(shader2.getValue(), shader2.getKey(), shaderSource);
                if (source == null) {
                    LOGGER.error("Couldn't find source for {} shader ({})", (Object)shader2.getKey(), (Object)shader2.getValue());
                    return null;
                }
                SpvModule spvModule = this.compiler.compileToSpv(shader2.getValue().toString(), source, shader2.getKey(), pipeline.getShaderDefines(), shaderSource);
                shaderSources.put(shader2.getKey(), spvModule);
                shaderCreateInfos.add((Object)new BackendRenderPipeline.CreateInfo.Shader(shader2.getValue().toString(), "main", spvModule));
            }
            ReferenceArrayList vertexBuffers = new ReferenceArrayList();
            ReferenceArrayList attribBindings = new ReferenceArrayList();
            if (shaderSources.containsKey((Object)ShaderType.VERTEX)) {
                @Nullable Int2ObjectArrayMap attribFormats = new Int2ObjectArrayMap();
                SpvModule vertexShader = (SpvModule)shaderSources.get((Object)ShaderType.VERTEX);
                List<SpvModule.Reflection.InterfaceVariable> vertexShaderInputs = vertexShader.reflect().inputs();
                String vertexStageName2 = pipeline.getShaders().get((Object)ShaderType.VERTEX).toString();
                @Nullable ObjectIterator vertexShaderInputsByName = vertexShaderInputs.stream().collect(Collectors.toUnmodifiableMap(SpvModule.Reflection.InterfaceVariable::name, Function.identity()));
                List<@Nullable VertexFormat> vertexBindings = pipeline.getVertexFormatBindings();
                String previousElementName = null;
                int previousElementLocation = 0;
                for (int i = 0; i < vertexBindings.size(); ++i) {
                    VertexFormat vertexBinding = vertexBindings.get(i);
                    if (vertexBinding == null) continue;
                    vertexBuffers.add(new BackendRenderPipeline.CreateInfo.VertexBuffer(i, vertexBinding.getVertexSize(), vertexBinding.getStepRate()));
                    for (VertexFormatElement element : vertexBinding.getElements()) {
                        SpvModule.Reflection.InterfaceVariable input = (SpvModule.Reflection.InterfaceVariable)vertexShaderInputsByName.get(element.name());
                        if (input == null) continue;
                        int attribLocation = element.name().equals(previousElementName) ? previousElementLocation + 1 : input.location();
                        attribBindings.add(new BackendRenderPipeline.CreateInfo.AttribBinding(i, attribLocation, element.offset(), element.format()));
                        attribFormats.put(attribLocation, (Object)element.format());
                        previousElementName = element.name();
                        previousElementLocation = attribLocation;
                    }
                }
                for (SpvModule.Reflection.InterfaceVariable vertexShaderInput : vertexShaderInputs) {
                    if (vertexShaderInput.decoration(31) != 0) {
                        throw new ShaderCompileException(String.format(Locale.ROOT, "vertex shader (%s) attrib (%s) has component decoration, this is not permitted", vertexStageName2, vertexShaderInput.name()));
                    }
                    GpuFormat format = (GpuFormat)((Object)attribFormats.get(vertexShaderInput.location()));
                    if (format == null) {
                        throw new ShaderCompileException(String.format(Locale.ROOT, "vertex shader (%s) attrib (%s) does not have a matching vertex buffer element", vertexStageName2, vertexShaderInput.name()));
                    }
                    int expectedBaseType = switch (format.componentType()) {
                        case GpuFormat.ComponentType.UNORM_8, GpuFormat.ComponentType.SNORM_8, GpuFormat.ComponentType.UNORM_16, GpuFormat.ComponentType.SNORM_16, GpuFormat.ComponentType.FLOAT_16, GpuFormat.ComponentType.FLOAT_32 -> 13;
                        case GpuFormat.ComponentType.UINT_8, GpuFormat.ComponentType.UINT_16, GpuFormat.ComponentType.UINT_32 -> 8;
                        case GpuFormat.ComponentType.SINT_8, GpuFormat.ComponentType.SINT_16, GpuFormat.ComponentType.SINT_32 -> 7;
                        default -> throw new ShaderCompileException("Unexpected value: " + String.valueOf((Object)format.componentType()));
                    };
                    if (expectedBaseType != vertexShaderInput.type().baseType()) {
                        throw new ShaderCompileException(String.format(Locale.ROOT, "Unexpected base type for input attribute %s in vertex shader %s, expected %s got %s", vertexShaderInput.name(), vertexStageName2, SpvUtil.baseTypeString(expectedBaseType), SpvUtil.baseTypeString(vertexShaderInput.type().baseType())));
                    }
                    if (vertexShaderInput.type().vectorSize() <= format.componentCount()) continue;
                    throw new ShaderCompileException(String.format(Locale.ROOT, "Not enough components for input attribute %s in vertex shader %s, expected at least %s got %s", vertexShaderInput.name(), vertexStageName2, vertexShaderInput.type().vectorSize(), format.componentCount()));
                }
            }
            if (shaderSources.containsKey((Object)ShaderType.VERTEX)) {
                vertexStageName = pipeline.getShaders().get((Object)ShaderType.VERTEX).toString();
                outputSlotMap = PipelineBuilder.generateSlotMap(((SpvModule)shaderSources.get((Object)ShaderType.VERTEX)).reflect().outputs(), vertexStageName, ShaderType.VERTEX);
            } else {
                vertexStageName = "";
                outputSlotMap = Int2ObjectMaps.emptyMap();
            }
            String fragmentStageName = pipeline.getShaders().get((Object)ShaderType.FRAGMENT).toString();
            Int2ObjectMap<@Nullable InterfaceVariableInfo> inputSlotMap = PipelineBuilder.generateSlotMap(((SpvModule)shaderSources.get((Object)ShaderType.FRAGMENT)).reflect().inputs(), vertexStageName, ShaderType.FRAGMENT);
            for (Int2ObjectMap.Entry entry : inputSlotMap.int2ObjectEntrySet()) {
                int location = entry.getIntKey();
                InterfaceVariableInfo input = (InterfaceVariableInfo)entry.getValue();
                assert (input != null);
                InterfaceVariableInfo output = (InterfaceVariableInfo)outputSlotMap.get(location);
                if (output == null) {
                    throw new ShaderCompileException(String.format(Locale.ROOT, "Vertex shader (%s) missing output at location %d consumed by Fragment shader (%s)", vertexStageName, location, fragmentStageName));
                }
                if (output.equals(input)) continue;
                if (output.baseType() != input.baseType()) {
                    throw new ShaderCompileException(String.format(Locale.ROOT, "Vertex shader (%s) and Fragment shader (%s) have base type mismatch at location %d of %s and %s.", vertexStageName, fragmentStageName, location, SpvUtil.baseTypeString(output.baseType()), SpvUtil.baseTypeString(input.baseType())));
                }
                if (output.vectorLength() != input.vectorLength()) {
                    throw new ShaderCompileException(String.format(Locale.ROOT, "Vertex shader (%s) and Fragment shader (%s) have vector length mismatch at location %d of %d and %d.", vertexStageName, fragmentStageName, location, output.vectorLength(), input.vectorLength()));
                }
                if (output.flatInterpolated() == input.flatInterpolated()) continue;
                throw new ShaderCompileException(String.format(Locale.ROOT, "Vertex shader (%s) and Fragment shader (%s) have mismatched interpolation at location %d, flat in %s", vertexStageName, fragmentStageName, location, output.flatInterpolated() ? "vertex" : "fragment"));
            }
            List<BindGroupLayout.UniformDescription> pipelineUniforms = BindGroupLayout.flattenUniforms(pipeline.getBindGroupLayouts());
            @Nullable Object2ObjectOpenHashMap pipelineUniformsByName = new Object2ObjectOpenHashMap();
            for (BindGroupLayout.UniformDescription pipelineUniform : pipelineUniforms) {
                pipelineUniformsByName.put(pipelineUniform.name(), pipelineUniform);
            }
            IntArrayList uniformResourceTypes = new IntArrayList();
            IntArrayList uniformDimensions = new IntArrayList();
            ReferenceArrayList uniforms = new ReferenceArrayList();
            for (BackendRenderPipeline.CreateInfo.Shader shader3 : shaderCreateInfos) {
                SpvModule.Reflection reflectionInfo = shader3.module().reflect();
                for (SpvModule.Reflection.Descriptor descriptor : reflectionInfo.descriptors()) {
                    String uniformName = descriptor.name();
                    SpvModule.Reflection.Type type = descriptor.type();
                    BindGroupLayout.UniformDescription pipelineUniform = (BindGroupLayout.UniformDescription)pipelineUniformsByName.get(uniformName);
                    if (pipelineUniform == null) {
                        throw new ShaderCompileException("Unable to find shader defined uniform (" + uniformName + ")");
                    }
                    int newBinding = uniformBindings.computeIfAbsent((Object)descriptor.name(), object -> uniformBindings.size());
                    if (newBinding >= uniformResourceTypes.size()) {
                        uniformResourceTypes.add(descriptor.resourceType());
                        uniformDimensions.add(type.dimensions());
                        uniforms.add((Object)new BindGroupLayout.UniformDescription(pipelineUniform.name(), pipelineUniform.type(), pipelineUniform.gpuFormat()));
                    } else {
                        if (descriptor.resourceType() != uniformResourceTypes.getInt(newBinding)) {
                            throw new ShaderCompileException("Uniform type for " + descriptor.name() + " does not match across all stages");
                        }
                        if (type.dimensions() != uniformDimensions.getInt(newBinding)) {
                            throw new ShaderCompileException("Uniform dimensions for " + descriptor.name() + " does not match across all stages");
                        }
                    }
                    descriptor.binding(newBinding);
                    descriptor.descriptorSetIndex(0);
                }
            }
            for (String uniformName : uniformBindings.keySet()) {
                BindGroupLayout.UniformDescription pipelineUniform = (BindGroupLayout.UniformDescription)pipelineUniformsByName.get(uniformName);
                assert (pipelineUniform != null);
                int uniformBinding = uniformBindings.getInt((Object)uniformName);
                int definedResourceType = uniformResourceTypes.getInt(uniformBinding);
                int expectedResourceType = SpvUtil.resourceType(pipelineUniform.type());
                int definedDimensions = uniformDimensions.getInt(uniformBinding);
                if (expectedResourceType != definedResourceType) {
                    throw new ShaderCompileException("Uniform type in shader does not match expected type from RenderPipeline for (" + uniformName + ")");
                }
                if (definedDimensions == Integer.MAX_VALUE) continue;
                UniformType expectedUniformType = switch (definedDimensions) {
                    case 0 -> throw new ShaderCompileException("1D textures not supported (" + uniformName + ")");
                    case 1 -> UniformType.COMBINED_IMAGE_SAMPLER;
                    case 2 -> throw new ShaderCompileException("3D textures not supported (" + uniformName + ")");
                    case 3 -> UniformType.COMBINED_IMAGE_SAMPLER;
                    case 4 -> UniformType.COMBINED_IMAGE_SAMPLER;
                    case 5 -> UniformType.TEXEL_BUFFER;
                    default -> throw new ShaderCompileException("Unexpected SpvDim (" + definedDimensions + ") for uniform (" + uniformName + ")");
                };
                if (pipelineUniform.type() == expectedUniformType) continue;
                throw new ShaderCompileException("Unexpected dimensions for uniform (" + uniformName + "), does not match RenderPipeline definition");
            }
            for (BackendRenderPipeline.CreateInfo.Shader shader3 : shaderCreateInfos) {
                List<SpvModule.Reflection.PushConstant> pushConstants = shader3.module().reflect().pushConstants();
                if (pushConstants.isEmpty()) continue;
                if (pushConstants.size() > 1) {
                    throw new ShaderCompileException("Shader may define at most one push_constant block");
                }
                SpvModule.Reflection.PushConstant pushConstant = pushConstants.getFirst();
                if (pushConstant.size() <= pipeline.pushConstantSize()) continue;
                throw new ShaderCompileException("Shader push constant size exceeds pipeline declared size");
            }
            return new BackendRenderPipeline.CreateInfo(pipeline.getLocation().toString(), List.copyOf(shaderCreateInfos), List.copyOf(vertexBuffers), List.copyOf(attribBindings), List.copyOf(uniforms), pipeline.pushConstantSize(), pipeline.getDepthStencilState(), pipeline.getPolygonMode(), pipeline.isCull(), pipeline.getColorTargetStates(), pipeline.getPrimitiveTopology());
        }
        catch (ShaderCompileException e) {
            LOGGER.error("Couldn't compile pipeline ({}): ", (Object)pipeline.getLocation(), (Object)e);
            shaderCreateInfos.forEach(shader -> shader.module().close());
            return null;
        }
    }

    public CompletableFuture<CompiledRenderPipeline.Pending> compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource, Executor executor) {
        if (pipeline.getPolygonMode() == PolygonMode.WIREFRAME && !this.backendDevice.getDeviceInfo().features().wireframeFillMode()) {
            LOGGER.error("Pipeline {} uses {} fill mode, not supported by device", (Object)pipeline.getLocation(), (Object)PolygonMode.WIREFRAME);
            return CompletableFuture.completedFuture(CompiledRenderPipeline.Pending.NULL);
        }
        return CompletableFuture.supplyAsync(() -> {
            BackendRenderPipeline.Pending backendPendingPipeline;
            BackendRenderPipeline.CreateInfo createInfo;
            ReferenceArrayList shaderCreateInfos = new ReferenceArrayList();
            Object2IntOpenHashMap uniformBindings = new Object2IntOpenHashMap();
            try (Zone tracyZone = TracyClient.beginZone((String)"Frontend Compile and reflection", (boolean)false);){
                tracyZone.addText(pipeline.getLocation().toString());
                createInfo = this.generateBackendCreateInfo(pipeline, shaderSource, (ReferenceArrayList<BackendRenderPipeline.CreateInfo.Shader>)shaderCreateInfos, (Object2IntOpenHashMap<String>)uniformBindings);
            }
            if (createInfo == null) {
                return CompiledRenderPipeline.Pending.NULL;
            }
            try (Zone tracyZone = TracyClient.beginZone((String)"Backend Compile", (boolean)false);){
                tracyZone.addText(pipeline.getLocation().toString());
                backendPendingPipeline = this.backendDevice.compilePipeline(createInfo);
            }
            return () -> {
                BackendRenderPipeline backendPipeline;
                try (Zone tracyZone = TracyClient.beginZone((String)"Complete Compile", (boolean)false);){
                    tracyZone.addText(pipeline.getLocation().toString());
                    backendPipeline = backendPendingPipeline.finishCompile();
                }
                executor.execute(() -> shaderCreateInfos.forEach(shader -> shader.module().close()));
                if (backendPipeline == null) {
                    return null;
                }
                return new FrontendRenderPipeline(pipeline.getLocation().toString(), backendPipeline, (List<VertexFormat>)ReferenceLists.unmodifiable((ReferenceList)new ReferenceArrayList(pipeline.getVertexFormatBindings())), (Object2IntMap<String>)Object2IntMaps.unmodifiable((Object2IntMap)uniformBindings), BindGroupLayout.flattenUniforms(pipeline.getBindGroupLayouts()), (List<ColorTargetState>)ReferenceLists.unmodifiable((ReferenceList)new ReferenceArrayList(pipeline.getColorTargetStates())), pipeline.wantsDepthTexture(), pipeline.pushConstantSize());
            };
        }, executor);
    }

    private static @Nullable String loadShaderSource(Identifier id, ShaderType type, ShaderSource shaderSource) {
        String source = shaderSource.getShader(id, type);
        if (source == null) {
            LOGGER.error("Couldn't find source for {} shader ({})", (Object)type, (Object)id);
            return null;
        }
        return source;
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    private static Int2ObjectMap<@Nullable InterfaceVariableInfo> generateSlotMap(List<SpvModule.Reflection.InterfaceVariable> interfaceVariables, String stageName, ShaderType shaderType) throws ShaderCompileException {
        @Nullable Int2ObjectArrayMap slotMap = new Int2ObjectArrayMap();
        for (SpvModule.Reflection.InterfaceVariable interfaceVariable : interfaceVariables) {
            int i;
            SpvModule.Reflection.Type interfaceVariableType = interfaceVariable.type();
            int baseLocation = interfaceVariable.location();
            if (interfaceVariable.decoration(31) != 0) {
                throw new ShaderCompileException(String.format(Locale.ROOT, "%s shader (%s) interface (%s) has component decoration, this is not permitted", shaderType.getName(), stageName, interfaceVariable.name()));
            }
            switch (interfaceVariableType.baseType()) {
                case 9: 
                case 10: 
                case 14: 
                case 15: {
                    throw new ShaderCompileException(String.format(Locale.ROOT, "Unsupported interface variable type %s in %s shader (%s) location %d", SpvUtil.baseTypeString(interfaceVariableType.baseType()), shaderType.getName(), stageName, baseLocation));
                }
            }
            InterfaceVariableInfo info = new InterfaceVariableInfo(interfaceVariableType.baseType(), interfaceVariableType.vectorSize(), interfaceVariable.hasDecoration(14));
            int totalArraySize = 1;
            int arrayDimensions = interfaceVariableType.arrayDimensions();
            if (arrayDimensions > 0) {
                for (i = 0; i < arrayDimensions; ++i) {
                    totalArraySize *= interfaceVariableType.arrayLength(i);
                }
            }
            for (i = 0; i < totalArraySize; ++i) {
                slotMap.put(baseLocation + i, (Object)info);
            }
        }
        return slotMap;
    }

    private record InterfaceVariableInfo(int baseType, int vectorLength, boolean flatInterpolated) {
    }
}

